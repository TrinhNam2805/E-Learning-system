// ===== MOCK DATA =====
const MOCK_USERS = [
  { id: 1, username: "ngoc123", email: "ngoc@example.com", password: "password123", fullName: "Nguyễn Thị Ngọc", role: "STUDENT", phone: "0901234567", active: true, locked: false, createdAt: "2026-01-15" },
  { id: 2, username: "minh456", email: "minh@example.com", password: "password456", fullName: "Trần Văn Minh", role: "STUDENT", phone: "0912345678", active: true, locked: false, createdAt: "2026-02-20" },
  { id: 3, username: "hung_teacher", email: "hung@example.com", password: "teacher123", fullName: "Võ Thanh Hùng", role: "TEACHER", phone: "0923456789", active: true, locked: false, createdAt: "2025-12-01" },
  { id: 4, username: "lan_teacher", email: "lan@example.com", password: "teacher123", fullName: "Nguyễn Thị Lan", role: "TEACHER", phone: "0934567890", active: true, locked: false, createdAt: "2025-11-15" },
  { id: 5, username: "admin", email: "admin@example.com", password: "admin123", fullName: "Quản Trị Viên", role: "ADMIN", phone: "0945678901", active: true, locked: false, createdAt: "2025-10-01" }
];

const MOCK_COURSES = [
  { id: 1, courseCode: "SE-101", courseName: "Nhập môn Kỹ nghệ phần mềm", description: "Giới thiệu các khái niệm cơ bản về kỹ nghệ phần mềm: vòng đời phần mềm, mô hình phát triển, yêu cầu phần mềm, thiết kế và kiểm thử.", teacherId: 3, enrollPassword: "join123", semester: "2026-1", academicYear: "2026", status: "PUBLISHED", maxStudents: 50, thumbnail: "images/html-css.png" },
  { id: 2, courseCode: "CS-201", courseName: "Cấu trúc dữ liệu & Giải thuật", description: "Các cấu trúc dữ liệu cơ bản: mảng, danh sách liên kết, stack, queue, cây, đồ thị. Các giải thuật sắp xếp, tìm kiếm và phân tích độ phức tạp.", teacherId: 3, enrollPassword: null, semester: "2026-1", academicYear: "2026", status: "PUBLISHED", maxStudents: 60, thumbnail: "images/python.jpg" },
  { id: 3, courseCode: "WEB-301", courseName: "Lập trình Web nâng cao", description: "HTML5, CSS3, JavaScript ES6+, React cơ bản. Xây dựng ứng dụng web responsive và tương tác.", teacherId: 4, enrollPassword: "web2026", semester: "2026-1", academicYear: "2026", status: "PUBLISHED", maxStudents: 40, thumbnail: "images/html-css.png" },
  { id: 4, courseCode: "DB-202", courseName: "Cơ sở dữ liệu", description: "Mô hình quan hệ, SQL, thiết kế CSDL, chuẩn hóa, transaction và các hệ quản trị CSDL phổ biến.", teacherId: 4, enrollPassword: null, semester: "2026-1", academicYear: "2026", status: "PUBLISHED", maxStudents: 55, thumbnail: "images/strategies.png" },
  { id: 5, courseCode: "AI-401", courseName: "Trí tuệ nhân tạo cơ bản", description: "Giới thiệu AI, machine learning, neural networks và các ứng dụng thực tế trong công nghệ thông tin.", teacherId: 3, enrollPassword: null, semester: "2026-2", academicYear: "2026", status: "DRAFT", maxStudents: 35, thumbnail: "images/python.jpg" }
];

