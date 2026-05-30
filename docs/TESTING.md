# Testing Guide

## Overview

This guide covers testing strategies for both backend and Android app.

## Backend Testing

### Unit Tests

**Framework**: Jest (recommended for Node.js)

**Installation**:
```bash
cd backend
npm install --save-dev jest
```

**Example**: Testing authentication controller

```javascript
// tests/authController.test.js
const authController = require('../src/controllers/authController');
const bcrypt = require('bcryptjs');

jest.mock('bcryptjs');

describe('Auth Controller', () => {
  describe('signup', () => {
    it('should create a new user with hashed password', async () => {
      // Mock request/response
      const req = {
        body: {
          email: 'test@example.com',
          password: 'password123'
        }
      };
      
      const res = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn()
      };
      
      // Mock bcrypt.hash
      bcrypt.hash.mockResolvedValue('hashed_password');
      
      // Call function
      await authController.signup(req, res);
      
      // Assertions
      expect(res.status).toHaveBeenCalledWith(201);
      expect(bcrypt.hash).toHaveBeenCalled();
    });
  });
});
```

**Run Tests**:
```bash
npm test
```

### Integration Tests

**Framework**: Supertest (for API testing)

```javascript
// tests/auth.integration.test.js
const request = require('supertest');
const app = require('../src/index');

describe('Auth Endpoints', () => {
  it('POST /api/auth/signup should create user', async () => {
    const res = await request(app)
      .post('/api/auth/signup')
      .send({
        email: 'newuser@example.com',
        password: 'password123'
      });
    
    expect(res.statusCode).toBe(201);
    expect(res.body.success).toBe(true);
    expect(res.body.token).toBeDefined();
  });
  
  it('POST /api/auth/signin should return token', async () => {
    const res = await request(app)
      .post('/api/auth/signin')
      .send({
        email: 'newuser@example.com',
        password: 'password123'
      });
    
    expect(res.statusCode).toBe(200);
    expect(res.body.token).toBeDefined();
  });
});
```

### Manual API Testing

**Using cURL**:

```bash
# Signup
curl -X POST http://localhost:3000/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Signin
curl -X POST http://localhost:3000/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Get user (replace TOKEN)
curl -X GET http://localhost:3000/api/auth/me \
  -H "Authorization: Bearer TOKEN"
```

**Using Postman**:

1. Create new collection "Auth App"
2. Add requests:
   - POST /api/auth/signup
   - POST /api/auth/signin
   - GET /api/auth/me
3. Use environment variables for token
4. Test flows

**Using Insomnia**:

1. Import API to Insomnia
2. Create workspace
3. Test endpoints with variables

## Android Testing

### Unit Tests

**Framework**: JUnit + Mockito

**Example**: Testing LoginViewModel

```kotlin
// LoginViewModelTest.kt
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var signinUseCase: SigninUseCase
    
    private lateinit var viewModel: LoginViewModel
    
    @Before
    fun setUp() {
        viewModel = LoginViewModel(signinUseCase)
    }
    
    @Test
    fun `signin with valid credentials returns success`() = runBlocking {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val mockUser = User("1", email, "Test User")
        
        whenever(signinUseCase(email, password))
            .thenReturn(Result.success(mockUser))
        
        // Act
        viewModel.processIntent(LoginIntent.SignIn(email, password))
        
        // Assert
        val state = viewModel.state.value
        assert(state is LoginState.Success)
        assert((state as LoginState.Success).user.email == email)
    }
    
    @Test
    fun `signin with invalid email returns error`() = runBlocking {
        // Arrange
        val email = "invalid-email"
        val password = "password123"
        
        whenever(signinUseCase(email, password))
            .thenReturn(Result.failure(IllegalArgumentException("Invalid email")))
        
        // Act
        viewModel.processIntent(LoginIntent.SignIn(email, password))
        
        // Assert
        val state = viewModel.state.value
        assert(state is LoginState.Error)
    }
}
```

**Run Tests**:
```bash
cd android
./gradlew test
```

### UI Tests (Compose)

**Framework**: Jetpack Compose Test

