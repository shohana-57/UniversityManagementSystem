package com.example.universitymanagementsystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    // username + password diye login, session (cookie) ferot dey
    protected MockHttpSession login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    protected void logout(MockHttpSession session) throws Exception {
        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk());
    }

    // login kora session diye create (201), response body ferot dey
    protected String create(MockHttpSession session, String url, String json) throws Exception {
        return mockMvc.perform(post(url).session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn().getResponse().getContentAsString();
    }

    // setup: seeded admin diye ekta account banano, jate sei account diye login kora jay
    protected void createAsAdmin(String url, String json) throws Exception {
        mockMvc.perform(post(url).with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    protected String idOf(String json) {
        return json.replaceAll(".*\"id\":(\\d+).*", "$1");
    }

    protected String adminJson(String name, String email, String username) {
        return """
                {"name":"%s","email":"%s","username":"%s","password":"123456"}
                """.formatted(name, email, username);
    }

    protected String teacherJson(String name, String email, String username) {
        return """
                {"name":"%s","email":"%s","department":"CSE","username":"%s","password":"123456"}
                """.formatted(name, email, username);
    }

    protected String studentJson(String name, String email, String studentId, String username) {
        return """
                {"name":"%s","email":"%s","studentId":"%s","department":"CSE","username":"%s","password":"123456"}
                """.formatted(name, email, studentId, username);
    }
}