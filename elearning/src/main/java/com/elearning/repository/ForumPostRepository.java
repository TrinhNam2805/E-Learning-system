package com.elearning.repository;

import com.elearning.model.entity.ForumPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
    @EntityGraph(attributePaths = {"course", "author", "lesson"})
    List<ForumPost> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @EntityGraph(attributePaths = {"author", "course", "lesson"})
    List<ForumPost> findByCourseIsNullOrderByCreatedAtDesc();
    List<ForumPost> findByCourseIdAndPostTypeOrderByCreatedAtDesc(Long courseId, ForumPost.PostType postType);
    long countByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"course", "author"})
    List<ForumPost> findByPostTypeOrderByCreatedAtDesc(ForumPost.PostType postType, Pageable pageable);
}
