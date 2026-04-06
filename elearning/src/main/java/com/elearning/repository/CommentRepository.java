package com.elearning.repository;

import com.elearning.model.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"author", "post"})
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    long countByPostId(Long postId);
}