const MOCK_LESSONS = [
  // SE-101
  { id: 1, courseId: 1, lessonOrder: 1, lessonTitle: "Giới thiệu Kỹ nghệ phần mềm", lessonContent: "Kỹ nghệ phần mềm (Software Engineering) là ngành kỹ thuật áp dụng các nguyên tắc có hệ thống để phát triển phần mềm chất lượng cao.\n\nCác chủ đề chính:\n• Định nghĩa và tầm quan trọng của SE\n• Sự khác biệt giữa lập trình và kỹ nghệ phần mềm\n• Các thách thức trong phát triển phần mềm hiện đại\n• Tổng quan về vòng đời phần mềm (SDLC)", videoUrl: null, durationMinutes: 45, published: true },
  { id: 2, courseId: 1, lessonOrder: 2, lessonTitle: "Mô hình phát triển phần mềm", lessonContent: "Các mô hình phát triển phần mềm phổ biến:\n\n1. Waterfall Model: Tuần tự, từng giai đoạn rõ ràng\n2. Agile/Scrum: Linh hoạt, lặp lại theo sprint\n3. Spiral Model: Kết hợp waterfall và prototyping\n4. V-Model: Kiểm thử song song với phát triển\n\nMỗi mô hình có ưu/nhược điểm riêng phù hợp với từng loại dự án.", videoUrl: null, durationMinutes: 60, published: true },
  { id: 3, courseId: 1, lessonOrder: 3, lessonTitle: "Phân tích và đặc tả yêu cầu", lessonContent: "Yêu cầu phần mềm là nền tảng của mọi dự án thành công.\n\nPhân loại yêu cầu:\n• Yêu cầu chức năng (Functional Requirements)\n• Yêu cầu phi chức năng (Non-functional Requirements)\n• Yêu cầu miền (Domain Requirements)\n\nKỹ thuật thu thập yêu cầu:\n• Phỏng vấn stakeholder\n• Quan sát và phân tích tài liệu\n• Use case và user story\n• Prototyping", videoUrl: null, durationMinutes: 75, published: true },
  { id: 4, courseId: 1, lessonOrder: 4, lessonTitle: "Thiết kế phần mềm", lessonContent: "Thiết kế phần mềm chuyển đổi yêu cầu thành kiến trúc hệ thống.\n\nCác cấp độ thiết kế:\n• Thiết kế kiến trúc (Architectural Design)\n• Thiết kế chi tiết (Detailed Design)\n• Thiết kế giao diện (Interface Design)\n\nCác nguyên tắc thiết kế tốt:\n• SOLID principles\n• DRY (Don't Repeat Yourself)\n• KISS (Keep It Simple, Stupid)\n• Separation of Concerns", videoUrl: null, durationMinutes: 90, published: true },
  // CS-201
  { id: 5, courseId: 2, lessonOrder: 1, lessonTitle: "Mảng và Danh sách liên kết", lessonContent: "Mảng (Array) là cấu trúc dữ liệu lưu trữ các phần tử cùng kiểu liên tiếp trong bộ nhớ.\n\nĐặc điểm:\n• Truy cập ngẫu nhiên O(1)\n• Chèn/xóa O(n)\n• Kích thước cố định\n\nDanh sách liên kết (Linked List):\n• Singly Linked List\n• Doubly Linked List\n• Circular Linked List\n\nSo sánh: Array vs Linked List về thời gian và không gian.", videoUrl: null, durationMinutes: 60, published: true },
  { id: 6, courseId: 2, lessonOrder: 2, lessonTitle: "Stack và Queue", lessonContent: "Stack (Ngăn xếp) - LIFO (Last In First Out):\n• Push: thêm phần tử vào đỉnh\n• Pop: lấy phần tử từ đỉnh\n• Peek: xem phần tử đỉnh\n• Ứng dụng: undo/redo, call stack, duyệt cây DFS\n\nQueue (Hàng đợi) - FIFO (First In First Out):\n• Enqueue: thêm vào cuối\n• Dequeue: lấy từ đầu\n• Ứng dụng: BFS, print queue, task scheduling", videoUrl: null, durationMinutes: 55, published: true },
  { id: 7, courseId: 2, lessonOrder: 3, lessonTitle: "Cây nhị phân và BST", lessonContent: "Cây (Tree) là cấu trúc dữ liệu phân cấp phi tuyến.\n\nCây nhị phân (Binary Tree):\n• Mỗi node có tối đa 2 con\n• Duyệt: Inorder, Preorder, Postorder\n\nCây nhị phân tìm kiếm (BST):\n• Node trái < Node gốc < Node phải\n• Tìm kiếm, chèn, xóa: O(log n) trung bình\n• Worst case O(n) khi cây mất cân bằng\n\nAVL Tree và Red-Black Tree: cây cân bằng tự động.", videoUrl: null, durationMinutes: 80, published: true },
  // WEB-301
  { id: 8, courseId: 3, lessonOrder: 1, lessonTitle: "HTML5 Semantic Elements", lessonContent: "HTML5 giới thiệu các thẻ semantic giúp cấu trúc trang web rõ ràng hơn.\n\nCác thẻ semantic quan trọng:\n• <header>, <footer>, <nav>\n• <main>, <section>, <article>, <aside>\n• <figure>, <figcaption>\n• <time>, <mark>, <details>\n\nLợi ích:\n• SEO tốt hơn\n• Accessibility cải thiện\n• Code dễ đọc và maintain", videoUrl: null, durationMinutes: 40, published: true },
  { id: 9, courseId: 3, lessonOrder: 2, lessonTitle: "CSS Flexbox & Grid", lessonContent: "Flexbox - Layout 1 chiều:\n• display: flex\n• flex-direction, justify-content, align-items\n• flex-wrap, flex-grow, flex-shrink\n• Phù hợp cho navigation, card layout\n\nCSS Grid - Layout 2 chiều:\n• display: grid\n• grid-template-columns/rows\n• grid-area, grid-gap\n• Phù hợp cho page layout phức tạp\n\nResponsive Design với Media Queries.", videoUrl: null, durationMinutes: 70, published: true },
  { id: 10, courseId: 3, lessonOrder: 3, lessonTitle: "JavaScript ES6+ Fundamentals", lessonContent: "ES6+ mang lại nhiều tính năng mạnh mẽ cho JavaScript:\n\n• let/const thay var\n• Arrow functions: () => {}\n• Template literals: `Hello ${name}`\n• Destructuring: const {a, b} = obj\n• Spread/Rest operator: ...args\n• Promises và async/await\n• Modules: import/export\n• Classes và inheritance\n\nThực hành: xây dựng todo app với ES6+.", videoUrl: null, durationMinutes: 90, published: true },
  // DB-202
  { id: 11, courseId: 4, lessonOrder: 1, lessonTitle: "Mô hình quan hệ và SQL cơ bản", lessonContent: "Mô hình quan hệ (Relational Model):\n• Bảng (Table/Relation)\n• Thuộc tính (Attribute/Column)\n• Bộ (Tuple/Row)\n• Khóa chính (Primary Key)\n• Khóa ngoại (Foreign Key)\n\nSQL cơ bản:\n• SELECT, FROM, WHERE\n• INSERT, UPDATE, DELETE\n• ORDER BY, GROUP BY, HAVING\n• JOIN: INNER, LEFT, RIGHT, FULL", videoUrl: null, durationMinutes: 75, published: true },
  { id: 12, courseId: 4, lessonOrder: 2, lessonTitle: "Thiết kế CSDL và Chuẩn hóa", lessonContent: "Thiết kế CSDL tốt giúp tránh dư thừa và bất nhất dữ liệu.\n\nCác dạng chuẩn:\n• 1NF: Loại bỏ nhóm lặp\n• 2NF: Loại bỏ phụ thuộc bộ phận\n• 3NF: Loại bỏ phụ thuộc bắc cầu\n• BCNF: Dạng chuẩn Boyce-Codd\n\nQuy trình thiết kế:\n1. Thu thập yêu cầu\n2. Vẽ ERD\n3. Chuyển sang mô hình quan hệ\n4. Chuẩn hóa\n5. Tối ưu hóa", videoUrl: null, durationMinutes: 85, published: true }
];

