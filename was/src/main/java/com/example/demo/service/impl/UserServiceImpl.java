package com.example.demo.service.impl;

import com.example.demo.dto.response.UserInfoResponse;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.InMemoryUserRepository;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final InMemoryUserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UserInfoResponse getCurrentUser() {
        User user = getCurrentAuthenticatedUser();

        return UserInfoResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList()))
            .createdAt(user.getCreatedAt())
            .build();
    }

    @Override
    public void deleteCurrentUser() {
        User user = getCurrentAuthenticatedUser();
        refreshTokenService.revokeAllUserTokens(user.getId());
        userRepository.deleteById(user.getId());
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
