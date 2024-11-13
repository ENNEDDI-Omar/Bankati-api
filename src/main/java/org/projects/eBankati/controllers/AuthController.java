package org.projects.eBankati.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.projects.eBankati.dto.request.LoginRequest;
import org.projects.eBankati.dto.request.RegisterRequest;
import org.projects.eBankati.dto.response.AuthResponse;
import org.projects.eBankati.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request,
                                              HttpSession session) {
        return ResponseEntity.ok(authService.login(request, session));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpSession session) {
        return ResponseEntity.ok(authService.logout(session));
    }
}