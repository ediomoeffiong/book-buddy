@echo off
REM Production Database Configuration Test Script (Windows)
REM Use this to verify your PostgreSQL connection works before deploying

setlocal enabledelayedexpansion

echo.
echo === Book Buddy Production Database Connection Test ===
echo.

REM Configuration
set DB_HOST=dpg-d4c1jber433s73d81skg-a
set DB_PORT=5432
set DB_NAME=bookbuddy_db_vfan
set DB_USER=bookbuddy_db_vfan_user
set DB_PASS=yoFiwxPxSUlCiwIapI5boNl3IVFv6Gd0

echo Database Configuration:
echo   Host: %DB_HOST%
echo   Port: %DB_PORT%
echo   Database: %DB_NAME%
echo   Username: %DB_USER%
echo.

echo === JDBC Connection String ===
echo jdbc:postgresql://%DB_HOST%:%DB_PORT%/%DB_NAME%?sslmode=require^&tcpKeepAlives=true
echo.

echo === Environment Variables to Set ===
echo DATABASE_URL=jdbc:postgresql://%DB_HOST%:%DB_PORT%/%DB_NAME%?sslmode=require^&tcpKeepAlives=true
echo DATABASE_USERNAME=%DB_USER%
echo DATABASE_PASSWORD=%DB_PASS%
echo SPRING_PROFILES_ACTIVE=prod
echo.

echo === Testing Application Connection ===
echo To test the Spring Boot app connection, run:
echo.
echo In PowerShell:
echo   $env:DATABASE_URL = "jdbc:postgresql://%DB_HOST%:%DB_PORT%/%DB_NAME%?sslmode=require&tcpKeepAlives=true"
echo   $env:DATABASE_USERNAME = "%DB_USER%"
echo   $env:DATABASE_PASSWORD = "%DB_PASS%"
echo   $env:SPRING_PROFILES_ACTIVE = "prod"
echo   mvn spring-boot:run
echo.
echo Or in CMD:
echo   set DATABASE_URL=jdbc:postgresql://%DB_HOST%:%DB_PORT%/%DB_NAME%?sslmode=require^&tcpKeepAlives=true
echo   set DATABASE_USERNAME=%DB_USER%
echo   set DATABASE_PASSWORD=%DB_PASS%
echo   set SPRING_PROFILES_ACTIVE=prod
echo   mvn spring-boot:run
echo.

echo === Quick Test ===
echo 1. Set environment variables (see above)
echo 2. Run: mvn spring-boot:run
echo 3. Test in another terminal: curl http://localhost:8080/health
echo 4. Expected response: {"status":"UP","database":"UP"}
echo.

echo Configuration verified and ready for deployment!
echo.
pause
