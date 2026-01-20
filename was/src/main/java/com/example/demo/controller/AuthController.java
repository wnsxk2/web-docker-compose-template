package com.example.demo.controller;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.TokenResponse;
import com.example.demo.dto.response.UserInfoResponse;
import com.example.demo.exception.InvalidTokenException;
import com.example.demo.service.AuthenticationService;
import com.example.demo.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfoResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserInfoResponse user = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("User registered successfully", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse loginResponse = authenticationService.login(
            request, ipAddress, userAgent
        );

        // Set refresh token in HTTP-only cookie
        Cookie refreshTokenCookie = cookieUtil.createRefreshTokenCookie(
            loginResponse.getRefreshToken()
        );
        httpResponse.addCookie(refreshTokenCookie);

        // Return access token in response body
        TokenResponse tokenResponse = TokenResponse.builder()
            .accessToken(loginResponse.getAccessToken())
            .tokenType("Bearer")
            .expiresIn(1800L)
            .build();

        return ResponseEntity.ok(
            ApiResponse.success("Login successful", tokenResponse)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String refreshToken = cookieUtil.extractRefreshTokenFromCookie(httpRequest);
        if (refreshToken == null) {
            throw new InvalidTokenException("Refresh token not found in cookie");
        }

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse loginResponse = authenticationService.refreshToken(
            refreshToken, ipAddress, userAgent
        );

        // Set new refresh token in cookie
        Cookie newRefreshTokenCookie = cookieUtil.createRefreshTokenCookie(
            loginResponse.getRefreshToken()
        );
        httpResponse.addCookie(newRefreshTokenCookie);

        TokenResponse tokenResponse = TokenResponse.builder()
            .accessToken(loginResponse.getAccessToken())
            .tokenType("Bearer")
            .expiresIn(1800L)
            .build();

        return ResponseEntity.ok(
            ApiResponse.success("Token refreshed successfully", tokenResponse)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String refreshToken = cookieUtil.extractRefreshTokenFromCookie(httpRequest);
        if (refreshToken != null) {
            authenticationService.logout(refreshToken);
        }

        // Clear refresh token cookie
        Cookie clearCookie = cookieUtil.createClearRefreshTokenCookie();
        httpResponse.addCookie(clearCookie);

        return ResponseEntity.ok(
            ApiResponse.success("Logout successful", null)
        );
    }
}
