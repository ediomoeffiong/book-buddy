# Book Buddy API Examples

This document provides example requests and responses for testing the Book Buddy API.

## Base URL
```
http://localhost:8080
```

## 1. Authentication Flow

### 1.1 Register a New User

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "password": "Password123",
    "confirmPassword": "Password123"
  }'
```

**Validation Rules:**
- Full name: Required, 2-100 characters
- Email: Required, valid format, must be unique
- Password: Required, minimum 8 characters, must contain at least one letter and one number
- Confirm Password: Required, must match password

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "john",
  "email": "john@example.com",
  "firstName": "John"
}
```

**Note:** Username is automatically generated from email (part before @)

### 1.2 Login

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "john",
  "email": "john@example.com",
  "firstName": "John"
}
```

**Maintenance Mode Response (HTTP 503):**
```json
{
  "status": 503,
  "message": "The system is currently under maintenance. Please try again later.",
  "timestamp": "2025-11-01T10:30:00"
}
```

**Note:** Save the token for subsequent requests! Use firstName for user greetings.

### 1.3 Get Current User Information

**Request:**
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
{
  "id": 1,
  "username": "john",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER",
  "bio": null,
  "profileImageUrl": null,
  "createdAt": "2025-11-16T10:30:00"
}
```

**Note:** This endpoint is useful for verifying the current user after login/registration and displaying user info in the frontend.

---

## 2. Book Operations

### 2.1 Search Books from Google Books API (No Auth Required)

**Request:**
```bash
curl -X GET "http://localhost:8080/api/books/search/external?query=harry+potter&maxResults=10"
```

**Response:**
```json
[
  {
    "id": null,
    "title": "Harry Potter and the Philosopher's Stone",
    "author": "J.K. Rowling",
    "isbn": "9780439708180",
    "description": "Harry Potter has never been...",
    "publisher": "Scholastic Inc.",
    "publishedDate": "2015-12-08",
    "pageCount": 309,
    "coverImageUrl": "http://books.google.com/books/content?id=...",
    "categories": ["Juvenile Fiction"],
    "language": "en",
    "googleBooksId": "wrOQLV6xB-wC",
    "openLibraryId": null,
    "averageRating": 4.5,
    "ratingsCount": 100
  }
]
```

### 2.2 Import Book from Google Books

**Request:**
```bash
curl -X POST http://localhost:8080/api/books/import/wrOQLV6xB-wC \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
{
  "id": 1,
  "title": "Harry Potter and the Philosopher's Stone",
  "author": "J.K. Rowling",
  ...
}
```

### 2.3 Get Book Details

**Request:**
```bash
curl -X GET http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 2.4 Search Books in Database

**Request:**
```bash
curl -X GET "http://localhost:8080/api/books/search?query=harry&page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 3. Shelf Management

### 3.1 Add Book to Shelf

**Request:**
```bash
curl -X POST http://localhost:8080/api/shelves/add \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "shelf": "CURRENTLY_READING",
    "notes": "Borrowed from friend"
  }'
```

**Shelf Types:**
- `WANT_TO_READ`
- `CURRENTLY_READING`
- `READ`

**Optional Fields:**
- `notes`: Optional notes about the book (max 1000 characters)

**Response:**
```json
{
  "id": 1,
  "book": {
    "id": 1,
    "title": "Harry Potter and the Philosopher's Stone",
    ...
  },
  "shelf": "CURRENTLY_READING",
  "currentPage": null,
  "progressPercentage": null,
  "startedReadingAt": "2024-01-15T10:30:00",
  "finishedReadingAt": null,
  "rating": null,
  "notes": "Borrowed from friend",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 3.2 Update Reading Progress

**Request:**
```bash
curl -X PUT http://localhost:8080/api/shelves/progress/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPage": 150,
    "progressPercentage": 48.5
  }'
```

### 3.3 Move Book to Different Shelf

**Request:**
```bash
curl -X PUT "http://localhost:8080/api/shelves/move/1?shelf=READ" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3.4 Get Books by Shelf

**Request:**
```bash
curl -X GET http://localhost:8080/api/shelves/CURRENTLY_READING \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3.5 Get Currently Reading Books

**Request:**
```bash
curl -X GET http://localhost:8080/api/shelves/currently-reading \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3.6 Get Reading Timeline

**Request:**
```bash
curl -X GET "http://localhost:8080/api/shelves/timeline?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3.7 Rate a Book

**Request:**
```bash
curl -X PUT "http://localhost:8080/api/shelves/rate/1?rating=5" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 3.8 Remove Book from Library

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/shelves/remove/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 4. Reviews