const MOCK_ENROLLMENTS = [
  { id: 1, studentId: 1, courseId: 1, enrolledAt: "2026-01-20", status: "ACTIVE", progressPercentage: 75, totalXp: 150 },
  { id: 2, studentId: 1, courseId: 2, enrolledAt: "2026-01-22", status: "ACTIVE", progressPercentage: 33, totalXp: 60 },
  { id: 3, studentId: 1, courseId: 3, enrolledAt: "2026-02-01", status: "ACTIVE", progressPercentage: 66, totalXp: 120 },
  { id: 4, studentId: 2, courseId: 1, enrolledAt: "2026-01-25", status: "ACTIVE", progressPercentage: 50, totalXp: 100 },
  { id: 5, studentId: 2, courseId: 4, enrolledAt: "2026-02-10", status: "ACTIVE", progressPercentage: 50, totalXp: 80 }
];

const MOCK_LESSON_PROGRESS = [
  { id: 1, studentId: 1, lessonId: 1, completed: true, completedAt: "2026-01-21" },
  { id: 2, studentId: 1, lessonId: 2, completed: true, completedAt: "2026-01-23" },
  { id: 3, studentId: 1, lessonId: 3, completed: true, completedAt: "2026-01-28" },
  { id: 4, studentId: 1, lessonId: 4, completed: false, completedAt: null },
  { id: 5, studentId: 1, lessonId: 5, completed: true, completedAt: "2026-01-24" },
  { id: 6, studentId: 1, lessonId: 6, completed: false, completedAt: null },
  { id: 7, studentId: 1, lessonId: 8, completed: true, completedAt: "2026-02-03" },
  { id: 8, studentId: 1, lessonId: 9, completed: true, completedAt: "2026-02-05" },
  { id: 9, studentId: 1, lessonId: 10, completed: false, completedAt: null },
  { id: 10, studentId: 2, lessonId: 1, completed: true, completedAt: "2026-01-26" },
  { id: 11, studentId: 2, lessonId: 2, completed: true, completedAt: "2026-01-28" },
  { id: 12, studentId: 2, lessonId: 11, completed: true, completedAt: "2026-02-12" }
];

