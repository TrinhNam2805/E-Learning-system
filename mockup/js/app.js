// ===== SHARED APP HELPERS =====

// Render nav based on login state
function renderNav(activePage) {
  const user = DB.getCurrentUser();
  const unread = user ? DB.getUnreadCount(user.id) : 0;
  const nav = document.getElementById('top-nav');
  if (!nav) return;

  const logoHref = 'homepage.html';
  let links = '';

  if (!user) {
    links = `
      <a href="courses.html">Khóa học</a>
      <a href="login.html">Đăng nhập</a>
      <a href="register.html">Đăng ký</a>`;
  } else {
    const dashHref = user.role === 'ADMIN' ? 'admin.html' : 'student-dashboard.html';
    links = `
      <a href="courses.html">Khóa học</a>
      <a href="${dashHref}">Dashboard</a>
      ${user.role !== 'ADMIN' ? '<a href="notes.html">📓 Ghi chú</a>' : ''}
      <div class="account-menu">
        <button class="icon-btn notification-btn" onclick="toggleNotif()" title="Thông báo">
          🔔${unread > 0 ? `<span class="notif-badge">${unread}</span>` : ''}
        </button>
        <div id="notif-dropdown" class="dropdown-menu" style="display:none; min-width:300px; right:0;">
          <div style="padding:0.75rem 1rem; font-weight:600; border-bottom:1px solid #eee;">Thông báo</div>
          ${DB.getNotifications().filter(n => n.userId === user.id).slice(0,4).map(n => `
            <div class="dropdown-item ${n.isRead ? '' : 'notif-unread'}">
              <div style="font-weight:${n.isRead ? '400' : '600'}; font-size:0.9rem;">${n.title}</div>
              <div style="font-size:0.82rem; color:#666; margin-top:2px;">${n.message}</div>
            </div>`).join('')}
          <a href="notifications.html" class="dropdown-item" style="text-align:center; color:#0a66c2; font-size:0.88rem;">Xem tất cả</a>
        </div>
      </div>
      <div class="account-menu">
        <button class="icon-btn account-btn" onclick="toggleAccount()" title="Tài khoản">
          👤 <span style="font-size:0.88rem; font-weight:600;">${user.fullName.split(' ').pop()}</span>
        </button>
        <div id="account-dropdown" class="dropdown-menu" style="display:none;">
          <div style="padding:0.75rem 1rem; border-bottom:1px solid #eee;">
            <div style="font-weight:600;">${user.fullName}</div>
            <div style="font-size:0.82rem; color:#888;">${user.role}</div>
          </div>
          <a href="profile.html" class="dropdown-item">👤 Hồ sơ cá nhân</a>
          <a href="${dashHref}" class="dropdown-item">📊 Dashboard</a>
          <a href="change-password.html" class="dropdown-item">🔐 Đổi mật khẩu</a>
          <hr style="margin:0.25rem 0; border:none; border-top:1px solid #eee;">
          <a href="#" class="dropdown-item logout-item" onclick="doLogout()">🚪 Đăng xuất</a>
        </div>
      </div>`;
  }

  nav.innerHTML = `
    <div class="logo"><a href="${logoHref}">E-Learning CNTT</a></div>
    <div class="nav-toggle" onclick="this.closest('nav').classList.toggle('open')">&#9776;</div>
    <div class="links">${links}</div>`;
}

function toggleNotif() {
  const d = document.getElementById('notif-dropdown');
  const a = document.getElementById('account-dropdown');
  if (a) a.style.display = 'none';
  if (d) d.style.display = d.style.display === 'none' ? 'block' : 'none';
}

function toggleAccount() {
  const d = document.getElementById('account-dropdown');
  const n = document.getElementById('notif-dropdown');
  if (n) n.style.display = 'none';
  if (d) d.style.display = d.style.display === 'none' ? 'block' : 'none';
}

document.addEventListener('click', e => {
  if (!e.target.closest('.account-menu')) {
    document.querySelectorAll('.dropdown-menu').forEach(m => m.style.display = 'none');
  }
});

function doLogout() {
  DB.logout();
  window.location.href = 'login.html';
}

function requireLogin() {
  if (!DB.isLoggedIn()) { window.location.href = 'login.html'; return false; }
  return true;
}

function requireRole(role) {
  const user = DB.getCurrentUser();
  if (!user || user.role !== role) { window.location.href = 'homepage.html'; return false; }
  return true;
}

function showToast(msg, type = 'success') {
  const t = document.createElement('div');
  t.className = `toast toast-${type}`;
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.classList.add('show'), 10);
  setTimeout(() => { t.classList.remove('show'); setTimeout(() => t.remove(), 400); }, 3500);
}

function getParam(name) {
  return new URLSearchParams(window.location.search).get(name);
}

function formatDate(str) {
  if (!str) return '';
  return new Date(str).toLocaleDateString('vi-VN');
}
