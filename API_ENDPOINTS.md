# Book Buddy API Endpoints

## Base URL
```
http://localhost:8080
```

---

## Authentication Endpoints (`/api/auth`)

### 1. Register User
**POST** `/api/auth/register`

**Description:** Register a new user account.

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "Password123",
  "confirmPassword": "Password123"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "john",
  "email": "john@example.com",
  "firstName": "John"
}
```

**Validation Rules:**
- `fullName`: Required, 2-100 characters
- `email`: Required, must be valid email
- `password`: Required, min 8 characters, must contain at least one letter and one number
- `confirmPassword`: Must match `password`

**Possible Errors:**
- `400 Bad Request`: Validation failed
- `409 Conflict`: Email already exists
- `500 Internal Server Error`: Password mismatch

---

### 2. Login User
**POST** `/api/auth/login`

**Description:** Login with email or username and password.

**Request Body (using email):**
```json
{
  "email": "john@example.com",
  "password": "Password123"
}
```

**Request Body (using username):**
```json
{
  "username": "john",
  "password": "Password123"
}
```

**Request Body (using usernameOrEmail):**
```json
{
  "usernameOrEmail": "john@example.com",
  "password": "Password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "john",
  "email": "john@example.com",
  "firstName": "John"
}
```

**Possible Errors:**
- `400 Bad Request`: Invalid email/username or password
- `503 Service Unavailable`: System under maintenance

---

## Book Endpoints (`/api/books`)

### 3. Get All Books (Paginated)
**GET** `/api/books?page=0&size=20`

**Description:** Get a paginated list of all books.

**Query Parameters:**
- `page` (optional, default=0): Page number (0-indexed)
- `size` (optional, default=20): Number of books per page

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "The Hobbit",
      "author": "J.R.R. Tolkien",
      "isbn": "9780261102217",
      "description": "A fantasy novel...",
      "publisher": "George Allen & Unwin",
      "publishedDate": "1937",
      "pageCount": 310,
      "coverImageUrl": "http://...",
      "categories": ["Fantasy"],
      "language": "en",
      "googleBooksId": "GB_HOBBIT",
      "averageRating": 4.5,
      "ratingsCount": 1000
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

---

### 4. Get Book by ID
**GET** `/api/books/{id}`

**Description:** Get details of a specific book.

**Path Parameters:**
- `id` (required): Book ID

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "The Hobbit",
  "author": "J.R.R. Tolkien",
  ...
}
```

**Possible Errors:**
- `404 Not Found`: Book not found
- `403 Forbidden`: Unauthorized access

---

### 5. Search Books in Database
**GET** `/api/books/search?query=harry&page=0&size=20`

**Description:** Search for books in the local database by title, author, or ISBN.

**Query Parameters:**
- `query` (required): Search term
- `page` (optional, default=0): Page number
- `size` (optional, default=20): Results per page

**Response (200 OK):**
```json
{
  "content": [
    { ... book data ... }
  ],
  "pageable": { ... },
  "totalElements": 5,
  "totalPages": 1
}
```

---

### 6. Search Books from External API
**GET** `/api/books/search/external?query=harry&maxResults=5`

**Description:** Search for books from Google Books API (public endpoint, no auth required).

**Query Parameters:**
- `query` (required): Search term
- `maxResults` (optional, default=20): Maximum number of results to return

**Response (200 OK):**
```json
[
  {
    "id": null,
    "title": "Harry Potter and the Philosopher's Stone",
    "author": "J.K. Rowling",
    "isbn": "...",
    "description": "...",
    "publisher": "Bloomsbury",
    "publishedDate": "1997",
    "pageCount": 223,
    "coverImageUrl": "http://...",
    "categories": ["Fantasy"],
    "language": "en",
    "googleBooksId": "...",
    "averageRating": 4.8,
    "ratingsCount": 50000
  }
]
```

---

### 7. Import Book from External API
**POST** `/api/books/import/{googleBooksId}`

**Description:** Import a book from Google Books API into the database (requires authentication).

**Path Parameters:**
- `googleBooksId` (required): Google Books ID

**Headers:**
- `Authorization: Bearer {token}` (required)

**Response (200 OK):**
```json
{
  "id": 5,
  "title": "Harry Potter and the Philosopher's Stone",
  ...
}
```

**Possible Errors:**
- `401 Unauthorized`: Missing or invalid token
- `404 Not Found`: Book not found on Google Books

---

### 8. Get Books by Category
**GET** `/api/books/category/{category}?page=0&size=20`

**Description:** Get all books in a specific category.

**Path Parameters:**
- `category` (required): Category name (e.g., "Fiction", "Fantasy", "Science")

**Query Parameters:**
- `page` (optional, default=0): Page number
- `size` (optional, default=20): Results per page

