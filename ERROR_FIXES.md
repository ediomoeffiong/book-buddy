# Error Fixes - Static Resources and Register Endpoint

## Issues Fixed

### 1. Static Resource Error (500)
**Problem:** Accessing `http://localhost:8080/` or static resources returned:
```json
{"status":500,"message":"No static resource .","timestamp":"..."}
```

**Root Cause:** 
- No controller was handling the root path (`/`)
- Spring was throwing exceptions for missing static resources that were being caught by the global exception handler
- Spring MVC wasn't configured to throw `NoHandlerFoundException` for 404s

**Fixes Applied:**
1. **Added RootController** - Handles the root path (`/`) and returns API information
2. **Added NoHandlerFoundException handler** - Properly handles 404 errors
3. **Updated application.yml** - Configured Spring MVC to throw exceptions for missing handlers
4. **Added static resource configuration** - Enabled static resource mappings

**File:** `src/main/java/com/bookbuddy/controller/RootController.java`
**File:** `src/main/resources/application.yml`

### 2. Register Endpoint Error (500 instead of 400)
**Problem:** Accessing `http://localhost:8080/api/auth/register` without a request body returned:
```json
{"status":500,"message":"Required request body is missing: ...","timestamp":"..."}
```

**Root Cause:**
- The endpoint requires a POST request with a JSON body
- When accessed via GET (like in a browser) or POST without body, Spring throws `HttpMessageNotReadableException`
- This exception was being caught by the global exception handler and returned as 500 instead of 400

**Fixes Applied:**
1. **Added HttpMessageNotReadableException handler** - Returns proper 400 Bad Request
2. **Added MissingServletRequestParameterException handler** - Handles missing parameters
3. **Added HttpRequestMethodNotSupportedException handler** - Handles wrong HTTP methods (405)

**File:** `src/main/java/com/bookbuddy/exception/GlobalExceptionHandler.java`

## How to Use

### Root Endpoint
- **GET** `http://localhost:8080/` - Returns API information
- Now returns: `{"message":"Book Buddy API","version":"1.0.0","status":"running",...}`

### Register Endpoint
- **POST** `http://localhost:8080/api/auth/register` - Requires JSON body
- **Correct Usage:**
  ```json
  {
    "fullName": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "confirmPassword": "SecurePass123"
  }
  ```
- **Error Response (400):** Now properly returns 400 Bad Request with clear message:
  ```json
  {
    "status": 400,
    "message": "Required request body is missing. Please provide a valid JSON body.",
    "timestamp": "..."
  }
  ```

### Static Resources
- Static resources in `src/main/resources/static/` are now properly handled
- Access via: `http://localhost:8080/static/favicon.ico` (if file exists)

## Error Codes Now Returned

- **400 Bad Request** - Missing/invalid request body, missing parameters
- **404 Not Found** - Endpoint not found
- **405 Method Not Allowed** - Wrong HTTP method (e.g., GET on POST-only endpoint)
- **500 Internal Server Error** - Only for unexpected errors

## Testing

1. **Test Root Endpoint:**
   ```bash
   curl http://localhost:8080/
   ```

2. **Test Register Endpoint (Correct):**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"fullName":"Test User","email":"test@example.com","password":"TestPass123","confirmPassword":"TestPass123"}'
   ```

3. **Test Register Endpoint (Wrong - Should return 400):**
   ```bash
   curl http://localhost:8080/api/auth/register
   # Returns 400 Bad Request with helpful message
   ```

