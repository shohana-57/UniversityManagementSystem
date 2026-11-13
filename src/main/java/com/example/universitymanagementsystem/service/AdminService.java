package com.example.universitymanagementsystem.service;

import com.example.universitymanagementsystem.dto.AdminRequest;
import com.example.universitymanagementsystem.dto.AdminResponse;
import com.example.universitymanagementsystem.exception.DuplicateResourceException;
import com.example.universitymanagementsystem.exception.ResourceNotFoundException;
import com.example.universitymanagementsystem.model.Admin;
import com.example.universitymanagementsystem.model.Role;
import com.example.universitymanagementsystem.model.User;
import com.example.universitymanagementsystem.repository.AdminRepository;
import com.example.universitymanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AdminResponse create(AdminRequest req) {
        if (req.password() == null || req.password().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (adminRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already exists");
        }
        User user = new User(null, req.username(), passwordEncoder.encode(req.password()), Role.ADMIN);
        Admin admin = new Admin(null, req.name(), req.email(), user);
        return toResponse(adminRepository.save(admin));
    }

    @Transactional(readOnly = true)
    public List<AdminResponse> getAll() {
        return adminRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminResponse getById(Long id) {
        return toResponse(find(id));
    }

    @Transactional
    public AdminResponse update(Long id, AdminRequest req) {
        Admin admin = find(id);
        if (!admin.getEmail().equals(req.email()) && adminRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Email already exists");
        }
        User user = admin.getUser();
        if (!user.getUsername().equals(req.username()) && userRepository.existsByUsername(req.username())) {
            throw new DuplicateResourceException("Username already exists");
        }
        admin.setName(req.name());
        admin.setEmail(req.email());
        user.setUsername(req.username());
        if (req.password() != null && !req.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.password()));
        }
        return toResponse(adminRepository.save(admin));
    }

    @Transactional
    public void delete(Long id) {
        adminRepository.delete(find(id));
    }

    private Admin find(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id " + id));
    }

    private AdminResponse toResponse(Admin a) {
        return new AdminResponse(a.getId(), a.getName(), a.getEmail(), a.getUser().getUsername());
    }
}