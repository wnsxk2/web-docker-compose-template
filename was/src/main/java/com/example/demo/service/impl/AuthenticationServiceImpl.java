package com.example.demo.service.impl;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserInfoResponse;
import com.example.demo.exception.CustomAuthenticationException;
import com.example.demo.exception.InvalidTokenException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.InMemoryUserRepository;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final InMemoryUserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        User user = (User) authentication.getPrincipal();

        // Generate access token
        String accessToken = jwtTokenProvider.generateAccessToken(user, user.getId());

        // Generate and store refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
            user, ipAddress, userAgent
        );

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken, String ipAddress, String userAgent) {
        // Rotate refresh token
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(
            refreshToken, ipAddress, userAgent
        );

        // Get user
        User user = userRepository.findById(newRefreshToken.getUserId())
            .orElseThrow(() -> new InvalidTokenException("User not found"));

        // Generate new access token
        String accessToken = jwtTokenProvider.generateAccessToken(user, user.getId());

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(newRefreshToken.getToken())
            .build();
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    @Override
    public UserInfoResponse register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomAuthenticationException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomAuthenticationException("Email already exists");
        }

        // Create new user
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .roles(Set.of(Role.ROLE_USER))
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .createdAt(LocalDateTime.now())
            .build();

        User savedUser = userRepository.save(user);

        return UserInfoResponse.builder()
            .id(savedUser.getId())
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .roles(savedUser.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList()))
            .createdAt(savedUser.getCreatedAt())
            .build();
    }
}
