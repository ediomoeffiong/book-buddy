# Frontend Integration Guide - Book Buddy

## Quick Reference for Frontend Developers

This guide provides essential information for integrating the Book Buddy frontend with the updated backend API.

---

## 🔐 Authentication Changes

### Registration Form

**Required Fields:**
```javascript
{
  fullName: string,        // Required, 2-100 chars
  email: string,           // Required, valid email, unique
  password: string,        // Required, min 8 chars, must have letter + number
  confirmPassword: string  // Required, must match password
}
```

**Example Form Validation:**
```javascript
const validateRegistration = (data) => {
  const errors = {};
  
  // Full Name
  if (!data.fullName || data.fullName.length < 2) {
    errors.fullName = "Full name is required (minimum 2 characters)";
  }
  
  // Email
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!data.email || !emailRegex.test(data.email)) {
    errors.email = "Please enter a valid email address";
  }
  
  // Password
  const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;
  if (!data.password || !passwordRegex.test(data.password)) {
    errors.password = "Password must be at least 8 characters and contain at least one letter and one number";
  }
  
  // Confirm Password
  if (data.password !== data.confirmPassword) {
    errors.confirmPassword = "Passwords do not match";
  }
  
  return errors;
};
```

**API Call:**
```javascript
const register = async (fullName, email, password, confirmPassword) => {
  const response = await fetch('http://localhost:8080/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ fullName, email, password, confirmPassword })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  const data = await response.json();
  // Store token and user info
  localStorage.setItem('token', data.token);
  localStorage.setItem('firstName', data.firstName);
  return data;
};
```

---

### Login Form

**Required Fields:**
```javascript
{
  email: string,     // Required (changed from username)
  password: string   // Required
}
```

**API Call:**
```javascript
const login = async (email, password) => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    
    if (!response.ok) {
      const error = await response.json();
      
      // Handle maintenance mode
      if (response.status === 503) {
        throw new Error(error.message); // Display maintenance message
      }
      
      throw new Error(error.message);
    }
    
    const data = await response.json();
    // Store token and user info
    localStorage.setItem('token', data.token);
    localStorage.setItem('firstName', data.firstName);
    return data;
  } catch (error) {
    throw error;
  }
};
```

**Maintenance Mode Handling:**
```javascript
// Display maintenance banner when status is 503
if (error.status === 503) {
  showMaintenanceBanner(error.message);
  disableLoginForm();
}
```

---

## 👋 User Greeting

After successful login/registration, use the `firstName` from the response:

```javascript
// Get firstName from localStorage or auth context
const firstName = localStorage.getItem('firstName');

// Display greetings
const myShelvesGreeting = `Hi ${firstName}, here are your current reads.`;
const browseBooksGreeting = `Hi ${firstName}, discover books you might enjoy.`;
```

**Example Component:**
```jsx
const MyShelvesPage = () => {
  const { firstName } = useAuth();
  
  return (
    <div>
      <h1>Hi {firstName}, here are your current reads.</h1>
      {/* Rest of the page */}
    </div>
  );
};
```

---

## 📚 Adding Books to Shelves

**With Optional Notes:**
```javascript
const addBookToShelf = async (bookId, shelf, notes = null) => {
  const token = localStorage.getItem('token');
  
  const response = await fetch('http://localhost:8080/api/shelves/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ bookId, shelf, notes })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return await response.json();
};
```

**Example Form:**
```jsx
const AddBookModal = ({ bookId, onClose }) => {
  const [shelf, setShelf] = useState('WANT_TO_READ');
  const [notes, setNotes] = useState('');
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await addBookToShelf(bookId, shelf, notes || null);
      onClose();
    } catch (error) {
      alert(error.message);
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      <select value={shelf} onChange={(e) => setShelf(e.target.value)}>
        <option value="WANT_TO_READ">Want to Read</option>
        <option value="CURRENTLY_READING">Currently Reading</option>
        <option value="READ">Read</option>
      </select>
      
      <textarea
        placeholder="Optional notes (e.g., 'Borrowed from friend')"
        value={notes}
        onChange={(e) => setNotes(e.target.value)}
        maxLength={1000}
      />
      
      <button type="submit">Save Book</button>
    </form>
  );
};
```

---

## 🔄 Progress Tracking

**Auto-move to "Read" shelf:**
When progress reaches 100%, the book automatically moves to the "Read" shelf.

