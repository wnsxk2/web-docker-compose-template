package com.example.demo.repository;

import com.example.demo.model.RefreshToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class InMemoryRefreshTokenRepository {

    private final ConcurrentHashMap<String, RefreshToken> tokensByToken = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<RefreshToken>> tokensByUserId = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public RefreshToken save(RefreshToken refreshToken) {
        if (refreshToken.getId() == null) {
            refreshToken.setId(idGenerator.getAndIncrement());
        }

        tokensByToken.put(refreshToken.getToken(), refreshToken);

        tokensByUserId.computeIfAbsent(refreshToken.getUserId(), k -> new ArrayList<>())
            .add(refreshToken);

        return refreshToken;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return Optional.ofNullable(tokensByToken.get(token));
    }

    public List<RefreshToken> findByUserIdAndRevokedFalse(Long userId) {
        return tokensByUserId.getOrDefault(userId, new ArrayList<>()).stream()
            .filter(token -> !token.isRevoked())
            .collect(Collectors.toList());
    }

    public void deleteExpiredTokens() {
        Instant now = Instant.now();
        List<String> expiredTokens = tokensByToken.values().stream()
            .filter(token -> token.getExpiryDate().isBefore(now))
            .map(RefreshToken::getToken)
            .collect(Collectors.toList());

        expiredTokens.forEach(tokensByToken::remove);
    }
}
