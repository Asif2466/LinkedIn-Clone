package com.asif.linkedin.user_service.dto;

import com.asif.linkedin.user_service.entity.UserRole;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String headline,
        String about,
        String location,
        String profilePhotoUrl,
        String coverPhotoUrl,
        List<String> skills,
        LocalDateTime createdAt
) {
}
