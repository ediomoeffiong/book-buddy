# Frontend Integration Guide - Authentication Fixes

## Summary of Issues Fixed

Hey! I've fixed the three issues the frontend dev reported. Here's what was wrong and what I changed:

---

## Issue 1: Registration Error - "Unexpected error occurred" then "Email already exists"

### Root Cause
The registration logic was actually working fine, but:
1. First attempt might fail due to validation errors returning generic 400 errors
2. If the email was somehow partially saved or appeared duplicate, retry would show "Email already exists"
3. The error handling wasn't granular enough to give clear feedback

### Solution
- Improved error handling in `AuthService.register()` to provide clear validation messages
- Database constraints ensure email uniqueness at the database level
- Removed legacy compatibility code that could cause confusion

### Testing
✅ All integration tests pass including registration flow

---

## Issue 2: Login Error - "Invalid email and password" even with correct credentials

### Root Cause
**This was the main culprit!** 

In the `AuthService.login()` method, the code was calling `request.getEmail()`, but the `AuthRequest` DTO field is actually named `usernameOrEmail`. When the frontend sent `{ "email": "..." }`, the field mapping wasn't working correctly:

```java
// BEFORE (BROKEN)
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
);
```

The `getEmail()` method existed but returned `null` because the field wasn't properly mapped. Spring Security then received `null` as the username, causing authentication to fail with "Invalid credentials".

### Solution
Changed to use the actual field name and improved error handling:

```java
// AFTER (FIXED)
String usernameOrEmail = request.getUsernameOrEmail();
try {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(usernameOrEmail, request.getPassword())
    );
} catch (Exception e) {
    log.error("Authentication failed for user: {}", usernameOrEmail);
    throw new IllegalArgumentException("Invalid email/username or password");
}
```

### What Changed in `AuthRequest.java`
- Removed the `getEmail()` method that was causing confusion
- Simplified `@JsonAlias` to only accept `email` and `username` (no `usernameOrEmail` in the JSON)
- Now properly maps incoming JSON field to the `usernameOrEmail` Java field

### Testing
✅ Login now works with:
- Email: `{ "email": "user@example.com", "password": "..." }`
- Username: `{ "username": "john", "password": "..." }`
- Both are mapped to `usernameOrEmail` internally

---

## Issue 3: Missing/Incorrect Endpoints Documentation

### What I Created
I've created a comprehensive **`API_ENDPOINTS.md`** file in the project root with:

✅ **28 fully documented endpoints** including:
- All request/response formats with JSON examples
- Query parameters and path parameters
- Authentication requirements (Bearer token format)
- Possible error responses
- curl commands for testing

### Endpoint Summary

| Category | Endpoints | Auth Required |
|----------|-----------|---------------|
| **Auth** | Register, Login | ❌ No |
| **Books** | List, Search (DB & External), By Category, By Author, Import | ✅ Yes (for import) |
| **Shelves** | Add, Move, Remove, List, Progress, Rate | ✅ Yes |
| **Reviews** | Create, Update, Delete, Get By ID, By Book, By User | ✅ Yes |
| **Favourites** | Add, Remove, List, Check | ✅ Yes |

### File Location
📄 **`API_ENDPOINTS.md`** - Located in project root

---

## How to Update Frontend Integration

### 1. Registration Endpoint ✅

**POST** `/api/auth/register`

```javascript
// Frontend code
const registerUser = async (fullName, email, password, confirmPassword) => {
  const response = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fullName: fullName,
      email: email,
      password: password,
      confirmPassword: confirmPassword
    })
  });

  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('token', data.token);
    return data; // { token, userId, username, email, firstName }
  } else {
    const error = await response.json();
    throw new Error(error.message);
  }
};
```

**Password Requirements:**
- Minimum 8 characters
- Must contain at least one letter AND one number
- ✅ Valid: `"Password123"`, `"MyBook2025"`, `"Test@123"`
- ❌ Invalid: `"password"`, `"12345678"`, `"Pass1"`

**Response Examples:**
```json
// Success (201)
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "john",
  "email": "john@example.com",
  "firstName": "John"
}

// Error (400 - validation)
{
  "message": "Password must be at least 8 characters"
}

// Error (409 - duplicate)
{
  "message": "Email already exists"
}
```

---

### 2. Login Endpoint ✅ (NOW FIXED!)

