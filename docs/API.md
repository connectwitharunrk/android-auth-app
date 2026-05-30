# API Documentation

## Base URL

```
http://localhost:3000
```

For production, update the endpoint in `NetworkModule.kt`

## Authentication

Authentication uses JWT (JSON Web Token). After login/signup, the server returns a token that must be sent in the `Authorization` header:

```
Authorization: Bearer <token>
```

## Endpoints

### 1. Sign Up

**Endpoint**: `POST /api/auth/signup`

**Request**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (Success - 201):
```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** (Error - 400/409):
```json
{
  "success": false,
  "message": "User already exists" // or "Email and password are required"
}
```

**Status Codes**:
- `201`: User created successfully
- `400`: Missing email or password
- `409`: User already exists
- `500`: Server error

---

### 2. Sign In

**Endpoint**: `POST /api/auth/signin`

**Request**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response** (Success - 200):
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "name": "John Doe"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response** (Error - 401):
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

**Status Codes**:
- `200`: Login successful
- `400`: Missing email or password
- `401`: Invalid credentials
- `500`: Server error

---

### 3. Logout

**Endpoint**: `POST /api/auth/logout`

**Headers**:
```
Authorization: Bearer <token>
```

**Response** (Success - 200):
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

**Status Codes**:
- `200`: Logout successful
- `401`: No token provided / Invalid token
- `500`: Server error

---

### 4. Get Current User

**Endpoint**: `GET /api/auth/me`

**Headers**:
```
Authorization: Bearer <token>
```

**Response** (Success - 200):
```json
{
  "success": true,
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "name": "John Doe"
  }
}
```

**Response** (Error - 404):
```json
{
  "success": false,
  "message": "User not found"
}
```

**Status Codes**:
- `200`: User found
- `401`: No token provided / Invalid token
- `404`: User not found
- `500`: Server error

---

## Error Handling

### Common Error Responses

**Invalid Token** (401):
```json
{
  "success": false,
  "message": "Invalid token"
}
```

**No Token Provided** (401):
```json
{
  "success": false,
  "message": "No token provided"
}
```

**Server Error** (500):
```json
{
  "success": false,
  "message": "Internal server error"
}
```

## Token Structure

JWT tokens contain the following payload:

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "iat": 1234567890,
  "exp": 1234654290
}
```

**Token Expiry**: 7 days

## Testing Endpoints

### Using cURL

**Sign Up**:
```bash
curl -X POST http://localhost:3000/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

**Sign In**:
```bash
curl -X POST http://localhost:3000/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

**Get Current User**:
```bash
curl -X GET http://localhost:3000/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Logout**:
```bash
curl -X POST http://localhost:3000/api/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Rate Limiting

Currently, no rate limiting is implemented. For production, consider adding rate limiting middleware.

## CORS

CORS is enabled for all origins by default. For production, restrict to specific domains in `index.js`:

```javascript
app.use(cors({
  origin: 'https://yourdomain.com',
  credentials: true
}));
```
