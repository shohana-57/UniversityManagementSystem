package com.example.universitymanagementsystem.config;

import com.example.universitymanagementsystem.model.Admin;
import com.example.universitymanagementsystem.model.Role;
import com.example.universitymanagementsystem.model.User;
import com.example.universitymanagementsystem.repository.AdminRepository;
import com.example.universitymanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            return;
        }
        User user = new User(null, "admin", passwordEncoder.encode("admin123"), Role.ADMIN);
        adminRepository.save(new Admin(null, "Default Admin", "admin@ums.com", user));
    }
}
