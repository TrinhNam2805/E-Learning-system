# Triển khai CSDL & dữ liệu (E-Learning IT)

## Thứ tự bước (database mới)

1. Tạo database (một lần):

```bash
mysql -u root -p --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS \`e-learning\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

2. Áp dụng lược đồ đầy đủ:

```bash
mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/schema-standard.sql
```

3. Nếu dùng bản **demo không sinh viên mẫu** (chỉ GV + Admin + nội dung môn học):

```bash
mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/seed-academic-demo.sql
```

4. Nếu cần **bộ dữ liệu đầy đủ có sinh viên / nộp bài / ghi chú mẫu** (để test nhanh):

```bash
mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/seed-data.sql
```

5. Cấu hình `application.properties`: `spring.datasource.url`, user/password MySQL, `app.base-url` (link reset mật khẩu).

## Database đã có sẵn (chỉ cần bổ sung cột)

Chạy lần lượt các migration (đọc nội dung file trước khi chạy production):

- `elearning/db/migration-password-reset.sql` — reset mật khẩu qua token
- `elearning/db/migration-activity-xp-notes.sql` — `activity_xp` trên `enrollments`, ghi chú nâng cao, bảng `note_links`

## Tài khoản sau seed-academic-demo

| Email | Vai trò | Mật khẩu (demo) |
|-------|---------|-----------------|
| teacher@cntt.edu.vn | TEACHER | Demo@2024 |
| admin@cntt.edu.vn | ADMIN | Demo@2024 |

Sinh viên / khách: **Đăng ký** tại `/register`, sau đó đăng nhập và **Enroll** khóa học (mật lớp trong trang chi tiết khóa học nếu có).

## File SQL trong `elearning/db/`

| File | Mục đích |
|------|----------|
| `schema-standard.sql` | DDL đầy đủ (ERD thực thi) |
| `migration-*.sql` | ALTER cho DB cũ |
| `seed-academic-demo.sql` | Nội dung môn + 1 GV + 1 Admin, không SV |
| `seed-data.sql` | Bộ đầy đủ có SV mẫu + nộp bài + ghi chú + link |