### 4.1 Create a Review

**Request:**
```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "content": "This is an amazing book! The story is captivating and the characters are well-developed. Highly recommended for fantasy lovers!",
    "rating": 5
  }'
```

**Response:**
```json
{
  "id": 1,
  "user": {
    "id": 1,
    "username": "johndoe",
    "firstName": "John",
    "lastName": "Doe",
    "profileImageUrl": null
  },
  "book": {
    "id": 1,
    "title": "Harry Potter and the Philosopher's Stone",
    ...
  },
  "content": "This is an amazing book!...",
  "rating": 5,
  "createdAt": "2024-01-15T11:00:00",
  "updatedAt": "2024-01-15T11:00:00"
}
```

### 4.2 Update a Review

**Request:**
```bash
curl -X PUT http://localhost:8080/api/reviews/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "content": "Updated review: Still an amazing book after re-reading!",
    "rating": 5
  }'
```

### 4.3 Delete a Review

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/reviews/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 4.4 Get Reviews for a Book

**Request:**
```bash
curl -X GET "http://localhost:8080/api/reviews/book/1?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 4.5 Get My Reviews

**Request:**
```bash
curl -X GET "http://localhost:8080/api/reviews/my-reviews?page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## 5. Favourites

### 5.1 Add Book to Favourites

**Request:**
```bash
curl -X POST http://localhost:8080/api/favourites/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 5.2 Remove Book from Favourites

**Request:**
```bash
curl -X DELETE http://localhost:8080/api/favourites/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 5.3 Get All Favourite Books

**Request:**
```bash
curl -X GET http://localhost:8080/api/favourites \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 5.4 Check if Book is Favourite

**Request:**
```bash
curl -X GET http://localhost:8080/api/favourites/check/1 \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**Response:**
```json
true
```

---

## 6. Complete User Flow Example

### Step 1: Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Alice Smith","email":"alice@example.com","password":"Password123","confirmPassword":"Password123"}'
```

### Step 2: Search for Books
```bash
curl -X GET "http://localhost:8080/api/books/search/external?query=1984+orwell&maxResults=5"
```

### Step 3: Import a Book
```bash
curl -X POST http://localhost:8080/api/books/import/GOOGLE_BOOKS_ID \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Step 4: Add to "Want to Read" Shelf
```bash
curl -X POST http://localhost:8080/api/shelves/add \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"shelf":"WANT_TO_READ","notes":"Recommended by a friend"}'
```

### Step 5: Move to "Currently Reading"
```bash
curl -X PUT "http://localhost:8080/api/shelves/move/1?shelf=CURRENTLY_READING" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Step 6: Update Progress
```bash
curl -X PUT http://localhost:8080/api/shelves/progress/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"currentPage":100,"progressPercentage":33.3}'
```

### Step 7: Add to Favourites
```bash
curl -X POST http://localhost:8080/api/favourites/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Step 8: Finish Reading and Write Review
```bash
# Move to Read shelf
curl -X PUT "http://localhost:8080/api/shelves/move/1?shelf=READ" \
  -H "Authorization: Bearer YOUR_TOKEN"

# Write review
curl -X POST http://localhost:8080/api/reviews \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"content":"A masterpiece of dystopian fiction!","rating":5}'
```

---

## Error Responses

### 400 Bad Request (Validation Error)
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-01-15T10:30:00",
  "errors": {
    "username": "Username is required",
    "email": "Email should be valid"
  }
}
```

### 401 Unauthorized
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 503 Service Unavailable (Maintenance Mode)
```json
{
  "status": 503,
  "message": "The system is currently under maintenance. Please try again later.",
  "timestamp": "2025-11-01T10:30:00"
}
```

### 404 Not Found
```json
{
  "status": 404,
  "message": "Book not found with id: 999",
  "timestamp": "2024-01-15T10:30:00"
}
```

### 409 Conflict
```json
{
  "status": 409,
  "message": "Book already exists in your library",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## Tips for Testing

1. **Save your token**: After login/register, save the JWT token for subsequent requests
2. **Use Postman or Insomnia**: Import these examples into API testing tools
3. **Check H2 Console**: Access `http://localhost:8080/h2-console` to view database
4. **Enable logging**: Set logging level to DEBUG in application.yml for detailed logs
5. **Test pagination**: Most list endpoints support `page` and `size` parameters

