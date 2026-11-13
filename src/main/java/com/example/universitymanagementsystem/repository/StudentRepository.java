package com.example.universitymanagementsystem.repository;

import com.example.universitymanagementsystem.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);
    boolean existsByStudentId(String studentId);
    Optional<Student> findByUserUsername(String username);
}