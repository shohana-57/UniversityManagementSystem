
package com.example.universitymanagementsystem.service;

import com.example.universitymanagementsystem.dto.TeacherRequest;
import com.example.universitymanagementsystem.dto.TeacherResponse;
import com.example.universitymanagementsystem.exception.DuplicateResourceException;
import com.example.universitymanagementsystem.exception.ResourceNotFoundException;
import com.example.universitymanagementsystem.model.Role;
import com.example.universitymanagementsystem.model.Teacher;
import com.example.universitymanagementsystem.model.User;
import com.example.universitymanagementsystem.repository.TeacherRepository;
import com.example.universitymanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public TeacherResponse create(TeacherRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (teacherRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already exists");
        }
        User user = new User(null, req.username(), passwordEncoder.encode(req.password()), Role.TEACHER);
        Teacher teacher = new Teacher(null, req.name(), req.email(), req.department(), user);
        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional(readOnly = true)
    public List<TeacherResponse> getAll() {
        return teacherRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TeacherResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest req) {
        Teacher teacher = find(id);
        if (!teacher.getEmail().equals(req.email()) && teacherRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already exists");
        }
        User user = teacher.getUser();
        if (!user.getUsername().equals(req.username()) && userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already exists");
        }
        teacher.setName(req.name());
        teacher.setEmail(req.email());
        teacher.setDepartment(req.department());
        user.setUsername(req.username());
        if (req.password() != null && !req.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public void delete(Long id) {
        teacherRepository.delete(find(id));
    }

    private Teacher find(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id " + id));
    }

    private TeacherResponse toResponse(Teacher t) {
        return new TeacherResponse(t.getId(), t.getName(), t.getEmail(), t.getDepartment(), t.getUser().getUsername());
    }
}