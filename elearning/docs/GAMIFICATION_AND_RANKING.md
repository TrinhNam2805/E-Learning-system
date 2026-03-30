# Gamification & Ranking — Đặc tả cho nhóm dự án

Tài liệu này gom **một nguồn duy nhất** về: điểm bài tập, quiz ôn tập, XP, xếp hạng, và **hạng Đồng / Bạc / Vàng / …** để team thống nhất khi triển khai UI và chỉnh code.

---

## 1. Có nên chia ranking theo hạng (Đồng, Bạc, Kim cương, …)?

**Nên** — nếu mục tiêu là:

- Người học **nhìn nhanh** tiến độ (dễ marketing hơn “số XP thô”).
- Giao diện **badge / màu sắc** theo từng bậc (motivation, không thay đổi công thức XP bên dưới).

**Lưu ý:**

- **Hạng (tier)** là **lớp hiển thị** gắn với **tổng XP xếp hạng** (`RankScore`), không thay thế việc lưu XP trong DB.
- Tránh quá nhiều bậc (6–7 bậc là đủ); tên có thể song ngữ (VN + EN) cho code.

---

## 2. Hai nguồn “điểm” trong hệ thống

| Loại | Mục đích | Điểm học thuật | XP gamification (app hiện tại) |
|------|-----------|----------------|-------------------------------|
| **Bài tập thường** (HOMEWORK / EXAM) | Chấm tay, đánh giá chính | `0 … max_score` (GV nhập) | Tối đa **50 XP** mỗi bài (lần chấm đầu, `type ≠ QUIZ`) |
| **Quiz ôn tập** (QUIZ) | Trắc nghiệm, auto chấm | `0 … max_score` (theo số câu đúng) | Tối đa **40 XP** mỗi lần nộp quiz |

**Chuẩn hóa tỉ lệ** (dùng cho công thức XP, không phụ thuộc thang 10 hay 100):

\[
r = \mathrm{clip}\left(\frac{\text{điểm đạt}}{\text{max\_score}},\, 0,\, 1\right)
\]

---

## 3. Công thức XP (đồng bộ code hiện tại)

Tham chiếu: `GamificationService`, `EnrollmentService`, `AssignmentController`.

### 3.1. Bài tập thường (sau khi GV chấm)

- Chỉ cộng XP khi **lần chấm đầu tiên** và assignment **không phải QUIZ** (`AssignmentService.grade`).
- Công thức (tương đương trong code):

\[
\text{XP}_\text{hw} = \mathrm{round}\bigl(\min(1,\; r) \times 50\bigr),\quad \text{tối đa } 50
\]

### 3.2. Quiz ôn tập (nộp xong là có điểm)

- Điểm: \(\text{score} = \dfrac{\text{số câu đúng}}{\text{số câu}} \times \text{max\_score}\) (cột `quiz_questions.points` **chưa** dùng khi tính điểm trong bản hiện tại).
- XP ngay sau khi nộp:

\[
\text{XP}_\text{qz} = \mathrm{round}\bigl(\min(1,\; r) \times 40\bigr),\quad \text{tối đa } 40
\]

### 3.3. XP học bài (lesson)

- Mỗi bài học hoàn thành trong khóa: **+20 XP** (phần `lesson_xp` trong `EnrollmentService.recalcProgress`).

### 3.4. Tổng XP trên một khóa học (một `enrollment`)

\[
\text{total\_xp} = \text{lesson\_xp} + \text{activity\_xp}
\]

- `activity_xp`: cộng dồn XP từ quiz + bài được chấm (theo luồng trên).
- `lesson_xp` = `(số lesson đã hoàn thành) × 20` (trong code hiện tại).

---

## 4. Xếp hạng (leaderboard)

**Điểm dùng để xếp hạng toàn hệ thống** (sinh viên):

\[
\text{RankScore} = \sum_{\text{mọi enrollment}} \text{total\_xp}
\]

