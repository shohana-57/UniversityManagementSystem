package com.example.universitymanagementsystem.repository;

import com.example.universitymanagementsystem.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    boolean existsByEmail(String email);
    Optional<Teacher> findByUserUsername(String username);
}