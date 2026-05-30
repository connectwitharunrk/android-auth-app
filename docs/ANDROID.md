# Android App Documentation

## Overview

The Android app is built with Kotlin, Jetpack Compose, and follows Clean Architecture with MVI pattern.

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/authapp/
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   └── TokenManager.kt      # DataStore token storage
│   │   │   ├── remote/
│   │   │   │   ├── ApiService.kt        # Retrofit interface
│   │   │   │   └── dto/
│   │   │   │       └── AuthDto.kt       # Data transfer objects
│   │   │   └── repository/
│   │   │       └── AuthRepositoryImpl.kt # Repository implementation
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   └── User.kt              # Business model
│   │   │   ├── repository/
│   │   │   │   └── AuthRepository.kt    # Repository interface
│   │   │   └── usecase/
│   │   │       ├── SigninUseCase.kt
│   │   │       ├── SignupUseCase.kt
│   │   │       └── LogoutUseCase.kt
│   │   ├── presentation/
│   │   │   ├── MainActivity.kt
│   │   │   ├── navigation/
│   │   │   │   └── NavGraph.kt          # Navigation routes
│   │   │   ├── screens/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── login/
│   │   │   │   │   │   ├── LoginViewModel.kt
│   │   │   │   │   │   └── LoginScreen.kt
│   │   │   │   │   └── signup/
│   │   │   │   │       ├── SignupViewModel.kt
│   │   │   │   │       └── SignupScreen.kt
│   │   │   │   └── home/
│   │   │   │       ├── HomeViewModel.kt
│   │   │   │       └── HomeScreen.kt
│   │   │   ├── theme/
│   │   │   │   ├── Theme.kt
│   │   │   │   ├── Type.kt              # Typography
│   │   │   │   └── Color.kt             # Color palette
│   │   │   └── ...
│   │   ├── di/
│   │   │   ├── NetworkModule.kt         # Network DI
│   │   │   └── RepositoryModule.kt      # Repository DI
│   │   └── AuthApp.kt                   # Application class
│   └── AndroidManifest.xml
├── build.gradle.kts
└── ...
```

## Key Dependencies

### Build & Gradle
```gradle
plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt") // Kapt for annotation processing
}
```

### Core Android
```gradle
// Lifecycle & ViewModel
androidx.lifecycle:lifecycle-runtime-ktx
androidx.lifecycle:lifecycle-viewmodel-ktx
androidx.lifecycle:lifecycle-viewmodel-compose
```

### Jetpack Compose
```gradle
// UI Framework
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended
androidx.activity:activity-compose

// Navigation
androidx.navigation:navigation-compose
```

### Networking
```gradle
// Retrofit & OkHttp
squareup.retrofit2:retrofit
squareup.retrofit2:converter-gson
squareup.okhttp3:okhttp
squareup.okhttp3:logging-interceptor
```

### Async & Reactive
```gradle
// Coroutines
kotlinx.coroutines:kotlinx-coroutines-core
kotlinx.coroutines:kotlinx-coroutines-android

// DataStore (secure storage)
androidx.datastore:datastore-preferences
```

### Dependency Injection
```gradle
// Hilt
com.google.dagger:hilt-android
androidx.hilt:hilt-navigation-compose
```

## UI Components

### Login Screen

**Location**: `presentation/screens/auth/login/LoginScreen.kt`

```kotlin
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // State management
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    
    // Event handling (navigation)
    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                LoginEvent.NavigateToHome -> {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
        }
    }
    
    // UI Layout
    Column(/* ... */) {
        // Email input
        OutlinedTextField(/* ... */)
        
        // Password input
        OutlinedTextField(/* ... */)
        
        // Sign In button
        Button(/* ... */)
        
        // Error handling
        if (state is LoginState.Error) {
            Text((state as LoginState.Error).message)
        }
    }
}
```

**Features**:
- Text input fields with validation
- Loading indicator on button
- Error message display
- Navigation to signup
- Password visibility toggle

### Signup Screen

**Location**: `presentation/screens/auth/signup/SignupScreen.kt`

**Features**:
- Email input with validation
- Password input
- Confirm password input
- Password match validation
- Back to login link

### Home Screen

**Location**: `presentation/screens/home/HomeScreen.kt`

**Features**:
- Top app bar with logout button
- Welcome card with user info
- Logout confirmation dialog
- Success state display

## ViewModels

### LoginViewModel

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
    
    // Intent handler
    fun processIntent(intent: LoginIntent) { /* ... */ }
}
```

