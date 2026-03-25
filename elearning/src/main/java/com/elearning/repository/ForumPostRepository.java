package com.elearning.repository;

import com.elearning.model.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    List<ForumPost> findByCourseIdOrderByCreatedAtDesc(Long courseId);
    List<ForumPost> findByCourseIdAndPostTypeOrderByCreatedAtDesc(Long courseId, ForumPost.PostType postType);
    long countByCourseId(Long courseId);
}
