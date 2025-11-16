# /api/books Endpoint - Google Books API Integration

## Quick Start

### Fetch from Database (Default)
```bash
curl "http://localhost:8080/api/books?page=0&size=20"
```

### Fetch Popular Books from Google Books API
```bash
curl "http://localhost:8080/api/books?page=0&size=20&external=true"
```

### Pagination Examples
```bash
# First page (10 results)
curl "http://localhost:8080/api/books?external=true&page=0&size=10"

# Second page (next 10 results)
curl "http://localhost:8080/api/books?external=true&page=1&size=10"

# Large page size (50 results)
curl "http://localhost:8080/api/books?external=true&page=0&size=50"
```

## Response Format

Both database and external API responses use the same format:

```json
{
  "content": [
    {
      "id": 1,
      "title": "Book Title",
      "author": "Author Name",
      "isbn": "9780123456789",
      "description": "Book description...",
      "publisher": "Publisher",
      "publishedDate": "2020",
      "pageCount": 350,
      "coverImageUrl": "https://example.com/cover.jpg",
      "categories": ["Fiction", "Adventure"],
      "language": "en",
      "googleBooksId": "ABC123def",
      "averageRating": 4.5,
      "ratingsCount": 1500
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

## Key Differences

| Aspect | Database | External API |
|--------|----------|--------------|
| **Data Source** | Local database | Google Books API |
| **ID Format** | Positive integers | Negative integers (temporary) |
| **Availability** | Fixed catalog | ~100 aggregated popular books |
| **Ratings** | User reviews | Google Books aggregated ratings |
| **Update Frequency** | Manual/Admin | Real-time from Google |
| **Import Capability** | Already in DB | Can import via `/api/books/import/{googleBooksId}` |
| **Response Time** | 20-50ms | 2-3 seconds (cold), 50ms (cached) |

## Popular Book Categories (external=true)

When `external=true`, the endpoint automatically queries these categories:
1. **bestseller** - Current bestselling books
2. **trending** - Trending now
3. **fiction** - Fiction genre
4. **science** - Science books
5. **mystery** - Mystery/Thriller
6. **romance** - Romance novels
7. **biography** - Biographies
8. **technology** - Tech/Programming
9. **self-help** - Self-improvement
10. **adventure** - Adventure/Travel

Approximately 10 books per category = ~100 total results

## Use Cases

### Discovery/Browsing
Users can browse popular/trending books:
```bash
GET /api/books?external=true&page=0&size=20
```

### Database-Only (Known Catalog)
Your app's curated book collection:
```bash
GET /api/books?page=0&size=20
# or
GET /api/books?external=false&page=0&size=20
```

### Import Books
Once you find a book from Google Books API, import it to your database:
```bash
POST /api/books/import/{googleBooksId}
```

After import, it appears in database results with a positive ID.

## Performance Tips

1. **Caching**: Results are cached for 5 minutes per query
2. **First request slower**: External API cold start ~2-3 seconds
3. **Subsequent requests faster**: Cached results served in ~50ms
4. **Combine searches**: Use multiple page requests to leverage cache

## Error Handling

- **Database unavailable**: Returns empty database results
- **Google API timeout**: Returns partial results or empty list
- **Invalid page numbers**: Returns empty content array
- **Rate limiting**: Uses caching to avoid exceeding API quotas

## Integration with Frontend

### Example React Component
```javascript
// Fetch from database (default)
const [books, setBooks] = useState([]);

useEffect(() => {
  fetch('http://localhost:8080/api/books?page=0&size=20')
    .then(r => r.json())
    .then(data => setBooks(data.content))
}, []);

// Fetch from Google Books
const fetchPopular = async () => {
  const data = await fetch('http://localhost:8080/api/books?page=0&size=20&external=true')
    .then(r => r.json());
  setBooks(data.content);
};

// Import to database
const importBook = async (googleBooksId) => {
  await fetch(`http://localhost:8080/api/books/import/${googleBooksId}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
};
```

## Query Parameters Reference

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | 0 | Page number (0-indexed) |
| `size` | integer | 20 | Results per page |
| `external` | boolean | false | Fetch from Google Books API if true |

## Related Endpoints

- `GET /api/books/{id}` - Get single book details
- `GET /api/books/search?query=xyz` - Search database
- `GET /api/books/search/external?query=xyz` - Search Google Books
- `POST /api/books/import/{googleBooksId}` - Import external book to database
- `GET /api/books/category/{category}` - Filter by category
- `GET /api/books/author/{author}` - Filter by author

---

**Documentation**: See `API_ENDPOINTS.md` for complete endpoint reference  
**Implementation**: See `GOOGLE_BOOKS_INTEGRATION.md` for technical details
