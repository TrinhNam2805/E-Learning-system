package com.elearning.config;

import com.elearning.model.entity.*;
import com.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository progressRepository;
    private final NotificationRepository notificationRepository;
    private final AssignmentRepository assignmentRepository;
    private final NoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized, skipping seed data.");
            return;
        }
        log.info("Seeding initial data...");

        // Users
        User student1 = userRepository.save(User.builder()
                .username("alice").email("alice@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Alice Johnson").role(User.Role.STUDENT).phone("555-0101").build());

        User student2 = userRepository.save(User.builder()
                .username("bob").email("bob@example.com")
                .password(passwordEncoder.encode("password456"))
                .fullName("Bob Smith").role(User.Role.STUDENT).phone("555-0102").build());

        User teacher1 = userRepository.save(User.builder()
                .username("prof_james").email("james@example.com")
                .password(passwordEncoder.encode("teacher123"))
                .fullName("James Carter").role(User.Role.TEACHER).phone("555-0201").build());

        User teacher2 = userRepository.save(User.builder()
                .username("prof_linda").email("linda@example.com")
                .password(passwordEncoder.encode("teacher123"))
                .fullName("Linda Park").role(User.Role.TEACHER).phone("555-0202").build());

        userRepository.save(User.builder()
                .username("admin").email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("System Admin").role(User.Role.ADMIN).phone("555-0001").build());

        // Courses
        Course c1 = courseRepository.save(Course.builder()
                .courseCode("SE-101").courseName("Introduction to Software Engineering")
                .description("Covers fundamental concepts of software engineering: software lifecycle, development models, requirements analysis, design, and testing.")
                .teacher(teacher1).enrollPassword("join123").semester("2026-1").academicYear("2026")
                .status(Course.Status.PUBLISHED).maxStudents(50).thumbnail("/images/html-css.png").build());

        Course c2 = courseRepository.save(Course.builder()
                .courseCode("CS-201").courseName("Data Structures & Algorithms")
                .description("Core data structures: arrays, linked lists, stacks, queues, trees, graphs. Sorting and searching algorithms with complexity analysis.")
                .teacher(teacher1).semester("2026-1").academicYear("2026")
                .status(Course.Status.PUBLISHED).maxStudents(60).thumbnail("/images/python.jpg").build());

        Course c3 = courseRepository.save(Course.builder()
                .courseCode("WEB-301").courseName("Advanced Web Development")
                .description("HTML5, CSS3, JavaScript ES6+, and React basics. Build responsive and interactive web applications.")
                .teacher(teacher2).enrollPassword("web2026").semester("2026-1").academicYear("2026")
                .status(Course.Status.PUBLISHED).maxStudents(40).thumbnail("/images/html-css.png").build());

        Course c4 = courseRepository.save(Course.builder()
                .courseCode("DB-202").courseName("Database Systems")
                .description("Relational model, SQL, database design, normalization, transactions, and popular database management systems.")
                .teacher(teacher2).semester("2026-1").academicYear("2026")
                .status(Course.Status.PUBLISHED).maxStudents(55).thumbnail("/images/strategies.png").build());

        // Lessons for SE-101
        Lesson l1 = lessonRepository.save(Lesson.builder().course(c1).lessonOrder(1)
                .lessonTitle("Introduction to Software Engineering").durationMinutes(45)
                .lessonContent("Software Engineering applies systematic principles to develop high-quality software.\n\nKey topics:\n- Definition and importance of SE\n- Difference between programming and software engineering\n- Challenges in modern software development\n- Overview of the Software Development Lifecycle (SDLC)").build());

        Lesson l2 = lessonRepository.save(Lesson.builder().course(c1).lessonOrder(2)
                .lessonTitle("Software Development Models").durationMinutes(60)
                .lessonContent("Common software development models:\n\n1. Waterfall Model: Sequential, clearly defined phases\n2. Agile/Scrum: Flexible, iterative sprints\n3. Spiral Model: Combines waterfall and prototyping\n4. V-Model: Testing runs parallel to development\n\nEach model has pros and cons suited to different project types.").build());

        Lesson l3 = lessonRepository.save(Lesson.builder().course(c1).lessonOrder(3)
                .lessonTitle("Requirements Analysis & Specification").durationMinutes(75)
                .lessonContent("Requirements are the foundation of every successful project.\n\nTypes of requirements:\n- Functional Requirements\n- Non-functional Requirements\n- Domain Requirements\n\nElicitation techniques:\n- Stakeholder interviews\n- Observation and document analysis\n- Use cases and user stories\n- Prototyping").build());

        Lesson l4 = lessonRepository.save(Lesson.builder().course(c1).lessonOrder(4)
                .lessonTitle("Software Design").durationMinutes(90)
                .lessonContent("Software design transforms requirements into a system architecture.\n\nDesign levels:\n- Architectural Design\n- Detailed Design\n- Interface Design\n\nGood design principles:\n- SOLID principles\n- DRY (Don't Repeat Yourself)\n- KISS (Keep It Simple, Stupid)\n- Separation of Concerns").build());

        // Lessons for CS-201
        Lesson l5 = lessonRepository.save(Lesson.builder().course(c2).lessonOrder(1)
                .lessonTitle("Arrays and Linked Lists").durationMinutes(60)
                .lessonContent("Arrays store elements of the same type in contiguous memory.\n\nCharacteristics:\n- Random access O(1)\n- Insert/delete O(n)\n- Fixed size\n\nLinked Lists:\n- Singly Linked List\n- Doubly Linked List\n- Circular Linked List").build());

        Lesson l6 = lessonRepository.save(Lesson.builder().course(c2).lessonOrder(2)
                .lessonTitle("Stacks and Queues").durationMinutes(55)
                .lessonContent("Stack - LIFO (Last In First Out):\n- Push: add to top\n- Pop: remove from top\n- Use cases: undo/redo, call stack, DFS traversal\n\nQueue - FIFO (First In First Out):\n- Enqueue: add to back\n- Dequeue: remove from front\n- Use cases: BFS, print queue, task scheduling").build());

        Lesson l7 = lessonRepository.save(Lesson.builder().course(c2).lessonOrder(3)
                .lessonTitle("Binary Trees and BST").durationMinutes(80)
                .lessonContent("Trees are non-linear hierarchical data structures.\n\nBinary Tree:\n- Each node has at most 2 children\n- Traversals: Inorder, Preorder, Postorder\n\nBinary Search Tree (BST):\n- Left < Root < Right\n- Search, insert, delete: O(log n) average").build());

        // Lessons for WEB-301
        Lesson l8 = lessonRepository.save(Lesson.builder().course(c3).lessonOrder(1)
                .lessonTitle("HTML5 Semantic Elements").durationMinutes(40)
                .lessonContent("HTML5 introduces semantic tags for clearer page structure.\n\nKey semantic tags:\n- <header>, <footer>, <nav>\n- <main>, <section>, <article>, <aside>\n- <figure>, <figcaption>\n\nBenefits:\n- Better SEO\n- Improved accessibility\n- Easier to read and maintain").build());

        Lesson l9 = lessonRepository.save(Lesson.builder().course(c3).lessonOrder(2)
                .lessonTitle("CSS Flexbox & Grid").durationMinutes(70)
                .lessonContent("Flexbox - 1D layout:\n- display: flex\n- flex-direction, justify-content, align-items\n- Great for navigation, card layouts\n\nCSS Grid - 2D layout:\n- display: grid\n- grid-template-columns/rows\n- Great for complex page layouts\n\nResponsive Design with Media Queries.").build());

        Lesson l10 = lessonRepository.save(Lesson.builder().course(c3).lessonOrder(3)
                .lessonTitle("JavaScript ES6+ Fundamentals").durationMinutes(90)
                .lessonContent("ES6+ brings powerful features to JavaScript:\n\n- let/const instead of var\n- Arrow functions: () => {}\n- Template literals: `Hello ${name}`\n- Destructuring: const {a, b} = obj\n- Spread/Rest operator: ...args\n- Promises and async/await\n- Modules: import/export").build());

        // Lessons for DB-202
        Lesson l11 = lessonRepository.save(Lesson.builder().course(c4).lessonOrder(1)
                .lessonTitle("Relational Model & Basic SQL").durationMinutes(75)
                .lessonContent("Relational Model:\n- Table (Relation)\n- Attribute (Column)\n- Tuple (Row)\n- Primary Key\n- Foreign Key\n\nBasic SQL:\n- SELECT, FROM, WHERE\n- INSERT, UPDATE, DELETE\n- ORDER BY, GROUP BY, HAVING\n- JOIN: INNER, LEFT, RIGHT, FULL").build());

        Lesson l12 = lessonRepository.save(Lesson.builder().course(c4).lessonOrder(2)
                .lessonTitle("Database Design & Normalization").durationMinutes(85)
                .lessonContent("Good database design avoids redundancy and inconsistency.\n\nNormal Forms:\n- 1NF: Eliminate repeating groups\n- 2NF: Eliminate partial dependencies\n- 3NF: Eliminate transitive dependencies\n- BCNF: Boyce-Codd Normal Form\n\nDesign process:\n1. Gather requirements\n2. Draw ERD\n3. Convert to relational model\n4. Normalize").build());

        // Enrollments
        Enrollment e1 = enrollmentRepository.save(Enrollment.builder()
                .student(student1).course(c1).progressPercentage(75).totalXp(150).build());
        Enrollment e2 = enrollmentRepository.save(Enrollment.builder()
                .student(student1).course(c2).progressPercentage(33).totalXp(60).build());
        Enrollment e3 = enrollmentRepository.save(Enrollment.builder()
                .student(student1).course(c3).progressPercentage(66).totalXp(120).build());
        enrollmentRepository.save(Enrollment.builder()
                .student(student2).course(c1).progressPercentage(50).totalXp(100).build());
        enrollmentRepository.save(Enrollment.builder()
                .student(student2).course(c4).progressPercentage(50).totalXp(80).build());

        // Lesson progress
        progressRepository.save(LessonProgress.builder().student(student1).lesson(l1).completed(true).completedAt(java.time.LocalDate.now().minusDays(10)).build());
        progressRepository.save(LessonProgress.builder().student(student1).lesson(l2).completed(true).completedAt(java.time.LocalDate.now().minusDays(8)).build());
        progressRepository.save(LessonProgress.builder().student(student1).lesson(l3).completed(true).completedAt(java.time.LocalDate.now().minusDays(5)).build());
        progressRepository.save(LessonProgress.builder().student(student1).lesson(l5).completed(true).completedAt(java.time.LocalDate.now().minusDays(7)).build());
        progressRepository.save(LessonProgress.builder().student(student1).lesson(l8).completed(true).completedAt(java.time.LocalDate.now().minusDays(4)).build());
        progressRepository.save(LessonProgress.builder().student(student1).lesson(l9).completed(true).completedAt(java.time.LocalDate.now().minusDays(2)).build());
        progressRepository.save(LessonProgress.builder().student(student2).lesson(l1).completed(true).completedAt(java.time.LocalDate.now().minusDays(6)).build());
        progressRepository.save(LessonProgress.builder().student(student2).lesson(l2).completed(true).completedAt(java.time.LocalDate.now().minusDays(4)).build());
        progressRepository.save(LessonProgress.builder().student(student2).lesson(l11).completed(true).completedAt(java.time.LocalDate.now().minusDays(3)).build());

        // Assignments
        assignmentRepository.save(Assignment.builder().course(c1).title("Assignment: Draw a Use Case Diagram")
                .type(Assignment.AssignmentType.HOMEWORK).dueDate(LocalDateTime.now().plusDays(14)).maxScore(10).build());
        assignmentRepository.save(Assignment.builder().course(c1).title("Midterm Quiz – SE-101")
                .type(Assignment.AssignmentType.QUIZ).dueDate(LocalDateTime.now().plusDays(21)).maxScore(10).build());
        assignmentRepository.save(Assignment.builder().course(c2).title("Assignment: Implement a Linked List")
                .type(Assignment.AssignmentType.HOMEWORK).dueDate(LocalDateTime.now().plusDays(10)).maxScore(10).build());
        assignmentRepository.save(Assignment.builder().course(c3).title("Assignment: Responsive Layout")
                .type(Assignment.AssignmentType.HOMEWORK).dueDate(LocalDateTime.now().plusDays(7)).maxScore(10).build());

        // Notifications
        notificationRepository.save(Notification.builder().user(student1)
                .title("New Announcement – SE-101").message("Your instructor posted an announcement about the midterm exam schedule.")
                .type(Notification.NotifType.ANNOUNCEMENT).isRead(false).build());
        notificationRepository.save(Notification.builder().user(student1)
                .title("Lesson Completed").message("Congratulations! You completed Lesson 3: Requirements Analysis & Specification.")
                .type(Notification.NotifType.BADGE).isRead(false).build());
        notificationRepository.save(Notification.builder().user(student1)
                .title("New Reply in Forum").message("Your instructor replied to your question in the SE-101 forum.")
                .type(Notification.NotifType.COMMENT).isRead(true).build());

        // Notes
        noteRepository.save(Note.builder().student(student1).lesson(l1).course(c1)
                .content("SDLC has 6 phases: Planning → Analysis → Design → Implementation → Testing → Maintenance")
                .highlightColor("#FFFF00").noteType(Note.NoteType.LESSON).build());
        noteRepository.save(Note.builder().student(student1).lesson(l2).course(c1)
                .content("Agile suits projects with changing requirements; Waterfall suits projects with clear upfront requirements")
                .highlightColor("#90EE90").noteType(Note.NoteType.LESSON).build());
        noteRepository.save(Note.builder().student(student1)
                .title("SE Summary Notes").content("Summary of SE-101 key concepts for the final exam")
                .highlightColor("#FFB6C1").noteType(Note.NoteType.STANDALONE).build());

        log.info("Seed data initialized successfully.");
    }
}