const MOCK_FORUM_POSTS = [
  { id: 1, courseId: 1, authorId: 1, title: "Hỏi về mô hình Waterfall vs Agile", content: "Cho mình hỏi khi nào nên dùng Waterfall, khi nào nên dùng Agile? Dự án nhỏ thì dùng cái nào phù hợp hơn ạ?", postType: "QUESTION", createdAt: "2026-01-25", viewCount: 24 },
  { id: 2, courseId: 1, authorId: 3, title: "Thông báo: Lịch kiểm tra giữa kỳ", content: "Các em chú ý: Kiểm tra giữa kỳ sẽ diễn ra vào tuần 8. Nội dung từ bài 1 đến bài 4. Hình thức: trắc nghiệm 30 câu + tự luận 2 câu.", postType: "ANNOUNCEMENT", createdAt: "2026-02-01", viewCount: 87 },
  { id: 3, courseId: 2, authorId: 2, title: "Không hiểu phần cây AVL", content: "Mình đọc bài về AVL Tree nhưng không hiểu tại sao cần rotation. Ai giải thích giúp mình với?", postType: "QUESTION", createdAt: "2026-02-05", viewCount: 15 },
  { id: 4, courseId: 3, authorId: 1, title: "Chia sẻ project CSS Grid hay", content: "Mình vừa làm xong một layout dùng CSS Grid khá đẹp, chia sẻ cho mọi người tham khảo nhé!", postType: "DISCUSSION", createdAt: "2026-02-08", viewCount: 32 }
];

const MOCK_NOTIFICATIONS = [
  { id: 1, userId: 1, title: "Thông báo mới từ SE-101", message: "Giảng viên vừa đăng thông báo về lịch kiểm tra giữa kỳ.", type: "ANNOUNCEMENT", isRead: false, createdAt: "2026-02-01" },
  { id: 2, userId: 1, title: "Bạn đã hoàn thành bài học", message: "Chúc mừng! Bạn đã hoàn thành bài 3: Phân tích và đặc tả yêu cầu.", type: "BADGE", isRead: false, createdAt: "2026-01-28" },
  { id: 3, userId: 1, title: "Có câu trả lời mới", message: "Giảng viên đã trả lời câu hỏi của bạn trong diễn đàn SE-101.", type: "COMMENT", isRead: true, createdAt: "2026-01-26" },
  { id: 4, userId: 2, title: "Đăng ký khóa học thành công", message: "Bạn đã đăng ký thành công khóa học Cơ sở dữ liệu.", type: "SYSTEM", isRead: false, createdAt: "2026-02-10" }
];

// Assignments / deadlines (dùng cho calendar)
const MOCK_ASSIGNMENTS = [
  { id: 1, courseId: 1, title: "Bài tập: Vẽ sơ đồ Use Case", type: "HOMEWORK", dueDate: "2026-03-05", maxScore: 10 },
  { id: 2, courseId: 1, title: "Quiz giữa kỳ – SE-101", type: "QUIZ", dueDate: "2026-03-12", maxScore: 10 },
  { id: 3, courseId: 1, title: "Kiểm tra cuối kỳ – SE-101", type: "EXAM", dueDate: "2026-04-20", maxScore: 10 },
  { id: 4, courseId: 2, title: "Bài tập: Cài đặt Linked List", type: "HOMEWORK", dueDate: "2026-03-08", maxScore: 10 },
  { id: 5, courseId: 2, title: "Quiz: Stack & Queue", type: "QUIZ", dueDate: "2026-03-19", maxScore: 10 },
  { id: 6, courseId: 3, title: "Bài tập: Responsive Layout", type: "HOMEWORK", dueDate: "2026-03-10", maxScore: 10 },
  { id: 7, courseId: 3, title: "Quiz: JavaScript ES6", type: "QUIZ", dueDate: "2026-03-22", maxScore: 10 },
  { id: 8, courseId: 4, title: "Bài tập: Thiết kế ERD", type: "HOMEWORK", dueDate: "2026-03-15", maxScore: 10 },
  { id: 9, courseId: 4, title: "Kiểm tra giữa kỳ – DB-202", type: "EXAM", dueDate: "2026-03-28", maxScore: 10 }
];

