package org.projects.eBankati.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.projects.eBankati.domain.entities.Role;
import org.projects.eBankati.dto.request.LoginRequest;
import org.projects.eBankati.dto.request.RegisterRequest;
import org.projects.eBankati.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    public void setup() {
        // Assurer que le rôle USER existe
        if (!roleRepository.findByName("USER").isPresent()) {
            Role userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);
        }
    }



    @Test
    public void testLoginSuccess() throws Exception {
        // Premier enregistrement
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("Login Test");
        registerRequest.setEmail("login@example.com");
        registerRequest.setPassword("Test@123");
        registerRequest.setAge(25);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(result -> {
                    System.out.println("Register Response for Login Test: " +
                            result.getResponse().getContentAsString());
                });

        // Test login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("Login Test");
        loginRequest.setPassword("Test@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andDo(result -> {
                    System.out.println("Login Response: " + result.getResponse().getContentAsString());
                });
    }

    @Test
    public void testRegisterDuplicateEmail() throws Exception {
        // Premier enregistrement
        RegisterRequest request1 = new RegisterRequest();
        request1.setUsername("User One");
        request1.setEmail("duplicate@example.com");
        request1.setPassword("Test@123");
        request1.setAge(25);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andDo(result -> {
                    System.out.println("First Register Response: " +
                            result.getResponse().getContentAsString());
                });

        // Deuxième enregistrement avec le même email
        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("User Two");
        request2.setEmail("duplicate@example.com");
        request2.setPassword("Test@123");
        request2.setAge(25);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    System.out.println("Second Register Response: " +
                            result.getResponse().getContentAsString());
                });
    }

    @Test
    public void testRegisterInvalidPassword() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Test User");
        request.setEmail("test@example.com");
        request.setPassword("weak");  // Mot de passe invalide
        request.setAge(25);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(result -> {
                    System.out.println("Invalid Password Response: " +
                            result.getResponse().getContentAsString());
                });
    }
}