# Backend Documentation

## Overview

The backend is built with Node.js, Express.js, and MySQL. It handles user authentication with JWT tokens.

## Project Structure

```
backend/
├── src/
│   ├── index.js                    # Main server
│   ├── config/
│   │   └── database.js             # MySQL pool configuration
│   ├── routes/
│   │   └── authRoutes.js           # Auth endpoints
│   ├── controllers/
│   │   └── authController.js       # Request handlers
│   ├── middleware/
│   │   └── auth.js                 # JWT verification
│   └── migrations/
│       └── runMigrations.js        # Database setup
├── package.json                    # Dependencies
├── .env.example                    # Environment template
└── README.md
```

## Key Files

### 1. index.js - Main Server

```javascript
const app = express();
app.use(cors());
app.use(bodyParser.json());
app.use('/api/auth', authRoutes);
```

**Responsibilities**:
- Initialize Express app
- Configure middleware (CORS, body parsing)
- Mount routes
- Error handling
- Start server on PORT 3000

### 2. database.js - MySQL Connection Pool

```javascript
const pool = mysql.createPool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  connectionLimit: 10
});
```

**Features**:
- Connection pooling for efficiency
- Max 10 concurrent connections
- Automatic reconnection

### 3. authController.js - Authentication Logic

#### Signup Handler

```javascript
exports.signup = async (req, res, next) => {
  // 1. Validate email and password
  // 2. Check if user exists
  // 3. Hash password with bcrypt
  // 4. Insert user into database
  // 5. Generate JWT token
  // 6. Return user and token
}
```

**Flow**:
1. Request validation
2. Check duplicate email
3. Password hashing (bcryptjs, salt rounds: 10)
4. Database insert
5. JWT generation (7 days expiry)
6. Response

#### Signin Handler

```javascript
exports.signin = async (req, res, next) => {
  // 1. Validate email and password
  // 2. Find user by email
  // 3. Compare password hash
  // 4. Generate JWT token
  // 5. Return user and token
}
```

**Password Verification**:
- Uses bcrypt.compare() to safely verify
- Never returns password hash

#### Logout Handler

```javascript
exports.logout = async (req, res) => {
  // Simple acknowledgment
  // Token invalidation handled on client
}
```

#### Get Current User

```javascript
exports.getCurrentUser = async (req, res, next) => {
  // 1. Get userId from JWT (from middleware)
  // 2. Query user from database
  // 3. Return user data (no password)
}
```

### 4. auth.js - JWT Middleware

```javascript
exports.authenticate = (req, res, next) => {
  // 1. Extract token from Authorization header
  // 2. Verify token signature
  // 3. Attach userId to request
  // 4. Call next middleware
}
```

**Token Format**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Database Schema

### Users Table

```sql
CREATE TABLE users (
  id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(255),
  createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Columns**:
- `id`: UUID primary key
- `email`: Unique email address
- `password`: Bcrypt hashed password
- `name`: Optional user name
- `createdAt`: Account creation timestamp
- `updatedAt`: Last update timestamp

## Security Features

### 1. Password Hashing
- Algorithm: bcryptjs
- Salt rounds: 10
- One-way hashing (irreversible)

### 2. JWT Tokens
- Algorithm: HS256
- Expiry: 7 days
- Secret: From environment variable

### 3. Input Validation
```javascript
if (!email || !password) {
  return res.status(400).json({
    success: false,
    message: 'Email and password are required'
  });
}
```

### 4. CORS
- Allows requests from any origin
- Should be restricted in production

### 5. Error Handling
- Generic error messages (don't expose internals)
- Proper HTTP status codes
- No stack traces in responses

## Environment Variables

```env
DB_HOST=localhost          # MySQL host
DB_PORT=3306              # MySQL port
DB_USER=root              # MySQL user
DB_PASSWORD=password      # MySQL password
DB_NAME=auth_db           # Database name
JWT_SECRET=secret_key     # JWT signing secret
PORT=3000                 # Server port
NODE_ENV=development      # Environment
```

## Error Handling

### HTTP Status Codes
- `200`: Success (GET, logout)
- `201`: Created (signup)
- `400`: Bad request (validation)
- `401`: Unauthorized (invalid credentials/token)
- `409`: Conflict (user exists)
- `500`: Server error

### Error Response Format
```json
{
  "success": false,
  "message": "Error description"
}
```

## API Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { /* data object */ },
  "token": "jwt_token_here" // Only for auth endpoints
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description"
}
```

## Performance Considerations

### Database Connection Pool
- Maintains up to 10 connections
- Reuses connections for efficiency
- Prevents connection leaks

### Query Optimization
- Uses parameterized queries (prevents SQL injection)
- Indexes on email (UNIQUE)
- UUID for scalability

### Async Operations
- All database operations are async
- Uses await/async for clean code
- Prevents blocking

## Production Deployment

### Environment Setup
```bash
NODE_ENV=production
```

### Security Checklist
- [ ] Update JWT_SECRET to strong value
- [ ] Restrict CORS to specific domains
- [ ] Use HTTPS
- [ ] Set strong MySQL password
- [ ] Enable database backups
- [ ] Monitor error logs
- [ ] Set up rate limiting
- [ ] Use environment-specific configs

### Example Production .env
```env
DB_HOST=prod-mysql-server.com
DB_USER=prod_user
DB_PASSWORD=strong_password_here
DB_NAME=auth_db_prod
JWT_SECRET=very_long_random_secret_string
PORT=3000
NODE_ENV=production
```

## Monitoring & Logging

### Console Logs
- Server start/stop messages
- Database connection status
- Request handling

### Recommended Tools
- **Winston**: Structured logging
- **Morgan**: HTTP request logging
- **Sentry**: Error tracking
- **New Relic**: Performance monitoring

## Future Enhancements

1. Email verification
2. Password reset flow
3. OAuth integration (Google, GitHub)
4. Rate limiting
5. Request logging
6. API documentation (Swagger)
7. User roles and permissions
8. Database migrations (Knex.js, Sequelize)
9. Caching (Redis)
10. Test suite (Jest)
