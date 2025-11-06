# API Documentation Summary

## Document Created: `API_DOCUMENTATION.md`

A comprehensive API documentation has been created for the **Book Buddy** backend application, following the format and structure of the provided Freelance Match API sample.

---

## 📋 Documentation Contents

### 1. **Authentication & Authorization** (`/api/auth`)
- **POST** `/api/auth/register` - Register new user
- **POST** `/api/auth/login` - Login existing user
- JWT-based authentication (24-hour token validity)
- Role-based access control (USER, ADMIN)

### 2. **Book Management** (`/api/books`)
- **GET** `/api/books/{id}` - Get book by ID
- **GET** `/api/books/search` - Search books in database (paginated)
- **GET** `/api/books/search/external` - Search Google Books API (public endpoint)
- **POST** `/api/books/import/{googleBooksId}` - Import book from Google Books

### 3. **Shelf Management** (`/api/shelves`)
- **POST** `/api/shelves/add` - Add book to shelf
- **PUT** `/api/shelves/move/{bookId}` - Move book to different shelf
- **DELETE** `/api/shelves/remove/{bookId}` - Remove book from library
- **GET** `/api/shelves/{shelf}` - Get books on specific shelf
- **GET** `/api/shelves/{shelf}/paginated` - Get books with pagination
- **GET** `/api/shelves/timeline` - Get reading timeline
- **GET** `/api/shelves/currently-reading` - Get currently reading books
- **PUT** `/api/shelves/progress/{bookId}` - Update reading progress
- **PUT** `/api/shelves/rate/{bookId}` - Rate a book (1-5 stars)

**Shelf Types:**
- `WANT_TO_READ`
- `CURRENTLY_READING`
- `READ`

### 4. **Review Management** (`/api/reviews`)
- **POST** `/api/reviews` - Create review
- **PUT** `/api/reviews/{reviewId}` - Update review
- **DELETE** `/api/reviews/{reviewId}` - Delete review
- **GET** `/api/reviews/{reviewId}` - Get review by ID
- **GET** `/api/reviews/book/{bookId}` - Get reviews for book (paginated)
- **GET** `/api/reviews/user/{userId}` - Get reviews by user (paginated)
- **GET** `/api/reviews/my-reviews` - Get authenticated user's reviews
- **GET** `/api/reviews/user/{userId}/book/{bookId}` - Get specific user's review for book

### 5. **Favourites Management** (`/api/favourites`)
- **POST** `/api/favourites/{bookId}` - Add to favourites
- **DELETE** `/api/favourites/{bookId}` - Remove from favourites
- **GET** `/api/favourites` - Get all favourites
- **GET** `/api/favourites/paginated` - Get favourites with pagination
- **GET** `/api/favourites/check/{bookId}` - Check if book is favourite

---

## 🏗️ Backend Architecture Overview

### Technology Stack
- **Framework**: Spring Boot 3.x with Java 17+
- **Database**: H2 (in-memory for development)
- **Security**: Spring Security with JWT
- **External API**: Google Books API via WebClient
- **ORM**: Spring Data JPA with Hibernate

### Data Storage
- H2 in-memory database (development)
- Supports migration to PostgreSQL/MySQL
- Entities: User, Book, UserBook, Review, Favourite

### Security Features
- JWT authentication (24-hour tokens)
- BCrypt password encryption
- Role-based access control
- CORS configuration
- Maintenance mode support

### External API Integration
- **Google Books API**: Search and import books
- **Base URL**: `https://www.googleapis.com/books/v1`
- **Authentication**: API key via environment variable

---

## 🔧 Error Handling

### Standard Error Response Format
```json
{
  "status": 400,
  "message": "Error description",
  "timestamp": "2024-01-15T14:30:00"
}
```

### Validation Error Response Format
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
- **200 OK**: Successful GET/PUT
- **201 Created**: Successful POST
- **204 No Content**: Successful DELETE
- **400 Bad Request**: Validation failed
- **401 Unauthorized**: Missing/invalid token
- **403 Forbidden**: Not authorized
- **404 Not Found**: Resource not found
- **409 Conflict**: Duplicate resource
- **500 Internal Server Error**: Server error
- **503 Service Unavailable**: Maintenance mode

---

## 📝 Key Features Documented

1. **Complete Endpoint Coverage**: All 30+ endpoints documented with:
   - HTTP method and path
   - Request body/parameters
   - Validation rules
   - Response format (200 OK)
   - Error responses with status codes
   - Example requests

2. **Request/Response Examples**: JSON examples for all endpoints

3. **Validation Rules**: Detailed validation constraints for all input fields

4. **Authentication Flow**: Step-by-step authentication process

5. **Pagination Support**: Documentation for all paginated endpoints

6. **External API Integration**: Google Books API integration details

7. **Error Handling**: Comprehensive error response formats

8. **Configuration**: Environment variables and application properties

9. **Usage Notes**: Best practices and workflow examples

10. **cURL Examples**: Ready-to-use API testing commands

11. **Extensibility**: Future enhancement suggestions

---

## 🎯 Documentation Highlights

### Similar to Freelance Match Sample:
✅ Structured by endpoint groups  
✅ Detailed request/response formats  
✅ Comprehensive error handling section  
✅ Backend architecture overview  
✅ Security and authentication details  
✅ Usage notes and examples  
✅ Technology stack documentation  

### Additional Features:
✅ cURL testing examples  
✅ Environment variable configuration  
✅ Pagination documentation  
✅ Shelf type enumerations  
✅ Book import workflow  
✅ Rating system explanation  
✅ Future extensibility section  

---

## 📊 Statistics

- **Total Endpoints**: 30+
- **Endpoint Groups**: 5 (Auth, Books, Shelves, Reviews, Favourites)
- **Document Length**: 875+ lines
- **Code Examples**: 40+ JSON/cURL examples
- **Error Codes**: 10 HTTP status codes documented

---

## 🚀 Usage

The documentation is ready for:
- **Frontend Developers**: Integration guide with request/response formats
- **API Consumers**: Complete endpoint reference
- **Testing Teams**: cURL examples for API testing
- **DevOps**: Configuration and deployment information
- **Stakeholders**: Architecture and feature overview

---

## 📁 File Location

**Main Documentation**: `API_DOCUMENTATION.md` (877 lines)

---

## ✨ Next Steps

1. **Review**: Review the documentation for accuracy
2. **Test**: Use cURL examples to test endpoints
3. **Share**: Distribute to frontend team and stakeholders
4. **Update**: Keep documentation in sync with code changes
5. **Extend**: Add Swagger/OpenAPI specification if needed

---

*Documentation created following the Freelance Match API sample format*

