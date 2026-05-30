# Architecture Documentation

## Overview

This project follows **Clean Architecture** principles combined with the **MVI (Model-View-Intent)** pattern for state management. The architecture is divided into three main layers:

```
┌─────────────────────────────────────────────────┐
│         Presentation Layer (MVI)                │
│  ┌─────────────┐  ┌──────────┐  ┌──────────┐  │
│  │   Screen    │→ │ ViewModel│→ │  State   │  │
│  │  (Intent)   │  │ (Handler)│  │ (Output) │  │
│  └─────────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────┐
│         Domain Layer (Business Logic)           │
│  ┌────────────┐  ┌─────────────┐               │
│  │  UseCase   │→ │ Repository  │               │
│  │            │  │ (Interface) │               │
│  └────────────┘  └─────────────┘               │
└─────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────┐
│  Data Layer (API, Local Storage, Database)     │
│  ┌──────────┐  ┌──────────┐  ┌────────────┐   │
│  │ Retrofit │  │DataStore │  │ Repository │   │
│  │   API    │  │  Local   │  │   Impl     │   │
│  └──────────┘  └──────────┘  └────────────┘   │
└─────────────────────────────────────────────────┘
```

## Layer Details

### 1. Presentation Layer (MVI Pattern)

**Location**: `presentation/`

#### Components:

- **Screen (Composable UI)**
  - Pure UI components built with Jetpack Compose
  - Observes ViewModel state and events
  - Sends intents to ViewModel on user interactions
  - No business logic

- **ViewModel (MVI Handler)**
  - Processes user intents
  - Manages UI state using StateFlow
  - Emits events using Channel
  - Communicates with domain layer through use cases

- **State (Output)**
  - Sealed classes representing UI states (Idle, Loading, Success, Error)
  - Immutable and reactive
  - Observed by screens for recomposition

- **Intent (Input)**
  - Sealed classes representing user actions
  - Sent from UI to ViewModel
  - Processed by ViewModel

- **Event (Side Effects)**
  - One-time events (navigation, toasts, etc.)
  - Emitted through Channel
  - Collected by UI layer

#### Example: LoginViewModel

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signinUseCase: SigninUseCase
) : ViewModel() {

    // State
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    // Events
    private val _event = Channel<LoginEvent>()
    val event = _event.receiveAsFlow()

    // Intent Handler
    fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.SignIn -> handleSignIn(intent.email, intent.password)
        }
    }

    private fun handleSignIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = LoginState.Loading
            val result = signinUseCase(email, password)
            result.onSuccess { user ->
                _state.value = LoginState.Success(user)
                _event.send(LoginEvent.NavigateToHome)
            }
            result.onFailure { error ->
                _state.value = LoginState.Error(error.message ?: "Unknown error")
            }
        }
    }
}
```

### 2. Domain Layer

**Location**: `domain/`

#### Components:

- **Models**
  - Core business entities
  - Platform-independent
  - Example: `User(id, email, name)`

- **Repositories (Interfaces)**
  - Contracts for data operations
  - Implemented by data layer
  - No implementation details

- **Use Cases**
  - Business logic operations
  - Single responsibility principle
  - Called by ViewModels
  - Example: SigninUseCase, SignupUseCase, LogoutUseCase

#### Example: SigninUseCase

```kotlin
class SigninUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Validation
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email"))
        }
        if (password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Password cannot be empty"))
        }
        // Call repository
        return authRepository.signin(email, password)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
```

### 3. Data Layer

**Location**: `data/`

#### Components:

- **Remote (Retrofit API)**
  - `ApiService`: Retrofit interface for API calls
  - DTOs: Data Transfer Objects from API
  - Example: `AuthRequest`, `AuthResponse`

- **Local (DataStore)**
  - `TokenManager`: Manages JWT token storage
  - Secure key-value storage
  - Used for token persistence

- **Repository Implementation**
  - Implements domain repository interface
  - Coordinates between remote and local data sources
  - Converts DTOs to domain models
  - Handles error mapping

#### Example: AuthRepositoryImpl

```kotlin
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun signin(
        email: String, 
        password: String
    ): Result<User> = try {
        // Call API
        val response = apiService.signin(AuthRequest(email, password))
        
        if (response.success && response.data != null && response.token != null) {
            // Save token locally
            tokenManager.saveToken(response.token)
            tokenManager.saveUserInfo(response.data.id, response.data.email)
            
            // Convert DTO to domain model and return
            Result.success(User(
                id = response.data.id,
                email = response.data.email,
                name = response.data.name ?: ""
            ))
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

## Data Flow

### Sign In Flow

```
1. User enters email/password
   ↓
2. LoginScreen sends LoginIntent.SignIn(email, password)
   ↓
3. LoginViewModel.processIntent() receives intent
   ↓
4. ViewModel calls signinUseCase(email, password)
   ↓
5. UseCase validates input
   ↓
6. UseCase calls authRepository.signin()
   ↓
7. Repository calls apiService.signin()
   ↓
8. Retrofit makes HTTP POST to /api/auth/signin
   ↓
9. Backend validates credentials
   ↓
10. Backend returns token and user data
   ↓
11. Repository saves token to DataStore
   ↓
12. Repository converts DTO to User model
   ↓
13. UseCase returns Result<User>
   ↓
14. ViewModel updates state to LoginState.Success
   ↓
15. ViewModel emits LoginEvent.NavigateToHome
   ↓
16. Screen observes state change and recomposes
   ↓
17. Screen collects event and navigates to home
```

## Dependency Injection

**Framework**: Hilt

**Modules**:

- **NetworkModule**: Provides Retrofit, OkHttp, API Service
- **RepositoryModule**: Binds repository implementations

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

## Benefits of This Architecture

✅ **Separation of Concerns**: Each layer has a specific responsibility  
✅ **Testability**: Easy to mock and test each layer independently  
✅ **Reusability**: Domain and data layers can be reused  
✅ **Maintainability**: Changes in one layer don't affect others  
✅ **Scalability**: Easy to add new features  
✅ **Type Safety**: Strongly typed state and intents  
✅ **Reactive**: Flow-based state management  

## Navigation

**Framework**: Jetpack Navigation

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
}

// Navigation flows:
// Login → Signup (Create Account)
// Login/Signup → Home (After authentication)
// Home → Login (After logout)
```
