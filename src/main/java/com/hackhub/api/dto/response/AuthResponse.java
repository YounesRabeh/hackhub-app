package com.hackhub.api.dto.response;

public record AuthResponse(String token, UserResponse user) {
}
