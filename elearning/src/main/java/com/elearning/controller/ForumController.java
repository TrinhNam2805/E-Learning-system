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
import java.util.List;

@Controller
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping
    public String list(@RequestParam Long courseId,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/courses";

        List<ForumPost> posts = forumService.findByCourseId(courseId);
        boolean canPost = enrollmentService.isEnrolled(user.getId(), courseId)
                || user.getRole() == User.Role.TEACHER
                || user.getRole() == User.Role.ADMIN;

        model.addAttribute("course", course);
        model.addAttribute("posts", posts);
        model.addAttribute("canPost", canPost);
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
        boolean canPost = enrollmentService.isEnrolled(user.getId(), post.getCourse().getId())
                || user.getRole() == User.Role.TEACHER
                || user.getRole() == User.Role.ADMIN;

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("canPost", canPost);
        model.addAttribute("currentUser", user);
        model.addAttribute("unreadCount", notificationService.countUnread(user.getId()));
        return "forum/post";
    }

    @PostMapping("/create")
    public String createPost(@RequestParam Long courseId,
                             @RequestParam String title,
                             @RequestParam String postType,
                             @RequestParam String content,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes ra) {
        if (userDetails == null) return "redirect:/login";
        User user = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
        if (user == null) return "redirect:/login";

        if (postType.equals("ANNOUNCEMENT") && user.getRole() == User.Role.STUDENT) {
            ra.addFlashAttribute("error", "Only teachers can post announcements.");
            return "redirect:/forum?courseId=" + courseId;
        }
        if (user.getRole() != User.Role.STUDENT) return "redirect:/access-denied";
        if (!enrollmentService.isEnrolled(user.getId(), courseId)) {
            ra.addFlashAttribute("error", "You must enroll in this course to post.");
            return "redirect:/forum?courseId=" + courseId;
        }

        Course course = courseService.findById(courseId).orElse(null);
        if (course == null) return "redirect:/courses";

        ForumPost post = ForumPost.builder()
                .course(course).author(user).title(title).content(content)
                .postType(ForumPost.PostType.valueOf(postType)).build();
        forumService.createPost(post);
        ra.addFlashAttribute("success", "Post created successfully!");
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
        if (user.getRole() != User.Role.STUDENT) return "redirect:/access-denied";
        if (!enrollmentService.isEnrolled(user.getId(), post.getCourse().getId())) {
            ra.addFlashAttribute("error", "You must enroll in this course to comment.");
            return "redirect:/forum?courseId=" + post.getCourse().getId();
        }

        Comment comment = Comment.builder().post(post).author(user).content(content).build();
        forumService.addComment(comment);
        ra.addFlashAttribute("success", "Comment added!");
        return "redirect:/forum/post/" + postId;
    }
}
