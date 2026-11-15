package com.example.universitymanagementsystem.service;

import com.example.universitymanagementsystem.model.Role;
import com.example.universitymanagementsystem.model.User;
import com.example.universitymanagementsystem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock UserRepository userRepository;
    @InjectMocks CustomUserDetailsService customUserDetailsService;

    // login: database er user theke username, hash password ar ROLE_ soho role ashe
    @Test
    void loadUserByUsername() {
        when(userRepository.findByUsername("karim"))
                .thenReturn(Optional.of(new User(1L, "karim", "HASH", Role.TEACHER)));

        UserDetails details = customUserDetailsService.loadUserByUsername("karim");

        assertEquals("karim", details.getUsername());
        assertEquals("HASH", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER")));
    }

    // je user nai tar login fail hoy
    @Test
    void loadUserByUsernameNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("ghost"));
    }
}
