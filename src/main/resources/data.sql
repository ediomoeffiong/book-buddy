-- Seed sample books for local list/search

INSERT INTO books (id, title, author, isbn, description, publisher, published_date, page_count, cover_image_url, language, google_books_id, average_rating, ratings_count, created_at)
VALUES (1, 'The Hobbit', 'J.R.R. Tolkien', '9780261102217', 'A fantasy novel about Bilbo Baggins.', 'George Allen & Unwin', '1937', 310, '', 'en', 'GB_HOBBIT', 4.5, 1000, CURRENT_TIMESTAMP);

INSERT INTO books (id, title, author, isbn, description, publisher, published_date, page_count, cover_image_url, language, google_books_id, average_rating, ratings_count, created_at)
VALUES (2, '1984', 'George Orwell', '9780451524935', 'Dystopian social science fiction novel.', 'Secker & Warburg', '1949', 328, '', 'en', 'GB_1984', 4.3, 2000, CURRENT_TIMESTAMP);

INSERT INTO books (id, title, author, isbn, description, publisher, published_date, page_count, cover_image_url, language, google_books_id, average_rating, ratings_count, created_at)
VALUES (3, 'Pride and Prejudice', 'Jane Austen', '9780192833554', 'Romantic novel of manners.', 'T. Egerton', '1813', 279, '', 'en', 'GB_PRIDE', 4.2, 1500, CURRENT_TIMESTAMP);

-- Categories for seeded books
INSERT INTO book_categories (book_id, category) VALUES (1, 'Fantasy');
INSERT INTO book_categories (book_id, category) VALUES (2, 'Dystopian');
INSERT INTO book_categories (book_id, category) VALUES (3, 'Classic');

-- Ensure the IDENTITY/sequence for `books.id` starts after seeded IDs
ALTER TABLE books ALTER COLUMN id RESTART WITH 4;
