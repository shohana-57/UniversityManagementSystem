package com.example.universitymanagementsystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminFlowIntegrationTest extends IntegrationTestSupport {

    @Test
    void adminFlow_login_manageAllThree_logout() throws Exception {
        // 1. seeded admin login (admin / admin123)
        MockHttpSession session = login("admin", "admin123");
        mockMvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // 2. admin tin dhoroner user add kore (201)
        String adminId = idOf(create(session, "/api/admins",
                adminJson("Second Admin", "admin2@ums.com", "admin_two")));
        String teacherId = idOf(create(session, "/api/teachers",
                teacherJson("Admin Made Teacher", "at@ums.com", "adm_tea")));
        String studentId = idOf(create(session, "/api/students",
                studentJson("Admin Made Student", "as@ums.com", "ADM-001", "adm_stu")));

        // 3. admin tinta list ei dekhte pare (200)
        mockMvc.perform(get("/api/admins").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/api/teachers").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/api/students").session(session)).andExpect(status().isOk());

        // 4. admin teacher update kore (200)
        mockMvc.perform(put("/api/teachers/" + teacherId).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson("Updated By Admin", "at@ums.com", "adm_tea")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated By Admin"));

        // 5. admin tinta delete kore (204)
        mockMvc.perform(delete("/api/admins/" + adminId).session(session)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/teachers/" + teacherId).session(session)).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/students/" + studentId).session(session)).andExpect(status().isNoContent());

        // 6. logout, tarpor 401
        logout(session);
        mockMvc.perform(get("/api/admins").session(session))
                .andExpect(status().isUnauthorized());
    }
}