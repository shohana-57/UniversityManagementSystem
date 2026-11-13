package com.example.universitymanagementsystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String username,
        @Size(min = 6, message = "Password must be at least 6 characters") String password
) {}