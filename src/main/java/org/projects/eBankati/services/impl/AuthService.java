package org.projects.eBankati.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.projects.eBankati.domain.entities.Role;
import org.projects.eBankati.domain.entities.User;
import org.projects.eBankati.dto.request.LoginRequest;
import org.projects.eBankati.dto.request.RegisterRequest;
import org.projects.eBankati.dto.response.AuthResponse;
import org.projects.eBankati.exceptions.AuthenticationException;
import org.projects.eBankati.repositories.RoleRepository;
import org.projects.eBankati.repositories.UserRepository;
import org.projects.eBankati.security.JwtService;
import org.projects.eBankati.security.TokenBlacklist;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final TokenBlacklist tokenBlacklist;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérification de l'existence de l'utilisateur
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AuthenticationException("Username already taken");
        }

        // Création de l'utilisateur
        User user = createUser(request);
        userRepository.save(user);

        // Génération du token
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .message("User registered successfully")
                .token(token)
                .success(true)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Authentification
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            if (authentication.isAuthenticated()) {
                User user = userRepository.findByUsername(request.getUsername())
                        .orElseThrow(() -> new AuthenticationException("User not found"));

                String token = jwtService.generateToken(user);

                return AuthResponse.builder()
                        .message("Login successful")
                        .token(token)
                        .success(true)
                        .build();
            }
            throw new AuthenticationException("Invalid credentials");
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            throw new AuthenticationException("Authentication failed");
        }
    }

    private User createUser(RegisterRequest request) {
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role 'USER' not found"));

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .age(request.getAge())
                .role(userRole)
                .build();
    }

    public void logout(String token) {
        // Ajouter le token à la blacklist
        tokenBlacklist.addToBlacklist(token);
        log.info("User logged out successfully");
    }
}
