package com.example.demo.config;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.InMemoryUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {

    private final InMemoryUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // User 1: Regular user
        User user1 = User.builder()
            .id(1L)
            .username("user")
            .email("user@example.com")
            .password(passwordEncoder.encode("password123"))
            .roles(Set.of(Role.ROLE_USER))
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .createdAt(LocalDateTime.now())
            .build();

        // User 2: Admin user
        User user2 = User.builder()
            .id(2L)
            .username("admin")
            .email("admin@example.com")
            .password(passwordEncoder.encode("admin123"))
            .roles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN))
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .createdAt(LocalDateTime.now())
            .build();

        // User 3: Test user
        User user3 = User.builder()
            .id(3L)
            .username("testuser")
            .email("test@example.com")
            .password(passwordEncoder.encode("test123"))
            .roles(Set.of(Role.ROLE_USER))
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .createdAt(LocalDateTime.now())
            .build();

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        System.out.println("Dummy users initialized:");
        System.out.println("1. username: user, password: password123");
        System.out.println("2. username: admin, password: admin123");
        System.out.println("3. username: testuser, password: test123");
    }
}
