# Troubleshooting Guide

## 500 Internal Server Error

If you're getting a 500 error, the application will now show the actual error message in the response (after restarting with the latest fixes).

### Common Causes and Solutions

#### 1. JWT Secret Key Issues
**Symptom:** Error about JWT secret being too short or invalid

**Solution:** The JWT secret in `application.yml` should be at least 32 characters. The default secret is 52 characters, which is sufficient. If you're using a custom secret via environment variable, ensure it's at least 32 characters long.

#### 2. Database Connection Issues
**Symptom:** Errors related to H2 database or JPA

**Solution:** 
- Ensure H2 database is properly configured in `application.yml`
- Check that the database URL is correct: `jdbc:h2:mem:bookbuddydb`
- The application uses in-memory H2, so data is lost on restart

#### 3. Missing Dependencies
**Symptom:** ClassNotFoundException or NoClassDefFoundError

**Solution:**
- Run `mvn clean install` to ensure all dependencies are downloaded
- Check that Java version matches (Java 21 required)

#### 4. Port Already in Use
**Symptom:** Application won't start, port 8080 is busy

**Solution:**
- Stop any other applications using port 8080
- Or change the port in `application.yml`: `server.port: 8081`

### How to Get Detailed Error Information

1. **Check Application Logs**
   - The application logs errors to the console
   - Look for stack traces that show the root cause

2. **Check Error Response**
   - After the latest fix, the error response now includes the actual error message
   - The response will show: `{"status": 500, "message": "<actual error>", "timestamp": "..."}`

3. **Enable Debug Logging**
   - Debug logging is already enabled in `application.yml`
   - Check console output for detailed error information

### Restarting the Application

1. Stop the current application (Ctrl+C or kill the Java process)
2. Rebuild: `mvn clean package -DskipTests`
3. Start: `mvn spring-boot:run`

### Testing After Fixes

Try accessing: `http://localhost:8080/api/auth/register` with a POST request containing:
```json
{
  "fullName": "Test User",
  "email": "test@example.com",
  "password": "TestPass123",
  "confirmPassword": "TestPass123"
}
```

The error response should now show the actual error message instead of the generic "An unexpected error occurred".