**Response (200 OK):**
```json
{
  "content": [ ... ],
  "pageable": { ... },
  "totalElements": 25,
  "totalPages": 2
}
```

---

### 9. Get Books by Author
**GET** `/api/books/author/{author}?page=0&size=20`

**Description:** Get all books by a specific author.

**Path Parameters:**
- `author` (required): Author name

**Query Parameters:**
- `page` (optional, default=0): Page number
- `size` (optional, default=20): Results per page

**Response (200 OK):**
```json
{
  "content": [ ... ],
  "pageable": { ... },
  "totalElements": 12,
  "totalPages": 1
}
```

---

## Shelf Endpoints (`/api/shelves`)
*Requires Authentication (Bearer Token)*

### 10. Add Book to Shelf
**POST** `/api/shelves/add`

**Description:** Add a book to user's shelf (WANT_TO_READ, CURRENTLY_READING, or READ).

**Headers:**
- `Authorization: Bearer {token}` (required)

**Request Body:**
```json
{
  "bookId": 1,
  "shelf": "WANT_TO_READ"
}
```

**Valid shelf values:**
- `WANT_TO_READ`
- `CURRENTLY_READING`
- `READ`

**Response (200 OK):**
```json
{
  "id": 10,
  "bookId": 1,
  "shelf": "WANT_TO_READ",
  "rating": null,
  "progress": 0
}
```

---

### 11. Move Book Between Shelves
**PUT** `/api/shelves/move/{bookId}?shelf=CURRENTLY_READING`

**Description:** Move a book from one shelf to another.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `bookId` (required): Book ID

**Query Parameters:**
- `shelf` (required): Target shelf (WANT_TO_READ, CURRENTLY_READING, READ)

**Response (200 OK):**
```json
{
  "id": 10,
  "bookId": 1,
  "shelf": "CURRENTLY_READING",
  ...
}
```

---

### 12. Remove Book from Shelf
**DELETE** `/api/shelves/remove/{bookId}`

**Description:** Remove a book from user's shelf.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `bookId` (required): Book ID

**Response (204 No Content)**

---

### 13. Get Books on Shelf
**GET** `/api/shelves/{shelf}`

**Description:** Get all books on a specific shelf.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `shelf` (required): Shelf name (WANT_TO_READ, CURRENTLY_READING, READ)

**Response (200 OK):**
```json
{
  "bookId": 1,
  "title": "The Hobbit",
  ...
}
```

---

### 14. Get Books on Shelf (Paginated)
**GET** `/api/shelves/{shelf}/paginated?page=0&size=20`

**Description:** Get paginated list of books on a specific shelf.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `shelf` (required): Shelf name (WANT_TO_READ, CURRENTLY_READING, READ)

**Query Parameters:**
- `page` (optional, default=0): Page number
- `size` (optional, default=20): Results per page

**Response (200 OK):**
```json
{
  "content": [ ... ],
  "pageable": { ... },
  "totalElements": 10,
  "totalPages": 1
}
```

---

### 15. Update Reading Progress
**PUT** `/api/shelves/progress/{bookId}`

**Description:** Update reading progress for a book (must be on CURRENTLY_READING shelf).

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `bookId` (required): Book ID

**Request Body:**
```json
{
  "currentPage": 150,
  "progressPercentage": 48.5
}
```

**Response (200 OK):**
```json
{
  "id": 10,
  "bookId": 1,
  "currentPage": 150,
  "progressPercentage": 48.5,
  ...
}
```

---

### 16. Rate Book
**PUT** `/api/shelves/rate/{bookId}`

**Description:** Add or update rating for a book (must be on READ shelf).

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `bookId` (required): Book ID

**Request Body:**
```json
{
  "rating": 5
}
```

**Valid rating values:** 1-5

**Response (200 OK):**
```json
{
  "id": 10,
  "bookId": 1,
  "rating": 5,
  "shelf": "READ",
  ...
}
```

---

## Review Endpoints (`/api/reviews`)
*Requires Authentication (Bearer Token)*

### 17. Create Review
**POST** `/api/reviews`

**Description:** Create a new review for a book.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Request Body:**
```json
{
  "bookId": 1,
  "rating": 4,
  "content": "Great book! Highly recommend it."
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "bookId": 1,
  "userId": 1,
  "rating": 4,
  "content": "Great book! Highly recommend it.",
  "createdAt": "2025-11-16T12:00:00"
}
```

---

### 18. Update Review
**PUT** `/api/reviews/{reviewId}`

**Description:** Update an existing review.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `reviewId` (required): Review ID

**Request Body:**
```json
{
  "rating": 5,
  "content": "Excellent book! Changed my mind."
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "bookId": 1,
  "userId": 1,
  "rating": 5,
  "content": "Excellent book! Changed my mind.",
  "updatedAt": "2025-11-16T13:00:00"
}
```

