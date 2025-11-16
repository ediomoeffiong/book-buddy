# Book Buddy API Documentation

This document provides a comprehensive overview of the Book Buddy platform's API endpoints. The platform is a personal book management system that allows users to organize their reading lists, track reading progress, write reviews, and discover new books through integration with the Google Books API. All endpoints are built using Spring Boot and assume a base URL of `http://localhost:8080`.

## Authentication & Authorization

All protected endpoints require a valid JWT token in the `Authorization` header (e.g., `Bearer <token>`). Tokens are issued upon successful login or registration and are valid for 24 hours.

**Role-based access control (RBAC):**
- **USER**: Can manage their own profile, shelves, reviews, and favourites
- **ADMIN**: Has elevated privileges (future implementation)

---

## Auth Endpoints (`/api/auth`)

### POST `/api/auth/register`
Register a new user account.

**Request Body:**
```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "confirmPassword": "SecurePass123"
}
```

**Validation Rules:**
- `fullName`: Required, 2-100 characters
- `email`: Required, valid email format
- `password`: Required, minimum 8 characters, must contain at least one letter and one number
- `confirmPassword`: Required, must match password

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John"
}
```

**Errors:**
- `400`: Validation failed (passwords don't match, invalid format)
- `409`: Email already exists

---

### POST `/api/auth/login`
Login an existing user.

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePass123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John"
}
```

**Errors:**
- `401`: Invalid email or password
- `503`: System under maintenance

---

### GET `/api/auth/me`
Retrieve the currently authenticated user's information.

