package com.asif.linkedin.user_service.dto;

import jakarta.persistence.Column;

public record AuthResponse(
         String userId,
         String email,
         String firstName,
         String lastName,
         String accessToken,
         String refreshToken,
         String tokenType
) {
}
