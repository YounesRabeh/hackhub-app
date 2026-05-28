package com.hackhub.api.dto.response;

import com.hackhub.domain.enums.Role;

public record UserResponse(Long id, String email, Role role) {
}