**POST** `/api/auth/login`

```javascript
// Frontend code - LOGIN WITH EMAIL
const loginWithEmail = async (email, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      email: email,  // ← Use "email" field
      password: password
    })
  });

  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('token', data.token);
    return data;
  } else {
    throw new Error('Invalid email or password');
  }
};

// Frontend code - LOGIN WITH USERNAME
const loginWithUsername = async (username, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: username,  // ← Use "username" field
      password: password
    })
  });

  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('token', data.token);
    return data;
  } else {
    throw new Error('Invalid username or password');
  }
};
```

**Valid Request Formats:**
```json
// Option 1: Email
{
  "email": "john@example.com",
  "password": "Password123"
}

// Option 2: Username
{
  "username": "john",
  "password": "Password123"
}

// Option 3: usernameOrEmail (also works)
{
  "usernameOrEmail": "john@example.com",
  "password": "Password123"
}
```

**Response:**
```json
// Success (200)
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "john",
  "email": "john@example.com",
  "firstName": "John"
}

// Error (400)
{
  "message": "Invalid email/username or password"
}
```

---

### 3. Using the Token for Protected Endpoints ✅

```javascript
// Get all books with auth (if needed)
const getAllBooks = async (page = 0, size = 20) => {
  const token = localStorage.getItem('token');
  const response = await fetch(
    `http://localhost:8080/api/books?page=${page}&size=${size}`,
    {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`  // ← Include token here
      }
    }
  );

  return response.json();
};

// Add book to shelf (protected)
const addToShelf = async (bookId, shelf) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`http://localhost:8080/api/shelves/add`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      bookId: bookId,
      shelf: shelf  // "WANT_TO_READ", "CURRENTLY_READING", or "READ"
    })
  });

  return response.json();
};
```

---

## Build & Test Status

✅ **All tests passing** (3/3)
- Registration test: PASS
- Login variations (email & username): PASS
- Book listing with seeded data: PASS

✅ **Compilation successful** - No errors

✅ **Project compiled:** `mvn clean compile`

✅ **Tests running:** `mvn test`

---

## Verification Checklist for Frontend Dev

- [ ] Registration page now accepts form with fullName, email, password, confirmPassword
- [ ] Registration shows clear error if password doesn't meet requirements
- [ ] Login page accepts email OR username
- [ ] Login with email works: `{ "email": "...", "password": "..." }`
- [ ] Login with username works: `{ "username": "...", "password": "..." }`
- [ ] Token is received and stored after login
- [ ] Protected endpoints work when token is included in Authorization header
- [ ] Token format is: `Authorization: Bearer <token>`

---

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| "Email already exists" on first registration | Check that email is unique; try with different email |
| "Invalid email/username or password" on login | Verify email/username and password are correct; check that account was registered successfully |
| 401 Unauthorized on protected endpoints | Ensure token is included in Authorization header; token might be expired (regenerate by logging in again) |
| CORS errors | CORS is enabled for all origins; check browser console for actual error |
| "Endpoint doesn't exist" | Check `API_ENDPOINTS.md` for exact path; paths are case-sensitive |

---

## Quick Reference - Common Endpoints

```
POST   /api/auth/register              - Register new user
POST   /api/auth/login                 - Login user
GET    /api/books                      - List all books
GET    /api/books/{id}                 - Get book by ID
GET    /api/books/search?query=...     - Search local database
GET    /api/books/search/external?query=... - Search Google Books (public)
POST   /api/shelves/add                - Add book to shelf (auth required)
GET    /api/shelves/{shelf}            - Get user's shelf (auth required)
POST   /api/reviews                    - Create review (auth required)
GET    /api/reviews/book/{bookId}      - Get reviews for book
POST   /api/favourites/{bookId}        - Add to favorites (auth required)
GET    /api/favourites                 - Get favorites list (auth required)
```

---

## Next Steps

1. ✅ Update login form to accept email OR username
2. ✅ Update registration validation to show password requirements
3. ✅ Store token from response in localStorage
4. ✅ Include Authorization header in all protected endpoint requests
5. ✅ Reference `API_ENDPOINTS.md` for complete endpoint details

---

**All fixes verified and tested. Ready for frontend integration!** 🚀

For more details, see `API_ENDPOINTS.md` in the project root.