**MVI Components**:
- `LoginIntent`: User actions (SignIn)
- `LoginState`: UI states (Idle, Loading, Success, Error)
- `LoginEvent`: One-time events (Navigate)

### SignupViewModel

Similar structure to LoginViewModel with signup-specific logic.

### HomeViewModel

Handles logout flow and user data loading.

## Data Flow

### UI State Updates

```
Screen
   ↓ (user input)
ViewModel.processIntent()
   ↓
UseCase.invoke()
   ↓
Repository.operation()
   ↓
ApiService (Retrofit) / TokenManager (DataStore)
   ↓ (success/failure)
Repository returns Result<T>
   ↓
UseCase returns Result<T>
   ↓
ViewModel updates state
   ↓
Screen recomposes with new state
```

## Networking

### API Service

```kotlin
interface ApiService {
    @POST("/api/auth/signup")
    suspend fun signup(@Body request: AuthRequest): AuthResponse
    
    @POST("/api/auth/signin")
    suspend fun signin(@Body request: AuthRequest): AuthResponse
    
    @POST("/api/auth/logout")
    suspend fun logout(): AuthResponse
    
    @GET("/api/auth/me")
    suspend fun getCurrentUser(): AuthResponse
}
```

### Request/Response DTOs

```kotlin
data class AuthRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: UserData? = null,
    val token: String? = null
)
```

### Retrofit Configuration

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "http://10.0.2.2:3000/"
    
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

## Local Storage

### TokenManager

```kotlin
class TokenManager(context: Context) {
    private val dataStore = context.dataStore
    
    val tokenFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }
    
    suspend fun saveToken(token: String) { /* ... */ }
    suspend fun clearToken() { /* ... */ }
}
```

**Features**:
- Secure token storage with DataStore
- Reactive Flow for token updates
- Token cleanup on logout

## Navigation

### Routes

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")      // Entry point
    object Signup : Screen("signup")
    object Home : Screen("home")        // After auth
}
```

### Navigation Graph

```kotlin
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
    }
}
```

## Theme

### Colors

```kotlin
val PrimaryColor = Color(0xFF6200EE)      // Purple
val SecondaryColor = Color(0xFF03DAC6)    // Teal
val ErrorColor = Color(0xFFB00020)        // Red
```

### Typography

```kotlin
val Typography = Typography(
    headlineLarge = TextStyle(fontSize = 32.sp),
    bodyLarge = TextStyle(fontSize = 16.sp),
    // ...
)
```

## Dependency Injection

### @HiltViewModel

```kotlin
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signinUseCase: SigninUseCase
) : ViewModel() { }
```

### Repository Binding

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
}
```

## Permissions

**AndroidManifest.xml**:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Testing

### Unit Tests

```kotlin
@Test
fun `signup with valid email returns success`() {
    // Arrange
    // Act
    // Assert
}
```

### UI Tests

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

@Test
fun `login button enabled when fields not empty`() {
    composeTestRule.setContent {
        LoginScreen(/* ... */)
    }
    // Assert button state
}
```

## Build Variants

### Debug
- Logging enabled
- Debuggable APK
- Fast build

### Release
- Minified code
- R8/ProGuard enabled
- Optimized APK

## Performance

### Best Practices
- LazyColumn for lists
- Remember for state preservation
- StateFlow for efficient state emission
- Coroutines for non-blocking operations

## Debugging

### Logcat
```bash
adb logcat -s AuthApp
```

### Timber Logging
```kotlin
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}

Timber.d("Debug message")
Timber.e(exception, "Error message")
```

## Production Build

```bash
./gradlew assembleRelease
./gradlew bundleRelease  # For Play Store
```
