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

3. Nap bo du lieu day du de test nhanh giao dien va chuc nang hoc tap:

```bash
mysql -u root -p --default-character-set=utf8mb4 e-learning < elearning/db/seed-data.sql
```

4. Neu muon tao database moi va nap schema + seed chi trong 1 lan chay:

```bash
mysql -u root -p --default-character-set=utf8mb4 < elearning/db/bootstrap-full-demo.sql
```

5. Cau hinh `application.properties`: `spring.datasource.url`, user/password MySQL, `app.base-url`.

## Tai khoan sau seed-data

| Email | Vai tro | Mat khau demo |
|-------|---------|----------------|
| `minh.chau@student.cntt.edu.vn` | `STUDENT` | `Demo@2024` |
| `thu.dung@student.cntt.edu.vn` | `STUDENT` | `Demo@2024` |
| `van.giang@student.cntt.edu.vn` | `STUDENT` | `Demo@2024` |
| `hoang.nam@student.cntt.edu.vn` | `STUDENT` | `Demo@2024` |
| `ngoc.mai@student.cntt.edu.vn` | `STUDENT` | `Demo@2024` |
| `quoc.bao@student.cntt.edu.vn` | `STUDENT` | `Demo@2024` |

Du an hien duoc gioi han trong pham vi `student-only`, vi vay bo seed chi tao tai khoan sinh vien.

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
| `seed-data.sql` | Ban day du co sinh vien, progress, note, forum, submission, badge |
| `bootstrap-full-demo.sql` | Tao database moi, tao schema va nap seed trong 1 lan chay |

