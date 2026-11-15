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
class StudentFlowIntegrationTest extends IntegrationTestSupport {

    @Test
    void studentFlow_login_add_update_delete_logout() throws Exception {
        // setup: admin ekta student account banay, jar diye login korbo
        createAsAdmin("/api/students", studentJson("Flow Student", "flow@ums.com", "FLOW-001", "flow_stu"));

        // 1. student login (username + password)
        MockHttpSession session = login("flow_stu", "123456");
        mockMvc.perform(get("/api/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("STUDENT"));

        // 2. student add (201)
        String body = create(session, "/api/students",
                studentJson("New Student", "new@ums.com", "FLOW-002", "new_stu"));
        String id = idOf(body);

        // 3. student update (200)
        mockMvc.perform(put("/api/students/" + id).session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(studentJson("Updated Student", "new@ums.com", "FLOW-002", "new_stu")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Student"));
        mockMvc.perform(get("/api/students/" + id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Student"));

        // 4. student delete (204), tarpor 404
        mockMvc.perform(delete("/api/students/" + id).session(session))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/students/" + id).session(session))
                .andExpect(status().isNotFound());

        // 5. student teacher ba admin er data dekhte pare na (403)
        mockMvc.perform(get("/api/teachers").session(session)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admins").session(session)).andExpect(status().isForbidden());

        // 6. logout, tarpor ar access nai (401)
        logout(session);
        mockMvc.perform(get("/api/students").session(session))
                .andExpect(status().isUnauthorized());
    }
}
