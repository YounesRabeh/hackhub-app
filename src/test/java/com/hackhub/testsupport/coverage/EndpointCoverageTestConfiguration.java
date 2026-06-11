package com.hackhub.testsupport.coverage;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
public class EndpointCoverageTestConfiguration implements WebMvcConfigurer {

	private final EndpointCoverageInterceptor endpointCoverageInterceptor;

	public EndpointCoverageTestConfiguration(
		EndpointCoverageInterceptor endpointCoverageInterceptor
	) {
		this.endpointCoverageInterceptor = endpointCoverageInterceptor;
	}

	@Override
	public void addInterceptors(@NonNull InterceptorRegistry registry) {
		registry.addInterceptor(endpointCoverageInterceptor);
	}
}
