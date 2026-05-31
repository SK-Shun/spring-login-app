package com.example.demo.dto.response;

import java.time.LocalDateTime;

import com.example.demo.entity.User;

public record UserResponse(
    Long id,
    String email,
    String username,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getCreatedAt()
        );
    }
}