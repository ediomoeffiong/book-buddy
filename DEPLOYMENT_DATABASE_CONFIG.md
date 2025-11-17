# Production Database Deployment Quick Start

## Your PostgreSQL Connection Details

```
JDBC URL: jdbc:postgresql://dpg-d4c1jber433s73d81skg-a:5432/bookbuddy_db_vfan?sslmode=require&tcpKeepAlives=true
Username: bookbuddy_db_vfan_user
Password: yoFiwxPxSUlCiwIapI5boNl3IVFv6Gd0
```

## Environment Variables (Set in Your Deployment Platform)

```bash
DATABASE_URL=jdbc:postgresql://dpg-d4c1jber433s73d81skg-a:5432/bookbuddy_db_vfan?sslmode=require&tcpKeepAlives=true
DATABASE_USERNAME=bookbuddy_db_vfan_user
DATABASE_PASSWORD=yoFiwxPxSUlCiwIapI5boNl3IVFv6Gd0
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=<generate_a_32_char_secret>
GOOGLE_BOOKS_API_KEY=<optional>
```

## What's Been Configured

✅ **Connection Pooling (HikariCP)**
- Max connections: 20
- Min idle: 5
- Connection timeout: 30s
- Idle timeout: 10 min
- Max lifetime: 15 min (compatible with cloud providers)

✅ **SSL/TLS Security**
- Enforced `sslmode=require`
- Prevents man-in-the-middle attacks
- Required for cloud PostgreSQL

✅ **Connection Reliability**
- TCP keep-alives enabled
- Automatic reconnection
- Connection validation: `SELECT 1`
- Handles cloud provider timeout limits

✅ **Auto Schema Management**
- Tables auto-created on startup
- Safe for production (`ddl-auto: update`)
- No data loss on updates

✅ **Performance & Monitoring**
- Query logging disabled in production
- Prometheus metrics exposed
- HikariCP pool metrics available

## Deployment Steps

### 1. Set Environment Variables
In your deployment platform (Render, Railway, etc.):
```
DATABASE_URL = jdbc:postgresql://dpg-d4c1jber433s73d81skg-a:5432/bookbuddy_db_vfan?sslmode=require&tcpKeepAlives=true
DATABASE_USERNAME = bookbuddy_db_vfan_user
DATABASE_PASSWORD = yoFiwxPxSUlCiwIapI5boNl3IVFv6Gd0
SPRING_PROFILES_ACTIVE = prod
JWT_SECRET = (generate 32+ character secret)
```

### 2. Deploy Application
```bash
git push origin main
# Your CI/CD will trigger
```

### 3. Verify Health
```bash
curl https://your-app.com/health
# Expected: {"status":"UP","database":"UP"}
```

### 4. Test API
```bash
curl -X POST https://your-app.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "password": "TestPassword123",
    "confirmPassword": "TestPassword123"
  }'
```

## Local Testing (Before Deployment)

### Windows PowerShell
```powershell
$env:DATABASE_URL = "jdbc:postgresql://dpg-d4c1jber433s73d81skg-a:5432/bookbuddy_db_vfan?sslmode=require&tcpKeepAlives=true"
$env:DATABASE_USERNAME = "bookbuddy_db_vfan_user"
$env:DATABASE_PASSWORD = "yoFiwxPxSUlCiwIapI5boNl3IVFv6Gd0"
$env:SPRING_PROFILES_ACTIVE = "prod"
mvn spring-boot:run
```

### Linux/Mac Bash
```bash
export DATABASE_URL="jdbc:postgresql://dpg-d4c1jber433s73d81skg-a:5432/bookbuddy_db_vfan?sslmode=require&tcpKeepAlives=true"
export DATABASE_USERNAME="bookbuddy_db_vfan_user"
export DATABASE_PASSWORD="yoFiwxPxSUlCiwIapI5boNl3IVFv6Gd0"
export SPRING_PROFILES_ACTIVE="prod"
mvn spring-boot:run
```

Then test:
```bash
curl http://localhost:8080/health
# Should return: {"status":"UP","database":"UP"}
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| **Connection timeout** | Verify all env vars are set correctly. Test with `psql` command. |
| **SSL error** | `sslmode=require` is in the URL. Cloud DB supports SSL. |
| **"Server closed connection"** | This is normal for cloud DBs. Handled by connection pooling & keep-alives. |
| **Connection pool exhausted** | App leaking connections. Check @Transactional annotations. |
| **Slow queries** | Enable query logging or check PostgreSQL CPU usage. |

## Additional Resources

- Full configuration guide: `PRODUCTION_DATABASE_SETUP.md`
- API endpoints: `API_ENDPOINTS.md`
- Health check details: `HEALTH_ENDPOINT_VERIFICATION.md`
- Test scripts: `test-db-connection.sh` (Linux) or `test-db-connection.bat` (Windows)

---

**Status**: ✅ Ready for Production Deployment  
**Configuration Version**: 1.0  
**Last Updated**: 2025-11-17
