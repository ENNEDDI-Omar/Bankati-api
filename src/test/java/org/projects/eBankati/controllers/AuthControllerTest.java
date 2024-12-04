package org.projects.eBankati.controllers;

import org.junit.jupiter.api.Test;
import org.projects.eBankati.dto.request.LoginRequest;
import org.projects.eBankati.dto.request.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setAge(25);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // D'abord enregistrer un utilisateur
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("Login Test");
        registerRequest.setEmail("login@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setAge(25);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Ensuite tester le login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("Login Test");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    public void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Duplicate User");
        request.setEmail("duplicate@example.com");
        request.setPassword("password123");
        request.setAge(25);

        // Premier enregistrement
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Deuxième enregistrement avec le même email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}