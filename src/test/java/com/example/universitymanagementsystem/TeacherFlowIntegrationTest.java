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
class TeacherFlowIntegrationTest extends IntegrationTestSupport {

    @Test
    void teacherFlow_login_add_update_delete_logout() throws Exception {
        // setup: admin ekta teacher account banay
        createAsAdmin("/api/teachers", teacherJson("Flow Teacher", "flowt@ums.com", "flow_tea"));

        // 1. teacher login
        MockHttpSession session = login("flow_tea", "123456");
        mockMvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("TEACHER"));

        // 2. teacher add (201)
        String body = create(session, "/api/teachers",
                teacherJson("New Teacher", "newt@ums.com", "new_tea"));
        String id = idOf(body);

        // 3. teacher update (200)
        mockMvc.perform(put("/api/teachers/" + id).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(teacherJson("Updated Teacher", "newt@ums.com", "new_tea")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Teacher"));

        // 4. teacher delete (204), tarpor 404
        mockMvc.perform(delete("/api/teachers/" + id).session(session))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/teachers/" + id).session(session))
                .andExpect(status().isNotFound());

        // 5. teacher student ba admin er data dekhte pare na (403)
        mockMvc.perform(get("/api/students").session(session)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admins").session(session)).andExpect(status().isForbidden());

        // 6. logout, tarpor 401
        logout(session);
        mockMvc.perform(get("/api/teachers").session(session))
                .andExpect(status().isUnauthorized());
    }
}
