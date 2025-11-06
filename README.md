# Book Buddy - Book Tracking and Recommendation Web Application

A comprehensive backend REST API for tracking books, managing reading progress, writing reviews, and organizing personal book collections.

## Features

### MVP Components Implemented

- **Book Browsing**: Integration with Google Books API for searching and importing books
- **Virtual Shelves**: Organize books into "Want to Read", "Currently Reading", and "Read" categories
- **Favourites System**: Bookmark books for quick access
- **Reading Progress Tracker**: Track reading progress by pages or percentage
- **Rating System**: Rate books on a 1-5 star scale
- **Review System**: Write, edit, and delete book reviews
- **Reading Timeline**: View recently read and currently reading books
- **User Authentication**: JWT-based authentication with registration and login

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA** - Database operations
- **Spring Security** - Authentication and authorization
- **JWT (JSON Web Tokens)** - Secure authentication
- **H2 Database** - Development (in-memory)
- **PostgreSQL** - Production (configurable)
- **Maven** - Dependency management
- **Lombok** - Reduce boilerplate code
- **WebFlux** - External API integration

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- (Optional) PostgreSQL for production

## 🔧 Installation & Setup

### 1. Clone the repository

```bash
git clone <repository-url>
cd Book
```

### 2. Configure application properties

The application uses H2 in-memory database by default. To use PostgreSQL:

Edit `src/main/resources/application.yml` or set environment variables:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bookbuddy
    username: your_username
    password: your_password
```

### 3. Set up Google Books API (Optional)

To enable external book search:

1. Get a Google Books API key from [Google Cloud Console](https://console.cloud.google.com/)
2. Set the environment variable:
   ```bash
   export GOOGLE_BOOKS_API_KEY=your_api_key_here
   ```

### 4. Build the project

```bash
mvn clean install
```

### 5. Run the application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

### Authentication Endpoints

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "johndoe",
  "email": "john@example.com"
}
```

### Book Endpoints

#### Search Books (External API)
```http
GET /api/books/search/external?query=harry+potter&maxResults=20
```

#### Search Books (Database)
```http
GET /api/books/search?query=harry&page=0&size=20
Authorization: Bearer {token}
```

#### Get Book by ID
```http
GET /api/books/{id}
Authorization: Bearer {token}
```

#### Import Book from Google Books
```http
POST /api/books/import/{googleBooksId}
Authorization: Bearer {token}
```

### Shelf Management Endpoints

#### Add Book to Shelf
```http
POST /api/shelves/add
Authorization: Bearer {token}
Content-Type: application/json

{
  "bookId": 1,
  "shelf": "CURRENTLY_READING"
}
```

Shelf types: `WANT_TO_READ`, `CURRENTLY_READING`, `READ`

#### Move Book to Different Shelf
```http
PUT /api/shelves/move/{bookId}?shelf=READ
Authorization: Bearer {token}
```

#### Get Books by Shelf
```http
GET /api/shelves/{shelf}
Authorization: Bearer {token}
```

#### Update Reading Progress
```http
PUT /api/shelves/progress/{bookId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPage": 150,
  "progressPercentage": 45.5
}
```

#### Rate a Book
```http
PUT /api/shelves/rate/{bookId}?rating=5
Authorization: Bearer {token}
```

#### Get Reading Timeline
```http
GET /api/shelves/timeline?page=0&size=20
Authorization: Bearer {token}
```

#### Get Currently Reading Books
```http
GET /api/shelves/currently-reading
Authorization: Bearer {token}
```

### Review Endpoints

#### Create Review
```http
POST /api/reviews
Authorization: Bearer {token}
Content-Type: application/json

{
  "bookId": 1,
  "content": "This is an amazing book! Highly recommended.",
  "rating": 5
}
```

#### Update Review
```http
PUT /api/reviews/{reviewId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "bookId": 1,
  "content": "Updated review content",
  "rating": 4
}
```

#### Delete Review
```http
DELETE /api/reviews/{reviewId}
Authorization: Bearer {token}
```

#### Get Reviews for a Book
```http
GET /api/reviews/book/{bookId}?page=0&size=20
Authorization: Bearer {token}
```

#### Get My Reviews
```http
GET /api/reviews/my-reviews?page=0&size=20
Authorization: Bearer {token}
```

### Favourite Endpoints

#### Add to Favourites
```http
POST /api/favourites/{bookId}
Authorization: Bearer {token}
```

#### Remove from Favourites
```http
DELETE /api/favourites/{bookId}
Authorization: Bearer {token}
```

#### Get Favourite Books
```http
GET /api/favourites
Authorization: Bearer {token}
```

#### Check if Book is Favourite
```http
GET /api/favourites/check/{bookId}
Authorization: Bearer {token}
```

## Database Schema

### Main Entities

- **User**: User accounts with authentication
- **Book**: Book information (title, author, ISBN, etc.)
- **UserBook**: User's books with shelf information and reading progress
- **Review**: User reviews with ratings
- **Favourite**: User's favourite books

## Security

- JWT-based authentication
- Password encryption using BCrypt
- Role-based access control (USER, ADMIN)
- CORS configuration for frontend integration

## Testing

Run tests with:
```bash
mvn test
```

## Configuration

### JWT Configuration
- Secret key: Set via `JWT_SECRET` environment variable
- Token expiration: 24 hours (configurable in `application.yml`)

### Database Configuration
- Development: H2 in-memory database
- Production: PostgreSQL (configure via environment variables)

### H2 Console
Access H2 console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:bookbuddydb`
- Username: `sa`
- Password: (empty)

## Deployment

### Production Profile

Run with production profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Or set environment variable:
```bash
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

## Project Structure

```
Book/
├── src/
│   ├── main/
│   │   ├── java/com/bookbuddy/
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Custom Exceptions
│   │   │   ├── model/           # JPA Entities
│   │   │   ├── repository/      # Spring Data Repositories
│   │   │   ├── security/        # Security Configuration
│   │   │   ├── service/         # Business Logic
│   │   │   └── util/            # Utility Classes
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-prod.yml
│   └── test/                    # Test files
├── pom.xml
└── README.md
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Authors

- Your Name

## Acknowledgments

- Google Books API for book data
- Spring Boot team for the excellent framework

