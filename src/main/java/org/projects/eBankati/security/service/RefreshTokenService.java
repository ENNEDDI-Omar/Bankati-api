package org.projects.eBankati.security.service;

import lombok.RequiredArgsConstructor;
import org.projects.eBankati.domain.entities.RefreshToken;
import org.projects.eBankati.domain.entities.User;
import org.projects.eBankati.exceptions.AuthenticationException;
import org.projects.eBankati.repositories.RefreshTokenRepository;
import org.projects.eBankati.security.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${app.security.jwt.refresh-token.expiration}")
    private Long refreshTokenDurationMs;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Révoquer tous les refresh tokens existants pour l'utilisateur
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyExpiration(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(refreshToken -> {
                    if (refreshToken.getExpiryDate().compareTo(Instant.now()) < 0 || refreshToken.isRevoked()) {
                        refreshTokenRepository.delete(refreshToken);
                        throw new AuthenticationException("Refresh token expiré. Veuillez vous reconnecter.");
                    }
                    return refreshToken;
                })
                .orElseThrow(() -> new AuthenticationException("Refresh token non trouvé."));
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthenticationException("Refresh token non trouvé."));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}