// Notes (ghi chú của sinh viên trên bài giảng)
const MOCK_NOTES = [
  { id: 1, studentId: 1, lessonId: 1, courseId: 1, content: "SDLC gồm 6 giai đoạn: Planning → Analysis → Design → Implementation → Testing → Maintenance", highlightColor: "#FFFF00", createdAt: "2026-01-21" },
  { id: 2, studentId: 1, lessonId: 2, courseId: 1, content: "Agile phù hợp dự án yêu cầu thay đổi liên tục, Waterfall phù hợp khi yêu cầu rõ ràng từ đầu", highlightColor: "#90EE90", createdAt: "2026-01-23" },
  { id: 3, studentId: 1, lessonId: 5, courseId: 2, content: "Array O(1) random access, LinkedList O(n) – nhớ kỹ khi so sánh!", highlightColor: "#FFB6C1", createdAt: "2026-01-24" },
  { id: 4, studentId: 1, lessonId: 8, courseId: 3, content: "Semantic HTML giúp SEO và accessibility tốt hơn rất nhiều", highlightColor: "#FFFF00", createdAt: "2026-02-03" }
];

// Rankings (XP leaderboard) — chỉ dùng userId có trong MOCK_USERS
const MOCK_RANKINGS = [
  { userId: 1, totalXp: 330, rank: 1 },
  { userId: 2, totalXp: 180, rank: 2 },
  { userId: 3, totalXp: 120, rank: 3 },
  { userId: 4, totalXp: 90,  rank: 4 }
];

