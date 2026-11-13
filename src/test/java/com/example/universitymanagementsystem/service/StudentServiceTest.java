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
class StudentServiceTest {

    @Mock StudentRepository studentRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks StudentService studentService;

    private Student sampleStudent(Long id, String name, String email, String studentId, String username) {
        User user = new User(id, username, "OLDHASH", Role.STUDENT);
        return new Student(id, name, email, studentId, "CSE", user);
    }

    // create(): password hash hoy, role STUDENT set hoy, duplicate studentId hole 409
    @Test
    void create() {
        when(passwordEncoder.encode("123456")).thenReturn("HASHED");
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));
        StudentRequest request =
                new StudentRequest("Rahim", "rahim@ums.com", "2207057", "CSE", "rahim", "123456");

        StudentResponse res = studentService.create(request);

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository).save(captor.capture());
        assertEquals(Role.STUDENT, captor.getValue().getUser().getRole());
        assertEquals("HASHED", captor.getValue().getUser().getPassword());
        assertEquals("2207057", res.studentId());

        // same studentId abar dile Duplicate error
        when(studentRepository.existsByStudentId("2207057")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> studentService.create(request));
        verify(studentRepository, times(1)).save(any());
    }

    // getAll(): shob student er list ferot dey
    @Test
    void getAll() {
        when(studentRepository.findAll()).thenReturn(List.of(
                sampleStudent(1L, "Rahim", "rahim@ums.com", "2207057", "rahim"),
                sampleStudent(2L, "Rabina", "rabina@ums.com", "2207058", "rabina")));

        List<StudentResponse> list = studentService.getAll();

        assertEquals(2, list.size());
        assertEquals("rahim", list.get(0).username());
        assertEquals("2207058", list.get(1).studentId());
    }

    // getById(): paile response, na paile ResourceNotFoundException (404)
    @Test
    void getById() {
        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(sampleStudent(1L, "Rahim", "rahim@ums.com", "2207057", "rahim")));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        StudentResponse res = studentService.getById(1L);

        assertEquals("Rahim", res.name());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getById(99L));
    }

    // update(): data bodlay, password na dile purono password thake
    @Test
    void update() {
        Student existing = sampleStudent(1L, "Rahim", "rahim@ums.com", "2207057", "rahim");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentResponse res = studentService.update(1L,
                new StudentRequest("Rahim Uddin", "rahim@ums.com", "2207057", "EEE", "rahim", null));

        assertEquals("Rahim Uddin", res.name());
        assertEquals("EEE", res.department());
        assertEquals("OLDHASH", existing.getUser().getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    // delete(): repository.delete() call hoy, id na paile 404
    @Test
    void delete() {
        Student existing = sampleStudent(1L, "Rahim", "rahim@ums.com", "2207057", "rahim");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        studentService.delete(1L);

        verify(studentRepository).delete(existing);
        assertThrows(ResourceNotFoundException.class, () -> studentService.delete(99L));
    }
}