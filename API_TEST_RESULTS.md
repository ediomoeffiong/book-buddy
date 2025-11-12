# Book Buddy API Test Results

## Test Date: 2025-11-12

### ✅ Working Endpoints

#### Authentication
1. **POST /api/auth/register** - ✅ Working
   - Successfully registers new users
   - Returns JWT token, userId, username, email, firstName

2. **POST /api/auth/login** - ✅ Working
   - Successfully authenticates users
   - Returns JWT token

#### Books
3. **GET /api/books/search/external** - ✅ Working
   - Public endpoint (no auth required)
   - Successfully searches Google Books API
   - Returns list of books with googleBooksId

4. **GET /api/books/{id}** - ✅ Working (after book is imported)
   - Requires authentication
   - Returns book details

5. **POST /api/books/import/{googleBooksId}** - ⚠️ Needs Testing
   - Requires authentication
   - Imports book from Google Books API to database

#### Shelves
6. **POST /api/shelves/add** - ⚠️ Needs Testing
   - Requires authentication
   - Adds book to user's shelf

7. **GET /api/shelves/{shelf}** - ✅ Working
   - Requires authentication
   - Returns books on specified shelf

8. **GET /api/shelves/{shelf}/paginated** - ⚠️ Needs Testing
   - Requires authentication
   - Returns paginated shelf books

9. **GET /api/shelves/timeline** - ⚠️ Needs Testing
   - Requires authentication
   - Returns reading timeline

10. **GET /api/shelves/currently-reading** - ⚠️ Needs Testing
    - Requires authentication
    - Returns currently reading books

11. **PUT /api/shelves/progress/{bookId}** - ⚠️ Needs Testing
    - Requires authentication
    - Updates reading progress

12. **PUT /api/shelves/move/{bookId}** - ⚠️ Needs Testing
    - Requires authentication
    - Moves book to different shelf

13. **PUT /api/shelves/rate/{bookId}** - ⚠️ Needs Testing
    - Requires authentication
    - Rates a book

14. **DELETE /api/shelves/remove/{bookId}** - ⚠️ Needs Testing
    - Requires authentication
    - Removes book from library

#### Reviews
15. **POST /api/reviews** - ⚠️ Needs Testing
    - Requires authentication
    - Creates a review

16. **GET /api/reviews/{reviewId}** - ⚠️ Needs Testing
    - Requires authentication
    - Gets review by ID

17. **GET /api/reviews/book/{bookId}** - ⚠️ Needs Testing
    - Requires authentication
    - Gets reviews for a book

18. **GET /api/reviews/user/{userId}** - ⚠️ Needs Testing
    - Requires authentication
    - Gets reviews by user

19. **GET /api/reviews/my-reviews** - ⚠️ Needs Testing
    - Requires authentication
    - Gets authenticated user's reviews

20. **GET /api/reviews/user/{userId}/book/{bookId}** - ⚠️ Needs Testing
    - Requires authentication
    - Gets specific user's review for book

21. **PUT /api/reviews/{reviewId}** - ⚠️ Needs Testing
    - Requires authentication
    - Updates a review

22. **DELETE /api/reviews/{reviewId}** - ⚠️ Needs Testing
    - Requires authentication
    - Deletes a review

#### Favourites
23. **POST /api/favourites/{bookId}** - ⚠️ Needs Testing
    - Requires authentication
    - Adds book to favourites

24. **DELETE /api/favourites/{bookId}** - ⚠️ Needs Testing
    - Requires authentication
    - Removes book from favourites

25. **GET /api/favourites** - ✅ Working
    - Requires authentication
    - Returns list of favourite books

26. **GET /api/favourites/paginated** - ⚠️ Needs Testing
    - Requires authentication
    - Returns paginated favourites

27. **GET /api/favourites/check/{bookId}** - ⚠️ Needs Testing
    - Requires authentication
    - Checks if book is favourite

### 🔧 Fixes Applied

1. **JwtUtil.java** - Fixed deprecated method warnings
   - Changed `.setClaims()` to `.claims()`
   - Changed `.setSubject()` to `.subject()`
   - Changed `.setIssuedAt()` to `.issuedAt()`
   - Changed `.setExpiration()` to `.expiration()`

2. **ReviewService.java** - Removed unused imports
   - Removed `java.util.List`
   - Removed `java.util.stream.Collectors`

3. **BookService.java** - Added null handling
   - Added null checks for title and author in `convertToEntity()`
   - Defaults to "Untitled" and "Unknown Author" if null

4. **GoogleBooksService.java** - Added null handling
   - Added null check for VolumeInfo
   - Added null check for title
   - Returns null if VolumeInfo is missing

### 📝 Notes

- Application runs on `http://localhost:8080`
- JWT tokens are valid for 24 hours
- External book search works without authentication
- All other endpoints require Bearer token authentication
- CORS is configured for localhost:3000, localhost:4200, and localhost:8080