**Headers:**
- `Authorization` (String, required): Bearer token

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER",
  "bio": null,
  "profileImageUrl": null,
  "createdAt": "2025-11-16T10:30:00"
}
```

**Errors:**
- `401`: Unauthorized (missing or invalid JWT token)
- `404`: User not found

---

## Health & Monitoring

- `GET /health` : Lightweight health check that verifies database connectivity. Returns `200 OK` with JSON `{ "status": "UP", "database": "UP" }` when DB responds to `SELECT 1`. If the DB check fails the endpoint returns `503 Service Unavailable` with an `error` field describing the issue.

- Actuator endpoints (when enabled in production via `application-prod.yml`):
  - `GET /actuator/health` : Actuator health endpoint (may return detailed info depending on `management.endpoint.health.show-details`).
  - `GET /actuator/metrics` : Micrometer metrics endpoint.
  - `GET /actuator/prometheus` : Prometheus scrape endpoint (requires `micrometer-registry-prometheus` and exposure of `prometheus`).

Notes:
- The repository includes `docker-compose.yml` and `.env.example` for local Postgres testing. In production set `SPRING_PROFILES_ACTIVE=prod` and provide `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` and `JWT_SECRET`.


## Book Management (`/api/books`)

Handles book discovery, search, and import from external APIs. Books can be searched from the local database or fetched from Google Books API.

### GET `/api/books/{id}`
Retrieve a specific book by its database ID.

**Path Parameters:**
- `id` (Long): Book ID

**Response (200 OK):**
```json
{
  "id": 1,
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
```

**Errors:**
- `401`: Unauthorized
- `404`: Book not found

---

### GET `/api/books/search`
Search books in the local database.

**Query Parameters:**
- `query` (String, required): Search term (searches title, author, ISBN)
- `page` (Integer, default: 0): Page number
- `size` (Integer, default: 20): Page size

**Example:** `GET /api/books/search?query=harry+potter&page=0&size=20`

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Harry Potter and the Philosopher's Stone",
      "author": "J.K. Rowling",
      ...
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

**Errors:**
- `401`: Unauthorized

---

### GET `/api/books/search/external`
Search books from Google Books API (public endpoint, no authentication required).

**Query Parameters:**
- `query` (String, required): Search term
- `maxResults` (Integer, default: 20): Maximum number of results (1-40)

**Example:** `GET /api/books/search/external?query=harry+potter&maxResults=20`

**Response (200 OK):**
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

**Note:** Books returned from external API have `id: null` until imported into the database.

---

### POST `/api/books/import/{googleBooksId}`
Import a book from Google Books API into the local database.

**Path Parameters:**
- `googleBooksId` (String): Google Books ID (e.g., "wrOQLV6xB-wC")

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Harry Potter and the Philosopher's Stone",
  "author": "J.K. Rowling",
  "googleBooksId": "wrOQLV6xB-wC",
  ...
}
```

**Errors:**
- `401`: Unauthorized
- `404`: Book not found in Google Books API

---

## Shelf Management (`/api/shelves`)

Manage user's personal book shelves and reading progress. Each user can organize books into three shelves: `WANT_TO_READ`, `CURRENTLY_READING`, and `READ`.

### POST `/api/shelves/add`
Add a book to a shelf.

**Request Body:**
```json
{
  "bookId": 1,
  "shelf": "CURRENTLY_READING",
  "notes": "Borrowed from friend"
}
```

**Validation Rules:**
- `bookId`: Required
- `shelf`: Required, one of: `WANT_TO_READ`, `CURRENTLY_READING`, `READ`
- `notes`: Optional, max 1000 characters

**Response (201 Created):**
```json
{
  "id": 1,
  "book": { ... },
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

**Errors:**
- `400`: Invalid data
- `401`: Unauthorized
- `404`: Book not found
- `409`: Book already in library

---

### PUT `/api/shelves/move/{bookId}`
Move a book to a different shelf.

**Path Parameters:**
- `bookId` (Long): Book ID

**Query Parameters:**
- `shelf` (ShelfType, required): Target shelf (`WANT_TO_READ`, `CURRENTLY_READING`, `READ`)

**Example:** `PUT /api/shelves/move/1?shelf=READ`

**Response (200 OK):**
```json
{
  "id": 1,
  "book": { ... },
  "shelf": "READ",
  "finishedReadingAt": "2024-01-20T15:45:00",
  ...
}
```

**Errors:**
- `400`: Invalid shelf type
- `401`: Unauthorized
- `404`: Book not found in library

---

### DELETE `/api/shelves/remove/{bookId}`
Remove a book from the library entirely.

**Path Parameters:**
- `bookId` (Long): Book ID

**Response (204 No Content)**

**Errors:**
- `401`: Unauthorized
- `404`: Book not found in library

---

### GET `/api/shelves/{shelf}`
Get all books on a specific shelf.

**Path Parameters:**
- `shelf` (ShelfType): Shelf type (`WANT_TO_READ`, `CURRENTLY_READING`, `READ`)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "book": { ... },
    "shelf": "CURRENTLY_READING",
    "currentPage": 150,
    "progressPercentage": 48.5,
    ...
  }
]
```

**Errors:**
- `401`: Unauthorized

---

### GET `/api/shelves/{shelf}/paginated`
Get books on a specific shelf with pagination.

**Path Parameters:**
- `shelf` (ShelfType): Shelf type

**Query Parameters:**
- `page` (Integer, default: 0): Page number
- `size` (Integer, default: 20): Page size

**Response (200 OK):** Paginated response similar to book search

---

### GET `/api/shelves/timeline`
Get reading timeline (all books ordered by most recently updated).

**Query Parameters:**
- `page` (Integer, default: 0): Page number
- `size` (Integer, default: 20): Page size

**Response (200 OK):** Paginated list of UserBookDTO

---

### GET `/api/shelves/currently-reading`
Get all books currently being read.

**Response (200 OK):** List of UserBookDTO with `shelf: "CURRENTLY_READING"`

---

### PUT `/api/shelves/progress/{bookId}`
Update reading progress for a book.

**Path Parameters:**
- `bookId` (Long): Book ID

**Request Body:**
```json
{
  "currentPage": 150,
  "progressPercentage": 48.5
}
```

**Validation Rules:**
- `currentPage`: Optional, minimum 0
- `progressPercentage`: Optional, 0-100

**Response (200 OK):** Updated UserBookDTO

**Errors:**
- `400`: Invalid progress values
- `401`: Unauthorized
- `404`: Book not found in library

---

### PUT `/api/shelves/rate/{bookId}`
Rate a book (1-5 stars).

**Path Parameters:**
- `bookId` (Long): Book ID

**Query Parameters:**
- `rating` (Integer, required): Rating value (1-5)

**Example:** `PUT /api/shelves/rate/1?rating=5`

**Response (200 OK):** Updated UserBookDTO

**Errors:**
- `400`: Invalid rating (must be 1-5)
- `401`: Unauthorized
- `404`: Book not found in library

---

## Review Management (`/api/reviews`)

Users can write, edit, and delete reviews for books. Reviews include text content and a 1-5 star rating.

### POST `/api/reviews`
Create a new review for a book.

**Request Body:**
```json
{
  "bookId": 1,
  "content": "An absolutely magical journey! This book captivated me from start to finish.",
  "rating": 5
}
```

**Validation Rules:**
- `bookId`: Required
- `content`: Required, 10-5000 characters
- `rating`: Required, 1-5

**Response (201 Created):**
```json
{
  "id": 1,
  "user": {
    "id": 1,
    "username": "john.doe",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "bio": null,
    "profileImageUrl": null,
    "createdAt": "2024-01-01T10:00:00"
  },
  "book": { ... },
  "content": "An absolutely magical journey! This book captivated me from start to finish.",
  "rating": 5,
  "createdAt": "2024-01-15T14:30:00",
  "updatedAt": "2024-01-15T14:30:00"
}
```

**Errors:**
- `400`: Validation failed (content too short/long, invalid rating)
- `401`: Unauthorized
- `404`: Book not found
- `409`: You have already reviewed this book

---

### PUT `/api/reviews/{reviewId}`
Update an existing review.

**Path Parameters:**
- `reviewId` (Long): Review ID

**Request Body:**
```json
{
  "bookId": 1,
  "content": "Updated review content...",
  "rating": 4
}
```

**Response (200 OK):** Updated ReviewDTO

**Errors:**
- `400`: Validation failed
- `401`: Unauthorized
- `403`: Not authorized to edit this review
- `404`: Review not found

---

### DELETE `/api/reviews/{reviewId}`
Delete a review.

**Path Parameters:**
- `reviewId` (Long): Review ID

**Response (204 No Content)**

**Errors:**
- `401`: Unauthorized
- `403`: Not authorized to delete this review
- `404`: Review not found

---

### GET `/api/reviews/{reviewId}`
Get a specific review by ID.

**Path Parameters:**
- `reviewId` (Long): Review ID

**Response (200 OK):** ReviewDTO

**Errors:**
- `404`: Review not found

---

### GET `/api/reviews/book/{bookId}`
Get all reviews for a specific book (paginated).

**Path Parameters:**
- `bookId` (Long): Book ID

**Query Parameters:**
- `page` (Integer, default: 0): Page number
- `size` (Integer, default: 20): Page size

**Response (200 OK):** Paginated list of ReviewDTO

---

### GET `/api/reviews/user/{userId}`
Get all reviews by a specific user (paginated).

**Path Parameters:**
- `userId` (Long): User ID

**Query Parameters:**
- `page` (Integer, default: 0): Page number
- `size` (Integer, default: 20): Page size

**Response (200 OK):** Paginated list of ReviewDTO

---

### GET `/api/reviews/my-reviews`
Get all reviews by the authenticated user (paginated).

**Query Parameters:**
- `page` (Integer, default: 0): Page number
- `size` (Integer, default: 20): Page size

**Response (200 OK):** Paginated list of ReviewDTO

**Errors:**
- `401`: Unauthorized

---

### GET `/api/reviews/user/{userId}/book/{bookId}`
Get a specific user's review for a specific book.

**Path Parameters:**
- `userId` (Long): User ID
- `bookId` (Long): Book ID

**Response (200 OK):** ReviewDTO

**Errors:**
- `404`: Review not found

---

## Favourites Management (`/api/favourites`)

Users can mark books as favourites for quick access. This is separate from the shelf system.

### POST `/api/favourites/{bookId}`
Add a book to favourites.

**Path Parameters:**
- `bookId` (Long): Book ID

**Response (201 Created)**

**Errors:**
- `401`: Unauthorized
- `404`: Book not found
- `409`: Book already in favourites

---

### DELETE `/api/favourites/{bookId}`
Remove a book from favourites.

**Path Parameters:**
- `bookId` (Long): Book ID

**Response (204 No Content)**

**Errors:**
- `401`: Unauthorized
- `404`: Book not found in favourites

---

### GET `/api/favourites`
Get all favourite books.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Harry Potter and the Philosopher's Stone",
    "author": "J.K. Rowling",
    ...
  }
]
```

**Errors:**
- `401`: Unauthorized

---

### GET `/api/favourites/paginated`
Get favourite books with pagination.

**Query Parameters:**
- `page` (Integer, default: 0): Page number
- `size` (Integer, default: 20): Page size

**Response (200 OK):** Paginated list of BookDTO

**Errors:**
- `401`: Unauthorized

---

### GET `/api/favourites/check/{bookId}`
Check if a book is in favourites.

**Path Parameters:**
- `bookId` (Long): Book ID

**Response (200 OK):**
```json
true
```

**Errors:**
- `401`: Unauthorized

---

## Backend Architecture Overview

### Technology Stack
- **Framework**: Spring Boot 3.x with Java 17+
- **Database**: H2 (in-memory for development) - easily configurable for PostgreSQL/MySQL in production
- **Security**: Spring Security with JWT authentication
- **External API**: Google Books API integration via WebClient
- **ORM**: Spring Data JPA with Hibernate

### Data Storage
- **Database**: H2 in-memory database (development), supports migration to PostgreSQL/MySQL
- **Connection Pool**: HikariCP (default Spring Boot configuration)
- **Schema Management**: Hibernate auto-update (DDL)
- **Entities**: User, Book, UserBook, Review, Favourite

### Security Features
- **JWT Authentication**: Tokens valid for 24 hours
- **Password Encryption**: BCrypt hashing
- **Role-Based Access Control**: USER and ADMIN roles
- **CORS Configuration**: Configurable for frontend integration
- **Maintenance Mode**: System-wide maintenance mode support

### External API Integration
- **Google Books API**: Search and import books
- **Base URL**: `https://www.googleapis.com/books/v1`
- **Authentication**: API key (configurable via `GOOGLE_BOOKS_API_KEY` environment variable)
- **Rate Limiting**: Handled by Google Books API (1000 requests/day for free tier)

