package com.hackhub.api;

import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
	name = OpenApiConfig.BEARER_AUTH,
	type = SecuritySchemeType.HTTP,
	scheme = "bearer",
	bearerFormat = "JWT",
	description = "Paste the raw JWT returned by /api/auth/login or /api/auth/register."
)
public class OpenApiConfig {

	public static final String BEARER_AUTH = "bearerAuth";
}
