package com.elearning.controller;

import com.elearning.model.entity.*;
import com.elearning.repository.UserRepository;
import com.elearning.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * {@code courseId} null → diễn đàn chung toàn kênh.
     * {@code lessonId} chỉ hợp lệ kèm {@code courseId} (gợi ý tiêu đề / gắn bài học).
     */
    @GetMapping
    public String list(@RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) Long lessonId,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        if (courseId == null) {
            List<ForumPost> posts = forumService.findGlobalPosts();
            model.addAttribute("globalForum", true);
            model.addAttribute("course", null);
            model.addAttribute("posts", posts);
            model.addAttribute("canCreatePost", mayCreateGlobalForumPost(user));
            model.addAttribute("courseLessons", Collections.<Lesson>emptyList());
        } else {
            Course course = courseService.findById(courseId).orElse(null);
            if (course == null) return "redirect:/forum";

            List<ForumPost> posts = forumService.findByCourseId(courseId);
            List<Lesson> courseLessons = lessonService.findPublishedByCourseId(courseId);

            model.addAttribute("globalForum", false);
            model.addAttribute("course", course);
            model.addAttribute("posts", posts);
            model.addAttribute("canCreatePost", mayUseCourseForum(user, courseId));
            model.addAttribute("courseLessons", courseLessons);

            if (lessonId != null) {
                lessonService.findById(lessonId).filter(l -> l.getCourse().getId().equals(courseId)).ifPresent(l -> {
                    model.addAttribute("prefillLessonId", lessonId);
                    model.addAttribute("prefillPostTitle", "Bài: " + l.getLessonTitle());
                });
            }
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "forum/list";
    }

    @GetMapping("/post/{postId}")
    public String viewPost(@PathVariable Long postId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        ForumPost post = forumService.findById(postId).orElse(null);
        if (post == null) return "redirect:/courses";

        forumService.incrementView(postId);
        List<Comment> comments = forumService.findCommentsByPostId(postId);
        boolean canComment = mayCommentOnPost(user, post);

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("canComment", canComment);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "forum/post";
    }

    @PostMapping("/create")
    public String createPost(@RequestParam(required = false) Long courseId,
                             @RequestParam(required = false) Long lessonId,
                             @RequestParam String title,
                             @RequestParam String postType,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        ForumPost.PostType type;
        try {
            type = ForumPost.PostType.valueOf(postType);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Invalid post type.");
            return redirectToForumList(courseId);
        }

        if (type == ForumPost.PostType.ANNOUNCEMENT && user.getRole() == User.Role.STUDENT) {
            ra.addFlashAttribute("error", "Chỉ giảng viên hoặc quản trị viên mới đăng được thông báo.");
            return redirectToForumList(courseId);
        }

        if (courseId == null) {
            if (!mayCreateGlobalForumPost(user)) {
                ra.addFlashAttribute("error", "Diễn đàn chung: chỉ giảng viên hoặc quản trị viên được tạo chủ đề mới. Sinh viên xem danh sách và bình luận trong từng bài.");
                return "redirect:/forum";
            }
            ForumPost post = ForumPost.builder()
                    .course(null).lesson(null)
                    .author(user).title(title.trim()).content(content)
                    .postType(type).build();
            forumService.createPost(post);
            ra.addFlashAttribute("success", "Đã đăng bài.");
            return "redirect:/forum";
        }

        if (!mayUseCourseForum(user, courseId)) {
            if (user.getRole() == User.Role.STUDENT) {
                ra.addFlashAttribute("error", "Bạn cần đăng ký khóa học để tham gia diễn đàn môn này.");
            } else {
                ra.addFlashAttribute("error", "Không có quyền đăng bài trong diễn đàn này.");
            }
            return "redirect:/forum?courseId=" + courseId;
        }

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/courses";

        Lesson lesson = null;
        if (lessonId != null) {
            lesson = lessonService.findById(lessonId).filter(l -> l.getCourse().getId().equals(courseId)).orElse(null);
            if (lesson == null) {
                ra.addFlashAttribute("error", "Bài học không thuộc khóa học này.");
                return "redirect:/forum?courseId=" + courseId;
            }
        }

        ForumPost post = ForumPost.builder()
                .course(course).lesson(lesson)
                .author(user).title(title.trim()).content(content)
                .postType(type).build();
        forumService.createPost(post);
        ra.addFlashAttribute("success", "Đã đăng bài.");
        return "redirect:/forum?courseId=" + courseId;
    }

    @PostMapping("/comment")
    public String addComment(@RequestParam Long postId,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        ForumPost post = forumService.findById(postId).orElse(null);
        if (post == null) return "redirect:/courses";
        if (!mayCommentOnPost(user, post)) {
            ra.addFlashAttribute("error", "Bạn không có quyền bình luận bài này.");
            return "redirect:/forum/post/" + postId;
        }

        Comment comment = Comment.builder().post(post).author(user).content(content).build();
        forumService.addComment(comment);
        ra.addFlashAttribute("success", "Đã thêm bình luận.");
        return "redirect:/forum/post/" + postId;
    }

    private static String redirectToForumList(Long courseId) {
        if (courseId == null) {
            return "redirect:/forum";
        }
        return "redirect:/forum?courseId=" + courseId;
    }

    /** Diễn đàn chung: chỉ GV / admin tạo chủ đề; sinh viên xem + bình luận. */
    private static boolean mayCreateGlobalForumPost(User user) {
        return user.getRole() == User.Role.TEACHER
                || user.getRole() == User.Role.ADMIN;
    }

    private static boolean mayCommentOnGlobalForum(User user) {
        return user.getRole() == User.Role.STUDENT
                || user.getRole() == User.Role.TEACHER
                || user.getRole() == User.Role.ADMIN;
    }

    /** Giảng viên / admin có thể đăng ở mọi khóa; sinh viên cần đã đăng ký. */
    private boolean mayUseCourseForum(User user, Long courseId) {
        if (user.getRole() == User.Role.ADMIN || user.getRole() == User.Role.TEACHER) {
            return true;
        }
        if (user.getRole() == User.Role.STUDENT) {
            return enrollmentService.isEnrolled(user.getId(), courseId);
        }
        return false;
    }

    private boolean mayCommentOnPost(User user, ForumPost post) {
        if (post.getCourse() == null) {
            return mayCommentOnGlobalForum(user);
        }
        return mayUseCourseForum(user, post.getCourse().getId());
    }
}