### Key Features
1. **Book Discovery**: Search from Google Books API without authentication
2. **Personal Library**: Organize books into three shelves (Want to Read, Currently Reading, Read)
3. **Reading Progress**: Track current page and percentage completion
4. **Reviews & Ratings**: Write reviews and rate books (1-5 stars)
5. **Favourites**: Quick access to favourite books
6. **Timeline**: View reading history chronologically

---

## Error Handling

All errors follow a consistent JSON format:

### Standard Error Response
```json
{
  "status": 400,
  "message": "Error description",
  "timestamp": "2024-01-15T14:30:00"
}
```

### Validation Error Response
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-01-15T14:30:00",
  "errors": {
    "email": "Email should be valid",
    "password": "Password must be at least 8 characters"
  }
}
```

### HTTP Status Codes
- **200 OK**: Successful GET/PUT request
- **201 Created**: Successful POST request (resource created)
- **204 No Content**: Successful DELETE request
- **400 Bad Request**: Validation failed or invalid data
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: User not authorized to perform action
- **404 Not Found**: Resource not found
- **409 Conflict**: Duplicate resource (e.g., email already exists, book already in library)
- **500 Internal Server Error**: Unexpected server error
- **503 Service Unavailable**: System under maintenance

---

## Configuration & Environment Variables

### Required Environment Variables
```bash
# JWT Configuration
JWT_SECRET=your-secret-key-here-minimum-256-bits

