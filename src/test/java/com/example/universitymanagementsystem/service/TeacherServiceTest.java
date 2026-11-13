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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock TeacherRepository teacherRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks TeacherService teacherService;

    private Teacher sampleTeacher(Long id, String name, String email, String username) {
        User user = new User(id, username, "OLDHASH", Role.TEACHER);
        return new Teacher(id, name, email, "CSE", user);
    }

    // create(): password hash hoy, role TEACHER set hoy, duplicate username hole 409
    @Test
    void create() {
        when(passwordEncoder.encode("123456")).thenReturn("HASHED");
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> inv.getArgument(0));
        TeacherRequest request = new TeacherRequest("Karim", "karim@ums.com", "CSE", "karim", "123456");

        TeacherResponse res = teacherService.create(request);

        ArgumentCaptor<Teacher> captor = ArgumentCaptor.forClass(Teacher.class);
        verify(teacherRepository).save(captor.capture());
        User saved = captor.getValue().getUser();
        assertEquals("HASHED", saved.getPassword());
        assertEquals(Role.TEACHER, saved.getRole());
        assertEquals("karim", res.username());

        // same username abar dile Duplicate error
        when(userRepository.existsByUsername("karim")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> teacherService.create(request));
        verify(teacherRepository, times(1)).save(any());
    }

    // getAll(): shob teacher er list ferot dey
    @Test
    void getAll() {
        when(teacherRepository.findAll()).thenReturn(List.of(
                sampleTeacher(1L, "Karim", "karim@ums.com", "karim"),
                sampleTeacher(2L, "Salma", "salma@ums.com", "salma")));

        List<TeacherResponse> list = teacherService.getAll();

        assertEquals(2, list.size());
        assertEquals("karim", list.get(0).username());
        assertEquals("salma", list.get(1).username());
    }

    // getById(): paile response, na paile ResourceNotFoundException (404)
    @Test
    void getById() {
        when(teacherRepository.findById(1L))
                .thenReturn(Optional.of(sampleTeacher(1L, "Karim", "karim@ums.com", "karim")));
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        TeacherResponse res = teacherService.getById(1L);

        assertEquals("Karim", res.name());
        assertThrows(ResourceNotFoundException.class, () -> teacherService.getById(99L));
    }

    // update(): data bodlay, password na dile purono password thake
    @Test
    void update() {
        Teacher existing = sampleTeacher(1L, "Karim", "karim@ums.com", "karim");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(inv -> inv.getArgument(0));

        TeacherResponse res = teacherService.update(1L,
                new TeacherRequest("Karim Uddin", "karim@ums.com", "EEE", "karim", null));

        assertEquals("Karim Uddin", res.name());
        assertEquals("EEE", res.department());
        assertEquals("OLDHASH", existing.getUser().getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    // delete(): repository.delete() call hoy, id na paile 404
    @Test
    void delete() {
        Teacher existing = sampleTeacher(1L, "Karim", "karim@ums.com", "karim");
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(teacherRepository.findById(99L)).thenReturn(Optional.empty());

        teacherService.delete(1L);

        verify(teacherRepository).delete(existing);
        assertThrows(ResourceNotFoundException.class, () -> teacherService.delete(99L));
    }
}