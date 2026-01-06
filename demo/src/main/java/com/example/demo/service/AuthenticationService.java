package com.example.demo.service;

import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.RegisterRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.UserInfoResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    LoginResponse refreshToken(String refreshToken, String ipAddress, String userAgent);
    void logout(String refreshToken);
    UserInfoResponse register(RegisterRequest request);
}
