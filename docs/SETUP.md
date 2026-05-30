# Setup Guide

## Prerequisites

### Android Development
- Android Studio (Giraffe or newer)
- Kotlin 1.9+
- JDK 17+
- Android SDK 34+
- Emulator or Physical Device (Android 7.0+)

### Backend Development
- Node.js 18+
- npm or yarn
- MySQL 8.0+

## Backend Setup

### 1. Install Node.js Dependencies

```bash
cd backend
npm install
```

**Dependencies installed**:
- `express`: Web framework
- `mysql2`: MySQL driver
- `bcryptjs`: Password hashing
- `jsonwebtoken`: JWT generation and verification
- `dotenv`: Environment variables
- `cors`: Cross-Origin Resource Sharing
- `body-parser`: JSON parsing
- `nodemon`: Development auto-reload

### 2. Create MySQL Database

**Option 1: Command Line**
```bash
mysql -u root -p
```

```sql
CREATE DATABASE auth_db;
```

**Option 2: MySQL Workbench**
1. Open MySQL Workbench
2. Click `+` to create new connection
3. Create database `auth_db`

### 3. Configure Environment Variables

```bash
cd backend
cp .env.example .env
```

Edit `.env`:
```
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=your_mysql_password
DB_NAME=auth_db
JWT_SECRET=your_super_secret_jwt_key_here
PORT=3000
NODE_ENV=development
```

### 4. Run Database Migrations

This creates the `users` table:

```bash
npm run migrate
```

Output:
```
✓ Database created/already exists
✓ Users table created
```

### 5. Start Backend Server

**Development** (with auto-reload):
```bash
npm run dev
```

**Production**:
```bash
npm start
```

Output:
```
Server running on port 3000
Environment: development
```

Test health endpoint:
```bash
curl http://localhost:3000/health
# Response: {"status": "Server is running"}
```

## Android Setup

### 1. Clone Repository

```bash
git clone https://github.com/connectwitharunrk/android-auth-app.git
cd android-auth-app
```

### 2. Open in Android Studio

1. Open Android Studio
2. Click **File** → **Open**
3. Select the `android` directory
4. Wait for Gradle sync to complete

### 3. Configure API Endpoint

Edit `android/app/src/main/java/com/example/authapp/di/NetworkModule.kt`:

**For Emulator** (default):
```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/"
```

**For Physical Device**:
```kotlin
private const val BASE_URL = "http://YOUR_IP_ADDRESS:3000/"
```

Replace `YOUR_IP_ADDRESS` with your machine's local IP (e.g., `192.168.1.100`)

### 4. Build and Run

1. **Select Target Device**:
   - Emulator: Click **Virtual Device Manager** → Select device → **Play**
   - Physical Device: Connect device via USB and enable USB debugging

2. **Build APK**:
   ```bash
   cd android
   ./gradlew build
   ```

3. **Run App**:
   - Android Studio: Click **Run** button or press `Shift + F10`
   - Or use terminal: `./gradlew installDebug`

### 5. Verify Setup

1. App should open on Login screen
2. Try signing up with a test email: `test@example.com`
3. Password: `password123`
4. Should navigate to Home screen after successful signup

## Troubleshooting

### Backend Issues

**Error: "ECONNREFUSED: Connection refused"**
- Ensure MySQL is running
- Check connection details in `.env`
- Try: `mysql -u root -p -e "SELECT 1"`

**Error: "Error: listen EADDRINUSE: address already in use :::3000"**
- Port 3000 is already in use
- Kill process: `lsof -ti:3000 | xargs kill -9`
- Or change PORT in `.env`

**Error: "ER_ACCESS_DENIED_FOR_USER"**
- Wrong MySQL password in `.env`
- Reset MySQL password or update `.env`

### Android Issues

**Error: "Failed to connect to localhost:3000"**
- Backend not running
- Wrong API endpoint in NetworkModule
- Firewall blocking connection

**Error: "Gradle sync failed"**
- Click **File** → **Sync Now**
- Or invalidate cache: **File** → **Invalidate Caches** → **Invalidate and Restart**

**Error: "Build failed"**
- Clean build: `./gradlew clean build`
- Or in Android Studio: **Build** → **Clean Project** → **Rebuild Project**

**Emulator Issues**
- Cold boot: **AVD Manager** → Right-click device → **Cold Boot Now**
- Check network: `adb shell ping 10.0.2.2`

## Development Workflow

### Making API Changes

1. Update `ApiService.kt` (interfaces)
2. Update DTOs in `data/remote/dto/`
3. Update repository implementation
4. Backend will restart automatically with nodemon

### Adding New Screens

1. Create screen in `presentation/screens/`
2. Create ViewModel with MVI pattern
3. Add route to `NavGraph.kt`
4. Navigate from existing screen

### Testing Locally

**Backend API Testing**:
```bash
# Use Postman, Insomnia, or cURL
curl -X POST http://localhost:3000/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

**Android UI Testing**:
- Use Android emulator with debugger
- Set breakpoints in ViewModels
- Monitor Logcat for errors

## Useful Commands

### Backend
```bash
npm install          # Install dependencies
npm run migrate      # Run migrations
npm run dev          # Start development server
npm start            # Start production server
```

### Android
```bash
cd android
./gradlew build      # Build project
./gradlew clean      # Clean build
./gradlew installDebug  # Install on device
adb logcat           # View logs
```

## Next Steps

1. ✅ Backend setup and running
2. ✅ Android setup and running
3. Test signup flow
4. Test login flow
5. Test logout flow
6. Add more features (profile update, password reset, etc.)
7. Deploy to production
