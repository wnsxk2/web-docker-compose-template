package com.example.demo.service;

import com.example.demo.dto.response.UserInfoResponse;

public interface UserService {
    UserInfoResponse getCurrentUser();
    void deleteCurrentUser();
}
