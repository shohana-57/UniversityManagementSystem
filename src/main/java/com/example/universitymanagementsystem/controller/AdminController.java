package com.example.universitymanagementsystem.controller;

import com.example.universitymanagementsystem.dto.AdminRequest;
import com.example.universitymanagementsystem.dto.AdminResponse;
import com.example.universitymanagementsystem.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<AdminResponse> create(@Valid @RequestBody AdminRequest request) {
        AdminResponse created = adminService.create(request);
        return ResponseEntity.created(URI.create("/api/admins/" + created.id())).body(created);
    }

    @GetMapping
    public List<AdminResponse> getAll() {
        return adminService.getAll();
    }

    @GetMapping("/{id}")
    public AdminResponse getById(@PathVariable Long id) {
        return adminService.getById(id);
    }

    @PutMapping("/{id}")
    public AdminResponse update(@PathVariable Long id, @Valid @RequestBody AdminRequest request) {
        return adminService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}