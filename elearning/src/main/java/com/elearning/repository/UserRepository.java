package com.elearning.repository;

import com.elearning.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findByRole(User.Role role);

    @Query("SELECT u FROM User u WHERE u.role = 'STUDENT' ORDER BY u.fullName")
    List<User> findAllStudents();

    @Query("SELECT u FROM User u WHERE u.role = 'TEACHER' ORDER BY u.fullName")
    List<User> findAllTeachers();
}
