# Design patterns & kiến trúc (dự án E-Learning)

## MVC (Model–View–Controller)

- **Model:** `com.elearning.model.entity` — thực thể JPA map bảng SQL; đại diện trạng thái nghiệp vụ và dữ liệu.
- **View:** `src/main/resources/templates` (Thymeleaf) — HTML, không truy vấn DB trực tiếp.
- **Controller:** `com.elearning.controller` — nhận HTTP, gọi service, đổ dữ liệu vào `Model`, trả view hoặc redirect.

Luồng chuẩn: **Controller → Service → Repository → DB**.

## Layered / Layered architecture

- **Repository** (`*Repository`): trừu tượng truy cập dữ liệu (Spring Data JPA).
- **Service** (`*Service`): quy tắc nghiệp vụ, giao dịch (`@Transactional`), orchestration.
- **Controller:** điều phối HTTP, không chứa SQL.

## Dependency Injection (IoC)

- Spring inject constructor (Lombok `@RequiredArgsConstructor`) — phụ thuộc vào interface/abstraction của framework (repository, service).

## Repository pattern

- Một bảng/aggregate chính → một `JpaRepository`; truy vấn đặt tên hoặc `@Query`.

## Strategy (gợi ý mở rộng)

- **Gamification:** `GamificationService` gom quy tắc cộng XP (quiz vs bài chấm tay) — có thể thay thế thuật toán mà không đổi controller.
- **Bài tập:** `Assignment.AssignmentType` (QUIZ / HOMEWORK / EXAM) — nhánh xử lý khác nhau trong controller/service (quiz tự chấm vs chấm thủ công).

## Template Method / lifecycle hooks

- JPA `@PrePersist` / `@PreUpdate` trên entity (ví dụ ghi `createdAt`).

## Front Controller

- `DispatcherServlet` (Spring MVC) — một điểm vào, ánh xạ tới controller theo URL.

## Điểm nhấn nghiệp vụ đã gắn với thiết kế

| Chức năng | Lớp chính | Ghi chú |
|-----------|-----------|---------|
| Làm quiz / nộp bài | `AssignmentController`, `AssignmentService`, `GamificationService` | Quiz cộng XP qua `GamificationService`; chấm tay cộng XP lần đầu khi `grade()`. |
| Tiến độ + XP bài học | `EnrollmentService.recalcProgress` | `total_xp = (số bài đã học × 20) + activity_xp`. |
| Xếp hạng | `RankingService` | Cộng `total_xp` theo các enrollment của sinh viên. |
| Ghi chú & liên kết | `NoteService`, `NoteLink`, `NoteController` | Trích dẫn nguồn (`source_excerpt`), tag, đồ thị liên kết (`note_links`). |

## REST API và MVC

- Hiện tại ứng dụng dùng **MVC server-rendered**. Có thể **thêm song song** các lớp `@RestController` trả JSON (`/api/...`) cho SPA/mobile mà **không thay thế** Thymeleaf — cùng service/repository.
