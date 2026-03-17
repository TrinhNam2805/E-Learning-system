// Authentication Service
class AuthService {
  constructor() {
    this.usersFile = 'mockData/users.json';
    this.users = [];
    this.loadUsers();
  }

  async loadUsers() {
    try {
      const response = await fetch(this.usersFile);
      const data = await response.json();
      this.users = data;
    } catch (error) {
      console.error('Failed to load users:', error);
    }
  }

  // Login với email và password
  async login(email, password) {
    // Đợi nếu đang load users
    if (this.users.length === 0) {
      await new Promise(resolve => setTimeout(resolve, 100));
    }

    const user = this.users.find(u => u.email === email && u.password === password);
    
    if (user) {
      // Tạo token đơn giản (trong production dùng JWT)
      const token = this.generateToken(user);
      
      // Lưu token và thông tin user vào localStorage
      localStorage.setItem('authToken', token);
      localStorage.setItem('currentUser', JSON.stringify({
        id: user.id,
        username: user.username,
        email: user.email,
        fullName: user.fullName,
        role: user.role
      }));
      
      return { success: true, user: user };
    } else {
      return { success: false, message: 'Invalid email or password' };
    }
  }

  // Tạo token (đơn giản, trong production dùng JWT)
  generateToken(user) {
    const tokenData = {
      id: user.id,
      email: user.email,
      iat: Date.now(),
      exp: Date.now() + (24 * 60 * 60 * 1000) // 24 hours
    };
    return btoa(JSON.stringify(tokenData)); // Base64 encoding
  }

  // Kiểm tra xem user đã login chưa
  isLoggedIn() {
    return localStorage.getItem('authToken') !== null;
  }

  // Lấy user hiện tại
  getCurrentUser() {
    const userStr = localStorage.getItem('currentUser');
    return userStr ? JSON.parse(userStr) : null;
  }

  // Lấy token hiện tại
  getToken() {
    return localStorage.getItem('authToken');
  }

  // Logout
  logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
  }

  /**
   * Cập nhật thông tin user hiện tại.
   * updatedFields: { id?, fullName?, email?, phone?, dob?, department?, address?, avatar? }
   * LƯU Ý: không thể ghi vào mockData/users.json từ client — ta cập nhật localStorage & bộ nhớ tạm users[] nếu có.
   */
  updateProfile(updatedFields = {}) {
    const current = this.getCurrentUser();
    if (!current) {
      return { success: false, message: 'No logged in user' };
    }

    // Merge into current user
    const updatedUser = { ...current, ...updatedFields };
    localStorage.setItem('currentUser', JSON.stringify(updatedUser));

    // Update in-memory users[] if loaded (best-effort)
    if (this.users && this.users.length > 0) {
      const idx = this.users.findIndex(u => u.id === updatedUser.id || u.email === updatedUser.email);
      if (idx !== -1) {
        // Update fields on mock user object
        this.users[idx] = { ...this.users[idx], ...updatedFields, fullName: updatedUser.fullName, email: updatedUser.email };
      }
    }

    return { success: true, user: updatedUser };
  }
}

// Khởi tạo Auth Service
let authService;
document.addEventListener('DOMContentLoaded', () => {
  authService = new AuthService();
});

// Login Form Handler
function handleLogin(e) {
  e.preventDefault();
  
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value.trim();
  const errorElement = document.getElementById('login-error');

  // Clear error message
  if (errorElement) {
    errorElement.textContent = '';
    errorElement.style.display = 'none';
  }

  // Validate input
  if (!email || !password) {
    if (errorElement) {
      errorElement.textContent = 'Please enter both email and password';
      errorElement.style.display = 'block';
    }
    return;
  }

  // Attempt login
  authService.login(email, password).then(result => {
    if (result.success) {
      // Login successful - redirect to homepage
      alert(`Welcome ${result.user.fullName}! Your token has been saved.`);
      window.location.href = 'homepage.html';
    } else {
      // Login failed
      if (errorElement) {
        errorElement.textContent = result.message || 'Login failed';
        errorElement.style.display = 'block';
      }
      // Clear password field
      document.getElementById('password').value = '';
    }
  });
}

// Register Form Handler
function handleRegister(e) {
  e.preventDefault();
  
  const fullname = document.getElementById('name').value.trim();
  const email = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value.trim();
  const confirmPassword = document.getElementById('confirm-password').value.trim();
  const errorElement = document.getElementById('register-error');

  // Clear error message
  if (errorElement) {
    errorElement.textContent = '';
    errorElement.style.display = 'none';
  }

  // Validate
  if (!fullname || !email || !password || !confirmPassword) {
    if (errorElement) {
      errorElement.textContent = 'Please fill in all fields';
      errorElement.style.display = 'block';
    }
    return;
  }

  if (password !== confirmPassword) {
    if (errorElement) {
      errorElement.textContent = 'Passwords do not match';
      errorElement.style.display = 'block';
    }
    return;
  }

  if (password.length < 6) {
    if (errorElement) {
      errorElement.textContent = 'Password must be at least 6 characters';
      errorElement.style.display = 'block';
    }
    return;
  }

  // Check nếu email đã tồn tại
  if (authService.users.some(u => u.email === email)) {
    if (errorElement) {
      errorElement.textContent = 'Email already registered';
      errorElement.style.display = 'block';
    }
    return;
  }

  // Register thành công (demo: không ghi file, hướng người dùng đăng nhập)
  alert('Registration successful! Please login.');
  window.location.href = 'login.html';
}