// ===== STATE HELPERS =====
const DB = {
  getUsers: () => JSON.parse(localStorage.getItem('db_users') || JSON.stringify(MOCK_USERS)),
  getCourses: () => MOCK_COURSES,
  getLessons: () => MOCK_LESSONS,
  getEnrollments: () => JSON.parse(localStorage.getItem('db_enrollments') || JSON.stringify(MOCK_ENROLLMENTS)),
  getProgress: () => JSON.parse(localStorage.getItem('db_progress') || JSON.stringify(MOCK_LESSON_PROGRESS)),
  getPosts: () => JSON.parse(localStorage.getItem('db_posts') || JSON.stringify(MOCK_FORUM_POSTS)),
  getNotifications: () => JSON.parse(localStorage.getItem('db_notifications') || JSON.stringify(MOCK_NOTIFICATIONS)),

  saveEnrollments: (data) => localStorage.setItem('db_enrollments', JSON.stringify(data)),
  saveProgress: (data) => localStorage.setItem('db_progress', JSON.stringify(data)),
  savePosts: (data) => localStorage.setItem('db_posts', JSON.stringify(data)),
  saveNotifications: (data) => localStorage.setItem('db_notifications', JSON.stringify(data)),
  saveUsers: (data) => localStorage.setItem('db_users', JSON.stringify(data)),

  getCurrentUser: () => JSON.parse(localStorage.getItem('currentUser') || 'null'),
  isLoggedIn: () => !!localStorage.getItem('currentUser'),

  login(email, password) {
    const users = this.getUsers();
    const user = users.find(u => u.email === email && u.password === password);
    if (!user) return null;
    if (user.locked) return 'locked';
    const session = { id: user.id, email: user.email, fullName: user.fullName, role: user.role, username: user.username };
    localStorage.setItem('currentUser', JSON.stringify(session));
    return session;
  },

  logout() { localStorage.removeItem('currentUser'); },

  register(fullName, email, password) {
    const users = this.getUsers();
    if (users.find(u => u.email === email)) return false;
    const newUser = { id: Date.now(), username: email.split('@')[0], email, password, fullName, role: 'STUDENT', active: true, locked: false, createdAt: new Date().toISOString().split('T')[0] };
    users.push(newUser);
    this.saveUsers(users);
    return newUser;
  },

  isEnrolled(studentId, courseId) {
    return this.getEnrollments().some(e => e.studentId === studentId && e.courseId === courseId);
  },

  enroll(studentId, courseId) {
    const enrollments = this.getEnrollments();
    if (this.isEnrolled(studentId, courseId)) return false;
    enrollments.push({ id: Date.now(), studentId, courseId, enrolledAt: new Date().toISOString().split('T')[0], status: 'ACTIVE', progressPercentage: 0, totalXp: 0 });
    this.saveEnrollments(enrollments);
    return true;
  },

  markLessonComplete(studentId, lessonId) {
    const progress = this.getProgress();
    const existing = progress.find(p => p.studentId === studentId && p.lessonId === lessonId);
    if (existing) { existing.completed = true; existing.completedAt = new Date().toISOString().split('T')[0]; }
    else progress.push({ id: Date.now(), studentId, lessonId, completed: true, completedAt: new Date().toISOString().split('T')[0] });
    this.saveProgress(progress);
    this.recalcProgress(studentId, lessonId);
  },

  recalcProgress(studentId, lessonId) {
    const lesson = MOCK_LESSONS.find(l => l.id === lessonId);
    if (!lesson) return;
    const enrollments = this.getEnrollments();
    const enrollment = enrollments.find(e => e.studentId === studentId && e.courseId === lesson.courseId);
    if (!enrollment) return;
    const courseLessons = MOCK_LESSONS.filter(l => l.courseId === lesson.courseId);
    const progress = this.getProgress();
    const completed = courseLessons.filter(l => progress.some(p => p.studentId === studentId && p.lessonId === l.id && p.completed)).length;
    enrollment.progressPercentage = Math.round((completed / courseLessons.length) * 100);
    enrollment.totalXp = completed * 20;
    this.saveEnrollments(enrollments);
  },

  getLessonCompleted(studentId, lessonId) {
    return this.getProgress().some(p => p.studentId === studentId && p.lessonId === lessonId && p.completed);
  },

  getUnreadCount(userId) {
    return this.getNotifications().filter(n => n.userId === userId && !n.isRead).length;
  },

  markAllRead(userId) {
    const notifs = this.getNotifications();
    notifs.filter(n => n.userId === userId).forEach(n => n.isRead = true);
    this.saveNotifications(notifs);
  },

  getAssignments: () => MOCK_ASSIGNMENTS,

  getNotes: () => JSON.parse(localStorage.getItem('db_notes') || JSON.stringify(MOCK_NOTES)),
  saveNotes: (data) => localStorage.setItem('db_notes', JSON.stringify(data)),

  getRankings() {
    // Recompute from live enrollments so XP changes reflect
    const enrollments = this.getEnrollments();
    const users = this.getUsers();
    const xpMap = {};
    enrollments.forEach(e => { xpMap[e.studentId] = (xpMap[e.studentId] || 0) + e.totalXp; });
    // Seed with mock rankings for users with no enrollments yet
    MOCK_RANKINGS.forEach(r => {
      const exists = users.find(u => u.id === r.userId);
      if (exists && !xpMap[r.userId]) xpMap[r.userId] = r.totalXp;
    });
    return Object.entries(xpMap)
      .map(([uid, xp]) => {
        const u = users.find(u => u.id === parseInt(uid));
        if (!u) return null; // skip ghost users
        return { userId: parseInt(uid), fullName: u.fullName, totalXp: xp };
      })
      .filter(Boolean)
      .sort((a, b) => b.totalXp - a.totalXp)
      .map((r, i) => ({ ...r, rank: i + 1 }));
  },

  updateProfile(userId, data) {
    const users = this.getUsers();
    const user = users.find(u => u.id === userId);
    if (!user) return false;
    if (data.fullName) user.fullName = data.fullName;
    if (data.phone !== undefined) user.phone = data.phone;
    this.saveUsers(users);
    // Update session
    const session = this.getCurrentUser();
    if (session && session.id === userId) {
      if (data.fullName) session.fullName = data.fullName;
      localStorage.setItem('currentUser', JSON.stringify(session));
    }
    return user;
  },

  // Deadlines for a user (from enrolled courses)
  getDeadlines(userId) {
    const enrollments = this.getEnrollments().filter(e => e.studentId === userId);
    const courseIds = enrollments.map(e => e.courseId);
    return MOCK_ASSIGNMENTS.filter(a => courseIds.includes(a.courseId))
      .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate));
  }
};