---

### 19. Delete Review
**DELETE** `/api/reviews/{reviewId}`

**Description:** Delete a review.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `reviewId` (required): Review ID

**Response (204 No Content)**

---

### 20. Get Review by ID
**GET** `/api/reviews/{reviewId}`

**Description:** Get details of a specific review.

**Path Parameters:**
- `reviewId` (required): Review ID

**Response (200 OK):**
```json
{
  "id": 1,
  "bookId": 1,
  "userId": 1,
  "rating": 4,
  "content": "Great book!",
  "createdAt": "2025-11-16T12:00:00"
}
```

---

### 21. Get Reviews for a Book
**GET** `/api/reviews/book/{bookId}?page=0&size=10`

**Description:** Get all reviews for a specific book.

**Path Parameters:**
- `bookId` (required): Book ID

**Query Parameters:**
- `page` (optional, default=0): Page number
- `size` (optional, default=10): Results per page

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "bookId": 1,
      "userId": 1,
      "rating": 4,
      "content": "Great book!",
      ...
    }
  ],
  "pageable": { ... },
  "totalElements": 3,
  "totalPages": 1
}
```

---

### 22. Get Reviews by User
**GET** `/api/reviews/user/{userId}?page=0&size=10`

**Description:** Get all reviews written by a specific user.

**Path Parameters:**
- `userId` (required): User ID

**Query Parameters:**
- `page` (optional, default=0): Page number
- `size` (optional, default=10): Results per page

**Response (200 OK):**
```json
{
  "content": [ ... ],
  "pageable": { ... },
  "totalElements": 5,
  "totalPages": 1
}
```

---

## Favourite Endpoints (`/api/favourites`)
*Requires Authentication (Bearer Token)*

### 23. Add Book to Favourites
**POST** `/api/favourites/{bookId}`

**Description:** Add a book to user's favorites list.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `bookId` (required): Book ID

**Response (201 Created)**

---

### 24. Remove Book from Favourites
**DELETE** `/api/favourites/{bookId}`

**Description:** Remove a book from user's favorites list.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `bookId` (required): Book ID

**Response (204 No Content)**

---

### 25. Get Favourite Books
**GET** `/api/favourites`

**Description:** Get all books in user's favorites list.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "The Hobbit",
    ...
  },
  {
    "id": 2,
    "title": "1984",
    ...
  }
]
```

---

### 26. Get Favourite Books (Paginated)
**GET** `/api/favourites/paginated?page=0&size=20`

**Description:** Get paginated list of favorite books.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Query Parameters:**
- `page` (optional, default=0): Page number
- `size` (optional, default=20): Results per page

**Response (200 OK):**
```json
{
  "content": [ ... ],
  "pageable": { ... },
  "totalElements": 15,
  "totalPages": 1
}
```

---

### 27. Check if Book is Favourite
**GET** `/api/favourites/check/{bookId}`

**Description:** Check if a book is in user's favorites.

**Headers:**
- `Authorization: Bearer {token}` (required)

**Path Parameters:**
- `bookId` (required): Book ID

**Response (200 OK):**
```json
true
```
or
```json
false
```

---

## Root Endpoint

### 28. API Status
**GET** `/`

**Description:** Check if API is running.

**Response (200 OK):**
```json
{
  "status": "Book Buddy API is running!"
}
```

---

## Authentication Header Format

For all endpoints that require authentication, use:
```
Authorization: Bearer {token}
```

Where `{token}` is the JWT token received from the `/api/auth/login` or `/api/auth/register` endpoint.

---

## Error Responses

### 400 Bad Request
```json
{
  "error": "Bad Request",
  "message": "Invalid request parameters"
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Missing or invalid authentication token"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to access this resource"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "Resource not found"
}
```

### 409 Conflict
```json
{
  "error": "Conflict",
  "message": "Email already exists"
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Testing Authentication

### Test Registration Flow
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

### Test Login Flow
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password123"
  }'
```

### Test Protected Endpoint
```bash
curl -X GET http://localhost:8080/api/books/1 \
  -H "Authorization: Bearer {token}"
```

---

## Important Notes

1. **Authentication**: Most endpoints (except registration, login, and external search) require a valid JWT token in the `Authorization` header.

2. **Pagination**: Use `page` and `size` query parameters for paginated endpoints. Page numbering is 0-indexed.

3. **Rate Limits**: Currently, there are no rate limits implemented, but this may change in future versions.

4. **CORS**: The API has CORS enabled for all origins to allow frontend development.

5. **Maintenance Mode**: The login endpoint may return 503 if the system is under maintenance. Check the `/` endpoint first if you're having issues.

---

**Last Updated:** November 16, 2025
