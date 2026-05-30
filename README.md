# Android Authentication App

A modern Android application demonstrating login/logout flow using Jetpack Compose, Clean Architecture, MVI Pattern, and Retrofit.

## Tech Stack

### Android
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture with MVI Pattern
- **Network**: Retrofit + OkHttp
- **Async**: Coroutines + Flow
- **Navigation**: Jetpack Navigation
- **State Management**: ViewModel + State Management
- **Dependency Injection**: Hilt
- **Local Storage**: DataStore

### Backend
- **Runtime**: Node.js
- **Framework**: Express.js
- **Database**: MySQL
- **Authentication**: JWT

## Project Structure

```
android-auth-app/
├── android/                    # Android Application
│   ├── app/
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/example/authapp/
│   │       │   │   ├── data/              # Data Layer
│   │       │   │   ├── domain/            # Domain Layer
│   │       │   │   ├── presentation/      # Presentation Layer (MVI)
│   │       │   │   └── di/                # Dependency Injection
│   │       │   └── res/
│   │       └── test/
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── backend/                    # Node.js Backend
│   ├── src/
│   ├── package.json
│   └── .env
└── docs/                       # Documentation
```

## Getting Started

### Prerequisites
- Android Studio (latest version)
- Kotlin 1.9+
- Node.js 18+
- MySQL 8.0+
- Git

### Setup Instructions

#### 1. Setup Backend

```bash
cd backend
npm install
```

Create MySQL database:
```sql
CREATE DATABASE auth_db;
```

Run migrations:
```bash
npm run migrate
```

Start server:
```bash
npm run dev
```

#### 2. Setup Android App

- Open `android` directory in Android Studio
- Update API endpoint in `app/src/main/java/com/example/authapp/di/NetworkModule.kt`
  - For emulator: `http://10.0.2.2:3000/`
  - For physical device: `http://YOUR_IP:3000/`
- Build and run on emulator or device

## Features

✅ **Authentication**
- User Registration (Sign Up)
- User Login (Sign In)
- JWT Token Management
- Logout with Token Cleanup

✅ **UI/UX**
- Modern Jetpack Compose UI
- Smooth Navigation Transitions
- Loading States
- Error Handling
- Input Validation

✅ **Architecture**
- Clean Architecture Layers
- MVI (Model-View-Intent) Pattern
- Separation of Concerns
- Dependency Injection with Hilt

✅ **Data Management**
- Coroutines for Async Operations
- Flow for Reactive Data
- Local Token Storage with DataStore
- Retrofit for API Communication

## API Endpoints

- `POST /api/auth/signup` - User Registration
- `POST /api/auth/signin` - User Login
- `POST /api/auth/logout` - User Logout
- `GET /api/auth/me` - Get Current User

## Architecture Layers

### Data Layer
- Remote: Retrofit API calls
- Local: DataStore token management
- Repository: Implements domain repository interface

### Domain Layer
- Models: Business entities (User)
- Repositories: Interfaces for data operations
- UseCases: Business logic (SignIn, SignUp, Logout)

### Presentation Layer (MVI)
- Screens: Composable UI components
- ViewModels: MVI state management
- Navigation: Jetpack Navigation
- Events: Intent-based interactions

## MVI Pattern Flow

```
Screen (Intent) → ViewModel (Intent Handler) → Repository (Use Case) 
→ API/Local (Data) → Repository → ViewModel (State Update) → Screen (Recompose)
```

## Author

Arun RK (@connectwitharunrk)

## License

MIT License
