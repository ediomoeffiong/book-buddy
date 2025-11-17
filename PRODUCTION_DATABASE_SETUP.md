# Production Database Configuration Guide

## PostgreSQL Connection Details

Your production database is hosted with the following details:

```
Host: dpg-d4c1jber433s73d81skg-a
Port: 5432
Database: bookbuddy_db_vfan
Username: bookbuddy_db_vfan_user
Password: yoFiwxPxSUlCiwIapI5boNl3IVFv6Gd0
```

## JDBC Connection String

```
jdbc:postgresql://dpg-d4c1jber433s73d81skg-a:5432/bookbuddy_db_vfan?sslmode=require&tcpKeepAlives=true
```

## Environment Variables Setup

Set these environment variables in your deployment platform (Render, Railway, Heroku, etc.):

```bash
DATABASE_URL=jdbc:postgresql://dpg-d4c1jber433s73d81skg-a:5432/bookbuddy_db_vfan?sslmode=require&tcpKeepAlives=true
DATABASE_USERNAME=bookbuddy_db_vfan_user
DATABASE_PASSWORD=yoFiwxPxSUlCiwIapI5boNl3IVFv6Gd0
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=<your_generated_secret>
GOOGLE_BOOKS_API_KEY=<your_api_key>
```

## Connection Pool Configuration (Automatic)

The application uses HikariCP for connection pooling with the following settings:

| Setting | Value | Purpose |
|---------|-------|---------|
| **Max Pool Size** | 20 | Maximum concurrent connections |
| **Min Idle** | 5 | Minimum idle connections to maintain |
| **Connection Timeout** | 30s | Wait time for available connection |
| **Idle Timeout** | 10 min | Auto-close unused connections |
| **Max Lifetime** | 15 min | Max connection age (PostgreSQL Cloud limit) |
| **Connection Test** | `SELECT 1` | Validates connection health |

## SSL Configuration

The connection string includes `?sslmode=require` which:
- ✓ Enforces SSL/TLS encryption
- ✓ Prevents man-in-the-middle attacks
- ✓ Required for cloud PostgreSQL hosts
- ✓ Adds ~5-10ms latency (acceptable for production)

## TCP Keep-Alives

The `?tcpKeepAlives=true` parameter:
- ✓ Prevents idle connection timeout (especially cloud proxy timeouts)
- ✓ Detects and closes stale connections
- ✓ Maintains persistent long-lived connections

## Database Features Enabled

### Automatic Schema Management
- `ddl-auto: update` - Automatically creates/updates tables
- Schema migrations happen on app startup
- Safe for production (only adds new columns, doesn't drop data)

### Query Performance
- `show-sql: false` - Disables SQL logging in production (performance)
- `open-in-view: false` - Prevents N+1 query problems

### Monitoring & Logging
- Hibernate logs at INFO level for debugging
- Connection pool logs all state changes
- Spring Security logs WARN level for suspicious auth attempts

## Testing the Connection

### 1. Check if App Starts Successfully
```bash
# Watch for startup logs
# Should see: "Initialized JPA EntityManagerFactory"
# Should NOT see connection timeout errors
```

### 2. Query Health Endpoint
```bash
curl http://localhost:8080/health
# Response: {"status":"UP","database":"UP"}
```

### 3. Create a Test User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "password": "TestPassword123",
    "confirmPassword": "TestPassword123"
  }'
```

### 4. Query the Database Directly (Optional)
```bash
psql -h dpg-d4c1jber433s73d81skg-a -U bookbuddy_db_vfan_user -d bookbuddy_db_vfan -c "SELECT COUNT(*) FROM users;"
```

## Common Issues & Solutions

### Issue: "Connection timeout" or "Could not connect"
**Solutions:**
1. Verify DATABASE_URL is set correctly
2. Check firewall/network allows outbound connections to port 5432
3. Verify password is correct (special characters need escaping)
4. Test with `psql` command above

### Issue: "SSL connection error"
**Solutions:**
1. Ensure `sslmode=require` is in the connection string
2. Verify PostgreSQL server supports SSL (it should)
3. Try `sslmode=require&sslcertmode=disable` if certificate issues persist

### Issue: "Server closed the connection unexpectedly"
**Solutions:**
1. Cloud providers often close idle connections after 15 minutes
2. Already fixed with `tcpKeepAlives=true` and `max-lifetime: 900000`
3. HikariCP will automatically reconnect

### Issue: Connection Pool Exhaustion
**Symptoms:** "Unable to acquire JDBC Connection" errors
**Solutions:**
1. Verify connection timeout is set (30000ms default)
2. Check application isn't leaking connections (ensure @Transactional closed properly)
3. Monitor active connections in HikariCP logs
4. Increase max-pool-size if legitimate high load

## Deployment Checklist

- [ ] Set `DATABASE_URL` environment variable
- [ ] Set `DATABASE_USERNAME` environment variable
- [ ] Set `DATABASE_PASSWORD` environment variable
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Generate and set `JWT_SECRET` (32+ characters)
- [ ] Test connection with `/health` endpoint
- [ ] Verify tables are created: `SELECT table_name FROM information_schema.tables WHERE table_schema='public';`
- [ ] Test authentication flow (register/login)
- [ ] Verify data persists after restart

## Performance Tuning

### If experiencing slow queries:
1. Enable query logging temporarily:
   ```yaml
   logging:
     level:
       org.hibernate.SQL: DEBUG
       org.hibernate.type.descriptor.sql.BasicBinder: TRACE
   ```

2. Check connection pool utilization:
   ```bash
   # Monitor HikariPool metrics
   curl http://localhost:8080/actuator/metrics/hikaricp.connections
   ```

### If experiencing timeouts:
1. Consider increasing `connection-timeout` (30000ms)
2. Increase `maximum-pool-size` from 20 to 30
3. Check PostgreSQL CPU/Memory on hosting platform

## Monitoring

The application exposes metrics via Prometheus at `/actuator/metrics`:

```bash
# Database connection metrics
curl http://localhost:8080/actuator/metrics/hikaricp.connections

# Number of active connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active

# Number of pending requests
curl http://localhost:8080/actuator/metrics/hikaricp.connections.pending
```

## Additional Resources

- [PostgreSQL JDBC Documentation](https://jdbc.postgresql.org/documentation/head/connect.html)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)
- [Spring Data JPA Best Practices](https://spring.io/projects/spring-data-jpa)

---

**Last Updated:** 2025-11-17  
**Configuration Version:** 1.0  
**Production Ready:** ✓
