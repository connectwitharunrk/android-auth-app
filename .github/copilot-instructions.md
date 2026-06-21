# Copilot Instructions

## Project Overview

Full-stack Android authentication app: Kotlin/Jetpack Compose frontend with a Node.js/Express/MySQL backend. The two sides are independent sub-projects in `android/` and `backend/`.

## Build, Test & Run Commands

### Backend
```bash
cd backend
npm install          # install dependencies
npm run migrate      # run DB migrations (requires MySQL with auth_db database)
npm run dev          # start dev server with nodemon (port 3000)
npm start            # start production server
npm test             # run Jest tests (Jest must be installed separately)
npm test -- --coverage  # with coverage report
```

### Android
All Gradle commands must be run from the `android/` directory:
```bash
cd android
./gradlew assembleDebug          # build debug APK
./gradlew test                   # run all unit tests
./gradlew connectedAndroidTest   # run instrumented/UI tests (device required)
./gradlew testDebugUnitTest --tests "LoginViewModelTest.signin*"  # run single test
./gradlew jacocoTestReport       # generate coverage report
```

## Architecture

### Android — Clean Architecture + MVI

Three strict layers with one-way dependency: **Presentation → Domain → Data**

```
presentation/   ← Compose screens + ViewModels (MVI)
domain/         ← Use cases + repository interfaces + domain models
data/           ← Repository implementations, Retrofit ApiService, DataStore TokenManager
di/             ← Hilt modules (NetworkModule, RepositoryModule)
```

**MVI flow per screen:**
```
Screen (Intent) → ViewModel.processIntent() → UseCase → Repository
                                                              ↓
Screen (recompose) ← StateFlow<XState> ← ViewModel ← Result<T>
                   ← Channel<XEvent> (one-time side effects, e.g. navigation)
```

Each screen has co-located MVI classes in its own file (e.g. `LoginViewModel.kt` contains `LoginViewModel`, `LoginIntent`, `LoginState`, and `LoginEvent`).

**State pattern:** `sealed class XState { Idle, Loading, Success(data), Error(message) }`  
**Events** (navigation, toasts) use `Channel<XEvent>` and are collected once via `receiveAsFlow()`.

### Backend — Express REST API

```
src/
  controllers/authController.js   ← request handling logic
  routes/authRoutes.js            ← route definitions
  middleware/auth.js              ← JWT verification middleware
  migrations/                     ← DB schema migration scripts
  config/                         ← DB connection config
  index.js                        ← Express app entry point
```

API endpoints all under `/api/auth/`: `POST signup`, `POST signin`, `POST logout`, `GET me`.  
JWT is issued on sign-in and validated via the `auth` middleware on protected routes.

## Key Conventions

### Android

- **Hilt DI everywhere:** All ViewModels use `@HiltViewModel` + `@Inject constructor`. Modules in `di/` install into `SingletonComponent`. Add new dependencies via `NetworkModule` or `RepositoryModule`.
- **Repository interface in domain, impl in data:** `AuthRepository` (interface) lives in `domain/repository/`, `AuthRepositoryImpl` lives in `data/repository/`. Hilt binds them in `RepositoryModule`.
- **DTOs stay in data layer:** `AuthRequest` / `AuthResponse` live in `data/remote/dto/`. Convert to domain `User` model inside `AuthRepositoryImpl` before returning to domain/presentation.
- **`Result<T>` for error propagation:** Use cases and repository methods return `kotlin.Result<T>`. ViewModels call `.onSuccess` / `.onFailure` — no exceptions bubble to UI.
- **Base URL configuration:** `NetworkModule.BASE_URL` defaults to `http://10.0.2.2:3000/` (emulator). Change to `http://<YOUR_IP>:3000/` for a physical device.
- **Token storage:** `TokenManager` wraps DataStore Preferences. Use it for all JWT read/write operations; do not store tokens in SharedPreferences or memory only.

### Backend

- **MySQL with `mysql2` promise API:** All DB queries use the promise-based pool from `config/`. Wrap in try/catch and return `{ success, message, data?, token? }` shaped responses.
- **bcryptjs for passwords, jsonwebtoken for tokens:** Never store plain-text passwords. Hash on signup, compare on signin, sign JWT with `process.env.JWT_SECRET`.
- **Migrations over manual schema:** Add schema changes as new migration files under `src/migrations/` and run via `npm run migrate`.
- **Environment variables via `.env`:** Copy `.env.example` to `.env` before running. Required vars: `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `JWT_SECRET`, `PORT`.
