package com.example.universitymanagementsystem.service;

import com.example.universitymanagementsystem.dto.StudentRequest;
import com.example.universitymanagementsystem.dto.StudentResponse;
import com.example.universitymanagementsystem.exception.DuplicateResourceException;
import com.example.universitymanagementsystem.exception.ResourceNotFoundException;
import com.example.universitymanagementsystem.model.Role;
import com.example.universitymanagementsystem.model.Student;
import com.example.universitymanagementsystem.model.User;
import com.example.universitymanagementsystem.repository.StudentRepository;
import com.example.universitymanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StudentResponse create(StudentRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (studentRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (studentRepository.existsByStudentId(req.studentId())) {
            throw new DuplicateResourceException("Student ID already exists");
        }
        User user = new User(null, req.username(), passwordEncoder.encode(req.password()), Role.STUDENT);
        Student student = new Student(null, req.name(), req.email(), req.studentId(), req.department(), user);
        return toResponse(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAll() {
        return studentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest req) {
        Student student = find(id);
        if (!student.getEmail().equals(req.email()) && studentRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (!student.getStudentId().equals(req.studentId()) && studentRepository.existsByStudentId(req.studentId())) {
            throw new DuplicateResourceException("Student ID already exists");
        }
        User user = student.getUser();
        if (!user.getUsername().equals(req.username()) && userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already exists");
        }
        student.setName(req.name());
        student.setEmail(req.email());
        student.setStudentId(req.studentId());
        student.setDepartment(req.department());
        user.setUsername(req.username());
        if (req.password() != null && !req.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        studentRepository.delete(find(id));
    }

    private Student find(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }

    private StudentResponse toResponse(Student s) {
        return new StudentResponse(s.getId(), s.getName(), s.getEmail(), s.getStudentId(), s.getDepartment(), s.getUser().getUsername());
    }
}
