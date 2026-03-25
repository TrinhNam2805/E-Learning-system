package com.elearning.service;

import com.elearning.model.entity.*;
import com.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumPostRepository postRepository;
    private final CommentRepository commentRepository;

    public List<ForumPost> findByCourseId(Long courseId) {
        return postRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    public Optional<ForumPost> findById(Long id) {
        return postRepository.findById(id);
    }

    @Transactional
    public ForumPost createPost(ForumPost post) {
        return postRepository.save(post);
    }

    @Transactional
    public void incrementView(Long postId) {
        postRepository.findById(postId).ifPresent(p -> {
            p.setViewCount(p.getViewCount() + 1);
            postRepository.save(p);
        });
    }

    public List<Comment> findCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    @Transactional
    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
