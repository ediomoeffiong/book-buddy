# Quick Start Guide - Book Buddy

Get your Book Buddy backend up and running in 5 minutes!

## Prerequisites
- Java 17 or higher installed
- Maven installed
- Your favorite API testing tool (Postman, Insomnia, or curl)

## Step 1: Build the Project

```bash
cd Book
mvn clean install
```

## Step 2: Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

You should see output like:
```
Started BookBuddyApplication in X.XXX seconds
```

## Step 3: Access H2 Database Console (Optional)

Open your browser and go to: `http://localhost:8080/h2-console`

- **JDBC URL**: `jdbc:h2:mem:bookbuddydb`
- **Username**: `sa`
- **Password**: (leave empty)

## Step 4: Test the API

### 4.1 Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Save the token from the response!**

### 4.2 Search for Books

```bash
curl -X GET "http://localhost:8080/api/books/search/external?query=harry+potter&maxResults=5"
```

**Note**: Results are cached for 5 minutes. Books in the response have temporary negative IDs until imported to your shelf.

### 4.2b Import Multiple Books (Admin Only)

If you have admin privileges, you can bulk-import external search results:

```bash
curl -X POST "http://localhost:8080/api/books/import/top?query=harry+potter&maxResults=3" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN_HERE"
```

This will search Google Books and automatically save the top N results to your database.

### 4.3 Add a Book to Your Shelf

First, import a book (use a googleBooksId from the search results):

```bash
curl -X POST http://localhost:8080/api/books/import/wrOQLV6xB-wC \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

Then add it to your shelf:

```bash
curl -X POST http://localhost:8080/api/shelves/add \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "shelf": "CURRENTLY_READING"
  }'
```

### 4.4 View Your Books

```bash
curl -X GET http://localhost:8080/api/shelves/CURRENTLY_READING \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## Step 5: Explore More

Check out these files for more information:
- **README.md** - Complete documentation
- **API_EXAMPLES.md** - Detailed API examples with all endpoints

## Common Issues

### Port 8080 Already in Use
Change the port in `src/main/resources/application.yml`:
```yaml
server:
  port: 8081
```

### Google Books API Not Working
The app works without a Google Books API key, but with limited results. To get full access:
1. Get an API key from [Google Cloud Console](https://console.cloud.google.com/)
2. Set environment variable:
   ```bash
   export GOOGLE_BOOKS_API_KEY=your_key_here
   ```
3. Restart the application

### External Search Results Caching
- Search results are cached for 5 minutes to minimize API rate limits
- Cache is stored in memory; restarting the app clears the cache
- Each unique search query is cached independently

### JWT Token Expired
Tokens expire after 24 hours. Simply login again to get a new token.

## Next Steps

1. ✅ Test all the endpoints using the examples in `API_EXAMPLES.md`
2. ✅ Explore the H2 database console to see your data
3. ✅ Try the complete user flow: register → search → add to shelf → update progress → write review
4. ✅ Configure PostgreSQL for production use
5. ✅ Build a frontend application to consume this API

## Need Help?

- Check the logs in the console for detailed error messages
- Review the `application.yml` for configuration options
- Ensure all prerequisites are installed correctly

## Advanced Features

### Caching
External book search results are automatically cached for 5 minutes to reduce API calls. You'll notice faster repeat searches for the same query.

### Admin Operations
Some endpoints require ADMIN role:
- `POST /api/books/import/top` - Bulk import external books

Default admin user (if seeded): admin@example.com / AdminPass123!

Happy Reading! 📚