```kotlin
// LoginScreenTest.kt
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `login button is disabled when fields are empty`() {
        composeTestRule.setContent {
            AuthAppTheme {
                LoginScreen(
                    navController = rememberNavController(),
                    viewModel = hiltViewModel()
                )
            }
        }
        
        composeTestRule
            .onNodeWithText("Sign In")
            .assertIsNotEnabled()
    }
    
    @Test
    fun `login button is enabled when fields are filled`() {
        composeTestRule.setContent {
            AuthAppTheme {
                LoginScreen(navController = rememberNavController())
            }
        }
        
        // Enter email
        composeTestRule
            .onNodeWithTag("emailField")
            .performTextInput("test@example.com")
        
        // Enter password
        composeTestRule
            .onNodeWithTag("passwordField")
            .performTextInput("password123")
        
        // Assert button is enabled
        composeTestRule
            .onNodeWithText("Sign In")
            .assertIsEnabled()
    }
    
    @Test
    fun `navigation to signup works`() {
        composeTestRule.setContent {
            AuthAppTheme {
                LoginScreen(navController = rememberNavController())
            }
        }
        
        composeTestRule
            .onNodeWithText("Create Account")
            .performClick()
    }
}
```

**Run Tests**:
```bash
cd android
./gradlew connectedAndroidTest
```

### Integration Tests

**Testing authentication flow**:

```kotlin
// AuthFlowTest.kt
@RunWith(AndroidJUnit4::class)
class AuthFlowTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `complete signup and login flow`() {
        // Start app
        composeTestRule.setContent {
            AuthAppTheme {
                AppNavGraph()
            }
        }
        
        // Navigate to signup
        composeTestRule
            .onNodeWithText("Create Account")
            .performClick()
        
        // Fill signup form
        composeTestRule
            .onNodeWithText("Email")
            .performTextInput("newuser@example.com")
        
        composeTestRule
            .onNodeWithText("Password")
            .performTextInput("password123")
        
        composeTestRule
            .onNodeWithText("Confirm Password")
            .performTextInput("password123")
        
        // Submit
        composeTestRule
            .onNodeWithText("Create Account")
            .performClick()
        
        // Wait for home screen
        composeTestRule
            .onNodeWithText("Welcome")
            .assertIsDisplayed()
    }
}
```

### Manual UI Testing Checklist

- [ ] Signup with valid email
- [ ] Signup with invalid email (error)
- [ ] Signup with weak password (error)
- [ ] Signup with mismatched passwords (error)
- [ ] Login with valid credentials
- [ ] Login with wrong password (error)
- [ ] Login with non-existent email (error)
- [ ] Logout
- [ ] Error message display
- [ ] Loading indicator display
- [ ] Navigation between screens
- [ ] Keyboard handling
- [ ] Device rotation

## Test Coverage

### Backend Target
- Controllers: 80%+
- Services: 90%+
- Overall: 75%+

**Generate Coverage Report**:
```bash
cd backend
npm test -- --coverage
```

### Android Target
- ViewModels: 85%+
- Repository: 80%+
- UseCase: 90%+
- Overall: 70%+

**Generate Coverage Report**:
```bash
cd android
./gradlew jacocoTestReport
```

## Continuous Integration

### GitHub Actions

```yaml
# .github/workflows/test.yml
name: Tests
on: [push, pull_request]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-node@v2
      - run: cd backend && npm install && npm test
  
  android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-java@v2
      - run: cd android && ./gradlew test
```

## Debugging Tests

### Backend
```bash
node --inspect-brk ./node_modules/jest/bin/jest.js
```

### Android
- Use Android Studio debugger
- Set breakpoints in test code
- Run single test: `./gradlew testDebugUnitTest --tests LoginViewModelTest.signup*`

## Best Practices

1. **Arrange-Act-Assert**: Clear test structure
2. **Descriptive Names**: Test names describe behavior
3. **Isolation**: Tests should be independent
4. **Mock External**: Mock API calls and database
5. **No Side Effects**: Tests shouldn't affect each other
6. **Fast**: Unit tests should run quickly
7. **Maintainable**: Update tests with code changes