```javascript
const updateProgress = async (bookId, currentPage, progressPercentage) => {
  const token = localStorage.getItem('token');
  
  const response = await fetch(`http://localhost:8080/api/shelves/progress/${bookId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ currentPage, progressPercentage })
  });
  
  const data = await response.json();
  
  // Check if book moved to "Read" shelf
  if (data.shelf === 'READ' && data.progressPercentage >= 100) {
    showNotification('Congratulations! You finished this book!');
  }
  
  return data;
};
```

---

## 🎨 UI/UX Recommendations

### Registration Page
1. Show password strength indicator
2. Real-time validation for password requirements
3. Show/hide password toggle
4. Clear error messages for each field

### Login Page
1. Display maintenance banner prominently when active
2. Disable login form during maintenance
3. Show "Invalid email or password" for failed attempts

### My Shelves Page
1. Display user greeting: "Hi {firstName}, here are your current reads."
2. Show three sections: Currently Reading, Want to Read, Read
3. Display progress bars for Currently Reading books
4. Show notes when hovering over book cards

### Browse Books Page
1. Display user greeting: "Hi {firstName}, discover books you might enjoy."
2. Add "Add to Shelf" button with notes option
3. Show favourite toggle (heart icon)

### Book Details Page
1. Show all book information
2. "Add to Shelf" button with modal for shelf selection and notes
3. Display user reviews with ratings
4. Allow rating and reviewing

---

## 📋 Error Handling

**Common Error Responses:**

```javascript
const handleApiError = (error) => {
  switch (error.status) {
    case 400:
      // Validation errors
      return error.errors; // Object with field-specific errors
      
    case 401:
      // Unauthorized - redirect to login
      localStorage.removeItem('token');
      window.location.href = '/login';
      break;
      
    case 403:
      // Forbidden
      alert('You do not have permission to perform this action');
      break;
      
    case 404:
      // Not found
      alert('Resource not found');
      break;
      
    case 409:
      // Conflict (e.g., duplicate email, book already in library)
      alert(error.message);
      break;
      
    case 503:
      // Maintenance mode
      showMaintenanceBanner(error.message);
      break;
      
    default:
      alert('An unexpected error occurred');
  }
};
```

---

## 🔧 Environment Configuration

```javascript
// config.js
export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const API_ENDPOINTS = {
  REGISTER: `${API_BASE_URL}/api/auth/register`,
  LOGIN: `${API_BASE_URL}/api/auth/login`,
  SEARCH_BOOKS: `${API_BASE_URL}/api/books/search/external`,
  ADD_TO_SHELF: `${API_BASE_URL}/api/shelves/add`,
  GET_SHELF: (shelf) => `${API_BASE_URL}/api/shelves/${shelf}`,
  UPDATE_PROGRESS: (bookId) => `${API_BASE_URL}/api/shelves/progress/${bookId}`,
  // ... other endpoints
};
```

---

## 🧪 Testing Checklist

### Registration
- [ ] Valid registration with all fields
- [ ] Password validation (min 8 chars, letter + number)
- [ ] Password mismatch error
- [ ] Duplicate email error
- [ ] Successful redirect to My Shelves with greeting

### Login
- [ ] Valid login with email
- [ ] Invalid credentials error
- [ ] Maintenance mode banner display
- [ ] Successful redirect to My Shelves with greeting

### Book Management
- [ ] Add book with notes
- [ ] Add book without notes
- [ ] Update reading progress
- [ ] Auto-move to Read shelf at 100%
- [ ] Display notes on book cards

### User Greeting
- [ ] Greeting displays on My Shelves page
- [ ] Greeting displays on Browse Books page
- [ ] Greeting disappears after logout

---

## 📞 Support

For questions or issues:
1. Check the API_EXAMPLES.md file for detailed API examples
2. Review IMPLEMENTATION_SUMMARY.md for technical details
3. Check the H2 console at http://localhost:8080/h2-console for database inspection

---

## 🚀 Quick Start

1. Ensure backend is running on http://localhost:8080
2. Update your API base URL in frontend config
3. Update registration form to use new fields
4. Update login form to use email instead of username
5. Add firstName to user context/state management
6. Display user greetings on appropriate pages
7. Add notes field to "Add to Shelf" modal
8. Test all flows thoroughly

---

**Happy Coding! 🎉**

