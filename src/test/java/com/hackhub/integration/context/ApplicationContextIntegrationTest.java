package com.hackhub.integration.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackhub.api.controller.AuthController;
import com.hackhub.api.controller.HackathonController;
import com.hackhub.api.controller.TeamController;
import com.hackhub.application.service.AuthService;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.TeamRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import com.hackhub.security.JwtAuthenticationFilter;
import com.hackhub.security.JwtService;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationContextIntegrationTest {

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Test
	void contextLoads() {
		assertThat(applicationContext).isNotNull();
		assertThat(applicationContext.isActive()).isTrue();
	}

	@Test
	void coreApplicationBeansAreRegistered() {
		assertThat(applicationContext.getBean(AuthService.class)).isNotNull();
		assertThat(applicationContext.getBean(UserRepository.class)).isNotNull();
		assertThat(applicationContext.getBean(TeamRepository.class)).isNotNull();
		assertThat(applicationContext.getBean(HackathonRepository.class)).isNotNull();
		assertThat(objectMapper).isNotNull();
	}

	@Test
	void securityBeansAreRegistered() {
		assertThat(applicationContext.getBean(SecurityFilterChain.class)).isNotNull();
		assertThat(applicationContext.getBean(AuthenticationManager.class)).isNotNull();
		assertThat(applicationContext.getBean(PasswordEncoder.class)).isNotNull();
		assertThat(applicationContext.getBean(JwtService.class)).isNotNull();
		assertThat(applicationContext.getBean(JwtAuthenticationFilter.class)).isNotNull();
	}

	@Test
	void apiControllersAreRegistered() {
		assertThat(applicationContext.getBean(AuthController.class)).isNotNull();
		assertThat(applicationContext.getBean(TeamController.class)).isNotNull();
		assertThat(applicationContext.getBean(HackathonController.class)).isNotNull();
	}

	@Test
	void authRoutesAreMapped() {
		Set<String> mappings = requestMappingHandlerMapping
			.getHandlerMethods()
			.keySet()
			.stream()
			.flatMap(mapping -> mapping.getPathPatternsCondition()
				.getPatterns()
				.stream()
				.flatMap(pattern -> mapping.getMethodsCondition()
					.getMethods()
					.stream()
					.map(method -> method + " " + pattern.getPatternString())))
			.collect(Collectors.toSet());

		assertThat(mappings).contains(
			RequestMethod.POST + " /api/auth/register",
			RequestMethod.POST + " /api/auth/login",
			RequestMethod.GET + " /api/auth/me"
		);
	}
}
