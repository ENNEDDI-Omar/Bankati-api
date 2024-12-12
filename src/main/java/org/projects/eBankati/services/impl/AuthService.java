package org.projects.eBankati.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.projects.eBankati.domain.entities.RefreshToken;
import org.projects.eBankati.domain.entities.Role;
import org.projects.eBankati.domain.entities.User;
import org.projects.eBankati.dto.request.LoginRequest;
import org.projects.eBankati.dto.request.RefreshTokenRequest;
import org.projects.eBankati.dto.request.RegisterRequest;
import org.projects.eBankati.dto.response.AuthResponse;
import org.projects.eBankati.exceptions.AuthenticationException;
import org.projects.eBankati.repositories.RoleRepository;
import org.projects.eBankati.repositories.UserRepository;
import org.projects.eBankati.security.service.JwtService;
import org.projects.eBankati.security.service.RefreshTokenService;
import org.projects.eBankati.security.token.TokenBlacklist;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AuthenticationException("Username already taken");
        }

        User user = createUser(request);
        userRepository.save(user);

        return AuthResponse.builder()
                .message("User registered successfully")
                .success(true)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            if (authentication.isAuthenticated())
            {

                User user = userRepository.findByUsername(request.getUsername())
                        .orElseThrow(() -> new AuthenticationException("User not found"));

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getUsername());

                String accessToken = jwtService.generateToken(userDetails);

                RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

                return AuthResponse.builder()
                        .message("Login successful")
                        .accessToken(accessToken)
                        .refreshToken(refreshToken.getToken())
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

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null) {
            tokenBlacklist.addToBlacklist(accessToken);
        }
        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.verifyExpiration(request.getRefreshToken());
            User user = refreshToken.getUser();

            // Convertir User en UserDetails pour générer le token
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUsername());
            String accessToken = jwtService.generateToken(userDetails);

            return AuthResponse.builder()
                    .message("Token refreshed successfully")
                    .accessToken(accessToken)
                    .refreshToken(request.getRefreshToken())
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new AuthenticationException("Token refresh failed: " + e.getMessage());
        }
    }
}
