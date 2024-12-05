package org.projects.eBankati.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class TokenBlacklist {
    private final JwtService jwtService;
    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public void addToBlacklist(String token) {
        Long expirationTime = jwtService.extractExpiration(token).getTime();
        blacklistedTokens.put(token, expirationTime);
        log.info("Token added to blacklist. Expires at: {}. Current blacklist size: {}",
                dateFormat.format(new Date(expirationTime)),
                blacklistedTokens.size());
    }

    public boolean isBlacklisted(String token) {
        boolean isBlacklisted = blacklistedTokens.containsKey(token);
        if (isBlacklisted) {
            log.debug("Token found in blacklist. Access denied.");
        }
        return isBlacklisted;
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired tokens...");
        log.info("Current blacklist size before cleanup: {}", blacklistedTokens.size());

        long currentTime = System.currentTimeMillis();
        int initialSize = blacklistedTokens.size();

        blacklistedTokens.entrySet().removeIf(entry -> {
            boolean isExpired = entry.getValue() < currentTime;
            if (isExpired) {
                log.debug("Removing expired token. Expired at: {}",
                        dateFormat.format(new Date(entry.getValue())));
            }
            return isExpired;
        });

        int removedTokens = initialSize - blacklistedTokens.size();

        log.info("Cleanup completed. Removed {} expired tokens. New blacklist size: {}",
                removedTokens,
                blacklistedTokens.size());

        // Log de performance si beaucoup de tokens sont gérés
        if (blacklistedTokens.size() > 1000) {
            log.warn("Blacklist size is large ({}). Consider adjusting cleanup frequency.",
                    blacklistedTokens.size());
        }
    }

    public int getBlacklistSize() {
        int size = blacklistedTokens.size();
        log.debug("Current blacklist size: {}", size);
        return size;
    }

    // Méthode utile pour le debugging
    public void logBlacklistStatus() {
        log.info("=== Blacklist Status ===");
        log.info("Total tokens: {}", blacklistedTokens.size());
        log.info("Current time: {}", dateFormat.format(new Date()));

        blacklistedTokens.forEach((token, expiry) -> {
            log.debug("Token expiring at: {} | Expired: {}",
                    dateFormat.format(new Date(expiry)),
                    expiry < System.currentTimeMillis());
        });
        log.info("====================");
    }
}