- Sắp xếp: **giảm dần** `RankScore` (`RankingService.getLeaderboard`).
- **Level (số trong app hiện tại)** — chỉ để hiển thị, có thể song song với **hạng** ở mục 5:

\[
\text{level} = \left\lfloor \frac{\text{RankScore}}{100} \right\rfloor + 1,\quad
\text{xp trong level} = \text{RankScore} \bmod 100
\]

---

## 5. Hạng Đồng → Kim cương (đề xuất tiêu chuẩn nhóm)

Gắn **một hạng** với **RankScore** (tổng XP toàn hệ thống). Ngưỡng dưới đây **đủ rộng** cho vài học kỳ hoạt động vừa phải; team có thể chỉnh sau khi có số liệu thật.

| Hạng (VN) | Tier (EN) | RankScore (XP) | Ghi chú |
|-----------|-----------|----------------|---------|
| **Đồng** | Bronze | **0 – 999** | Mới bắt đầu |
| **Bạc** | Silver | **1 000 – 2 999** | Đã tham gia đều |
| **Vàng** | Gold | **3 000 – 6 999** | Tích cực nhiều khóa |
| **Bạch kim** | Platinum | **7 000 – 14 999** | Rất tích cực |
| **Kim cương** | Diamond | **≥ 15 000** | Top cam kết |

**Quy tắc “lên hạng”:** so sánh `RankScore` với cột **Min XP** (ngưỡng dưới của bậc). Không cần đổi schema DB — có thể tính trong service hoặc Thymeleaf.

**Vì sao không gộp trùng “level = mỗi 100 XP”?**

- **Level (100 XP/level):** mịn, tăng liên tục — phù hợp thanh tiến độ.
- **Hạng (tier):** bậc **ít hơn**, khoảng cách **xa hơn** — phù hợp badge, màu avatar, không phải chỉnh mỗi 100 XP.

Hai thứ **có thể hiển thị cùng lúc**: ví dụ “Kim cương · Level 142”.

---

## 6. Quiz làm nhiều lần (khuyến nghị sản phẩm — chưa bắt buộc trong code cũ)

Để tránh “farm” XP khi cho làm lại quiz:

| Chiến lược | Mô tả |
|------------|--------|
| **A — Chỉ lần tốt nhất** | Chỉ giữ XP cao nhất / quiz / khóa (hoặc toàn hệ thống). |
| **B — Chỉ XP lần đầu** | Các lần sau không cộng. |
| **C — Giảm dần** | Lần 1: 100%, lần 2: 50%, lần 3+: 0%. |

Team chọn **một** chiến lược và ghi vào backlog triển khai trong `GamificationService` / luồng submit quiz.

---

## 7. Bảng tóm tắt file / class liên quan

| Nội dung | Vị trí |
|----------|--------|
| XP quiz / chấm bài | `GamificationService.java` |
| Cộng `activity_xp`, `total_xp` | `EnrollmentService.java` |
| Nộp quiz & bài tập | `AssignmentController.java`, `AssignmentService.java` |
| Bảng xếp hạng | `RankingService.java` |
| DDL + comment logic SQL | `db/assignments-quiz-ranking-module.sql` |

---

## 8. Checklist khi chỉnh số (cho PM / dev)

- [ ] `MAX_XP_ASSIGNMENT` / `MAX_XP_QUIZ` có đổi không? (mặc định 50 / 40)
- [ ] Ngưỡng **hạng** (Bảng mục 5) có cần scale theo số khóa / học kỳ?
- [ ] Quiz có **nhiều lần** không — đã chọn chiến lược mục 6?
- [ ] UI: hiển thị **RankScore**, **Level**, **Tier** thế nào (chỉ tier / cả ba)?

---

*Tài liệu thống nhất phiên bản nhóm — cập nhật khi đổi hằng số hoặc rule quiz nhiều lần.*
