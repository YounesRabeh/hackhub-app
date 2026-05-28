package com.hackhub.application.mapper;

import com.hackhub.api.dto.response.UserResponse;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponse toResponse(User user) {
		if (user == null) {
			return null;
		}

		return new UserResponse(
			MapperFieldAccess.read(user, "id", Long.class),
			MapperFieldAccess.read(user, "email", String.class),
			MapperFieldAccess.read(user, "role", Role.class)
		);
	}
}
