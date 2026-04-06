package com.elearning.controller;

import com.elearning.model.entity.Comment;
import com.elearning.model.entity.Course;
import com.elearning.model.entity.ForumPost;
import com.elearning.model.entity.Lesson;
import com.elearning.model.entity.User;
import com.elearning.repository.UserRepository;
import com.elearning.service.CourseService;
import com.elearning.service.EnrollmentService;
import com.elearning.service.ForumService;
import com.elearning.service.LessonService;
import com.elearning.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public String list(@RequestParam(required = false) Long courseId,
                       @RequestParam(required = false) Long lessonId,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        if (courseId == null) {
            model.addAttribute("globalForum", true);
            model.addAttribute("course", null);
            model.addAttribute("posts", forumService.findGlobalPosts());
            model.addAttribute("canCreatePost", mayCreateGlobalForumPost(user));
            model.addAttribute("courseLessons", Collections.<Lesson>emptyList());
            model.addAttribute("forumPageTitle", "Global forum");
            model.addAttribute("forumPageSubtitle", "Browse and create discussion topics across the student community.");
            model.addAttribute("lessonContextTitle", null);
        } else {
            Course course = courseService.findById(courseId).orElse(null);
            if (course == null) {
                return "redirect:/forum";
            }

            model.addAttribute("globalForum", false);
            model.addAttribute("course", course);
            model.addAttribute("posts", forumService.findByCourseId(courseId));
            model.addAttribute("canCreatePost", mayUseCourseForum(user, courseId));
            model.addAttribute("courseLessons", lessonService.findPublishedByCourseId(courseId));
            model.addAttribute("forumPageTitle", "Forum - " + course.getCourseName());
            model.addAttribute("forumPageSubtitle", "Enrolled students can start discussions for this course.");
            model.addAttribute("lessonContextTitle", null);

            if (lessonId != null) {
                lessonService.findById(lessonId)
                        .filter(l -> l.getCourse().getId().equals(courseId))
                        .ifPresent(l -> {
                            model.addAttribute("prefillLessonId", lessonId);
                            model.addAttribute("prefillPostTitle", "Lesson " + l.getLessonOrder() + " - " + l.getLessonTitle());
                            model.addAttribute("lessonContextTitle", "Discussing lesson " + l.getLessonOrder() + ": " + l.getLessonTitle());
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
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        ForumPost post = forumService.findById(postId).orElse(null);
        if (post == null) {
            return "redirect:/courses";
        }

        forumService.incrementView(postId);
        model.addAttribute("post", post);
        model.addAttribute("comments", forumService.findCommentsByPostId(postId));
        model.addAttribute("canComment", mayCommentOnPost(user, post));
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
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        ForumPost.PostType type;
        try {
            type = ForumPost.PostType.valueOf(postType);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Invalid post type.");
            return redirectToForumList(courseId);
        }

        if (type == ForumPost.PostType.ANNOUNCEMENT) {
            ra.addFlashAttribute("error", "Announcement posts are not supported in the student-only version.");
            return redirectToForumList(courseId);
        }

        if (courseId == null) {
            if (!mayCreateGlobalForumPost(user)) {
                ra.addFlashAttribute("error", "Only signed-in students can create global topics.");
                return "redirect:/forum";
            }
            forumService.createPost(ForumPost.builder()
                    .course(null)
                    .author(user)
                    .title(title.trim())
                    .content(content)
                    .postType(type)
                    .build());
            ra.addFlashAttribute("success", "Post published.");
            return "redirect:/forum";
        }

        if (!mayUseCourseForum(user, courseId)) {
            ra.addFlashAttribute("error", "Enroll in the course to use this forum.");
            return "redirect:/forum?courseId=" + courseId;
        }

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) {
            return "redirect:/courses";
        }

        Lesson lesson = null;
        if (lessonId != null) {
            lesson = lessonService.findById(lessonId)
                    .filter(l -> l.getCourse().getId().equals(courseId))
                    .orElse(null);
            if (lesson == null) {
                ra.addFlashAttribute("error", "That lesson does not belong to this course.");
                return "redirect:/forum?courseId=" + courseId;
            }
        }

        forumService.createPost(ForumPost.builder()
                .course(course)
                .lesson(lesson)
                .author(user)
                .title(title.trim())
                .content(content)
                .postType(type)
                .build());
        ra.addFlashAttribute("success", "Post published.");
        return "redirect:/forum?courseId=" + courseId;
    }

    @PostMapping("/comment")
    public String addComment(@RequestParam Long postId,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        ForumPost post = forumService.findById(postId).orElse(null);
        if (post == null) {
            return "redirect:/courses";
        }
        if (!mayCommentOnPost(user, post)) {
            ra.addFlashAttribute("error", "You do not have permission to comment on this post.");
            return "redirect:/forum/post/" + postId;
        }
        if (content == null || content.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Comment content cannot be empty.");
            return "redirect:/forum/post/" + postId;
        }

        forumService.addComment(Comment.builder().post(post).author(user).content(content.trim()).build());
        ra.addFlashAttribute("success", "Comment added.");
        return "redirect:/forum/post/" + postId;
    }

    private static String redirectToForumList(Long courseId) {
        return courseId == null ? "redirect:/forum" : "redirect:/forum?courseId=" + courseId;
    }

    private static boolean mayCreateGlobalForumPost(User user) {
        return user.getRole() == User.Role.STUDENT;
    }

    private static boolean mayCommentOnGlobalForum(User user) {
        return user.getRole() == User.Role.STUDENT;
    }

    private boolean mayUseCourseForum(User user, Long courseId) {
        return user.getRole() == User.Role.STUDENT
                && enrollmentService.isEnrolled(user.getId(), courseId);
    }

    private boolean mayCommentOnPost(User user, ForumPost post) {
        if (post.getCourse() == null) {
            return mayCommentOnGlobalForum(user);
        }
        return mayUseCourseForum(user, post.getCourse().getId());
    }
}
