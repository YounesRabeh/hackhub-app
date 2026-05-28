package com.hackhub;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class DevelopmentSecurityConfiguration {

	@Bean
	SecurityFilterChain developmentSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.ignoringRequestMatchers(PathRequest.toH2Console()))
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers(PathRequest.toH2Console()).permitAll()
				.requestMatchers(
					"/v3/api-docs/**",
					"/swagger-ui.html",
					"/swagger-ui/**"
				).permitAll()
				.anyRequest().authenticated()
			)
			.httpBasic(Customizer.withDefaults());

		return http.build();
	}
}
