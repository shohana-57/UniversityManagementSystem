package com.example.universitymanagementsystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional   // protita test er por database rollback hoy, tai test gulo ek arekta ke bhanga na
class ApiIntegrationTest {

    @Autowired MockMvc mockMvc;

    private String teacherJson(String username, String email) {
        return """
                {"name":"Karim","email":"%s","department":"CSE","username":"%s","password":"123456"}
                """.formatted(email, username);
    }

    private String studentJson(String username, String email, String studentId) {
        return """
                {"name":"Rahim","email":"%s","studentId":"%s","department":"CSE","username":"%s","password":"123456"}
                """.formatted(email, studentId, username);
    }

    @Test
    void withoutLogin_apiReturns401() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_canCreateTeacher_andPasswordIsNotReturned() throws Exception {
        mockMvc.perform(post("/api/teachers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson("it_karim", "it_karim@ums.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("it_karim"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void teacher_canAccessTeachers_butStudentsReturn403() throws Exception {
        mockMvc.perform(post("/api/teachers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson("it_teacher", "it_teacher@ums.com")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/teachers").with(httpBasic("it_teacher", "123456")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/students").with(httpBasic("it_teacher", "123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    void duplicateUsername_returns409() throws Exception {
        mockMvc.perform(post("/api/teachers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson("it_dup", "it_dup1@ums.com")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/teachers")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson("it_dup", "it_dup2@ums.com")))
                .andExpect(status().isConflict());
    }

    @Test
    void admin_canDeleteStudent_thenGetReturns404() throws Exception {
        String body = mockMvc.perform(post("/api/students")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("it_rahim", "it_rahim@ums.com", "IT-001")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = body.replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(delete("/api/students/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/students/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }
}