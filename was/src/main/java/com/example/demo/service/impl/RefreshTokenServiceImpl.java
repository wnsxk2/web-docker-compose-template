package com.example.demo.service.impl;

import com.example.demo.exception.InvalidTokenException;
import com.example.demo.exception.TokenExpiredException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import com.example.demo.repository.InMemoryRefreshTokenRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final InMemoryRefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider tokenProvider;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Override
    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        RefreshToken refreshToken = RefreshToken.builder()
            .token(tokenProvider.generateRefreshToken())
            .userId(user.getId())
            .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
            .revoked(false)
            .createdAt(LocalDateTime.now())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken rotateRefreshToken(String oldToken, String ipAddress, String userAgent) {
        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(oldToken)
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (oldRefreshToken.isRevoked()) {
            revokeAllUserTokens(oldRefreshToken.getUserId());
            throw new InvalidTokenException("Token reuse detected - all tokens revoked");
        }

        if (oldRefreshToken.isExpired()) {
            throw new TokenExpiredException("Refresh token expired");
        }

        // Create new refresh token
        RefreshToken newRefreshToken = RefreshToken.builder()
            .token(tokenProvider.generateRefreshToken())
            .userId(oldRefreshToken.getUserId())
            .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
            .revoked(false)
            .createdAt(LocalDateTime.now())
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();

        refreshTokenRepository.save(newRefreshToken);

        // Revoke old token
        oldRefreshToken.setRevoked(true);
        oldRefreshToken.setReplacedByToken(newRefreshToken.getToken());
        refreshTokenRepository.save(oldRefreshToken);

        return newRefreshToken;
    }

    @Override
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        List<RefreshToken> userTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(userId);
        userTokens.forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Override
    public RefreshToken getRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));
    }
}