# Google Books API
GOOGLE_BOOKS_API_KEY=your-google-books-api-key

# Maintenance Mode (optional)
MAINTENANCE_MODE=false
```

### Application Properties
Located in `src/main/resources/application.yml`:
- **Server Port**: 8080 (default)
- **Database**: H2 in-memory (development)
- **JWT Expiration**: 24 hours (86400000 milliseconds)
- **H2 Console**: Enabled at `/h2-console` (development only)

---

## Usage Notes

### Pagination
All list endpoints support pagination with query parameters:
- `page`: Page number (0-indexed, default: 0)
- `size`: Items per page (default: 20, max: 100)

**Example:** `GET /api/books/search?query=harry&page=0&size=20`

### Authentication Flow
1. **Register**: `POST /api/auth/register` → Receive JWT token
2. **Login**: `POST /api/auth/login` → Receive JWT token
3. **Use Token**: Include in `Authorization` header: `Bearer <token>`
4. **Token Expiry**: Tokens expire after 24 hours, user must login again

### Book Import Workflow
1. **Search External**: `GET /api/books/search/external?query=harry+potter` (no auth required)
2. **Import Book**: `POST /api/books/import/{googleBooksId}` (requires auth)
3. **Add to Shelf**: `POST /api/shelves/add` with imported book's ID

### Shelf Types
- **WANT_TO_READ**: Books the user plans to read
- **CURRENTLY_READING**: Books the user is actively reading
- **READ**: Books the user has finished reading

When moving a book to `CURRENTLY_READING`, `startedReadingAt` is automatically set.
When moving to `READ`, `finishedReadingAt` is automatically set.

### Rating System
- **Book Ratings**: 1-5 stars (stored in UserBook entity)
- **Review Ratings**: 1-5 stars (stored in Review entity)
- **Average Rating**: Automatically calculated from all reviews for a book

---

## API Testing Examples

### Using cURL

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "confirmPassword": "SecurePass123"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**Search Books (External):**
```bash
curl -X GET "http://localhost:8080/api/books/search/external?query=harry+potter&maxResults=10"
```

**Add Book to Shelf:**
```bash
curl -X POST http://localhost:8080/api/shelves/add \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "shelf": "CURRENTLY_READING",
    "notes": "Recommended by a friend"
  }'
```

**Create Review:**
```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "content": "Amazing book! Highly recommended.",
    "rating": 5
  }'
```

---

## Extensibility & Future Enhancements

### Potential Extensions
1. **Social Features**: Follow users, share reading lists
2. **Reading Goals**: Set annual reading goals and track progress
3. **Book Recommendations**: AI-powered recommendations based on reading history
4. **Reading Challenges**: Join community reading challenges
5. **Book Clubs**: Create and join book clubs with discussion forums
6. **Export/Import**: Export reading data to CSV/JSON
7. **Statistics Dashboard**: Reading statistics and analytics
8. **Mobile App**: Native iOS/Android apps
9. **Admin Panel**: User management, content moderation
10. **Multiple External APIs**: Integration with Open Library, Goodreads, etc.

### Admin Features (Future)
- User management endpoints
- Content moderation (reviews)
- System analytics and monitoring
- Bulk operations

---

## Support & Contact

For issues, questions, or contributions, please refer to the project repository or contact the development team.

**Base URL (Development):** `http://localhost:8080`

**Base URL (Production):** Configure via environment variables

---

*Last Updated: 6th of November 2025*

