# Trien khai CSDL va du lieu (E-Learning IT)

## Thu tu buoc (database moi)

1. Tao database:

```bash
mysql -u root -p --default-character-set=utf8mb4 -e "CREATE DATABASE IF NOT EXISTS \`e-learning\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

2. Ap dung schema day du:

```bash
mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/schema-standard.sql
```

3. Neu can ban demo khong co sinh vien mau, nhung van co day du 8 khoa hoc CNTT, 12 bai hoc moi khoa, quiz, homework va forum:

```bash
mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/seed-academic-demo.sql
```

4. Neu can bo du lieu day du de test nhanh giao dien va chuc nang hoc tap:

```bash
mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/seed-data.sql
```

5. Cau hinh `application.properties`: `spring.datasource.url`, user/password MySQL, `app.base-url`.

## Database da co san

Chay lan luot cac migration can thiet (doc file truoc khi chay production):

- `elearning/db/migration-password-reset.sql`
- `elearning/db/migration-activity-xp-notes.sql`
- `elearning/db/migration-lesson-sequential-unlock.sql`

## Tai khoan sau seed-academic-demo

| Email | Vai tro | Mat khau demo |
|-------|---------|----------------|
| `teacher@cntt.edu.vn` | `TEACHER` | `Demo@2024` |
| `bich.tran@cntt.edu.vn` | `TEACHER` | `Demo@2024` |
| `quang.le@cntt.edu.vn` | `TEACHER` | `Demo@2024` |
| `admin@cntt.edu.vn` | `ADMIN` | `Demo@2024` |

Sinh vien co the dang ky tai `/register`, sau do dang nhap va enroll vao khoa hoc.

## Bo du lieu moi

`seed-data.sql` hien tai tao bo du lieu tien trinh tu thap den cao gom 8 khoa hoc:

1. `ITF101` - Nhap mon CNTT va ky nang so
2. `PRG101` - Tu duy lap trinh voi Python
3. `DBI201` - Co so du lieu va SQL thuc hanh
4. `WEB201` - Phat trien giao dien web voi HTML CSS JavaScript
5. `OOP201` - Lap trinh huong doi tuong voi Java
6. `DSA201` - Cau truc du lieu va giai thuat
7. `API301` - Xay dung backend voi Spring Boot
8. `DEV301` - DevOps va trien khai ung dung

Moi khoa hoc co 12 bai hoc. Moi bai hoc co quiz mo khoa bai tiep theo va homework de nop tep. Bo full seed cung kem forum, notes, submissions, notifications va badge mau.

## File SQL trong `elearning/db/`

| File | Muc dich |
|------|----------|
| `schema-standard.sql` | DDL day du |
| `migration-*.sql` | ALTER cho DB cu |
| `seed-academic-demo.sql` | Ban demo 8 khoa hoc, khong co sinh vien/enrollment/note/submission |
| `seed-data.sql` | Ban day du co sinh vien, progress, note, forum, submission, badge |
| `seed-quiz-content-sample.sql` | Them quiz test bo sung cho bo course moi |
| `seed-file-upload-assignment-sample.sql` | Them homework upload bo sung |
| `seed-assessment-demo.sql` | Them lich su nop bai, cham diem va notification de demo assessment |
