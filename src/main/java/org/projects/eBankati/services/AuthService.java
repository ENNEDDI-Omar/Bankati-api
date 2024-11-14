package org.projects.eBankati.services;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.projects.eBankati.domain.entities.Role;
import org.projects.eBankati.domain.entities.User;
import org.projects.eBankati.dto.request.LoginRequest;
import org.projects.eBankati.dto.request.RegisterRequest;
import org.projects.eBankati.dto.response.AuthResponse;
import org.projects.eBankati.exceptions.AuthenticationException;
import org.projects.eBankati.repositories.RoleRepository;
import org.projects.eBankati.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setAge(request.getAge());

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AuthenticationException("Default role not found"));
        user.setRole(userRole);

        userRepository.save(user);

        return AuthResponse.builder()
                .message("Registration successful")
                .success(true)
                .build();
    }

    public AuthResponse login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        // Créer la session
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole().getName());

        return AuthResponse.builder()
                .message("Login successful")
                .success(true)
                .build();
    }

    public AuthResponse logout(HttpSession session) {
        session.invalidate();
        return AuthResponse.builder()
                .message("Logout successful")
                .success(true)
                .build();
    }
}