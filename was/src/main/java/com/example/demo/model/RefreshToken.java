package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private Long id;
    private String token;
    private Long userId;
    private Instant expiryDate;
    private boolean revoked;
    private String replacedByToken;
    private LocalDateTime createdAt;
    private String ipAddress;
    private String userAgent;

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
}
