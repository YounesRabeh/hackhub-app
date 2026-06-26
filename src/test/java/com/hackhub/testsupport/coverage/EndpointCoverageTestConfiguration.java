package com.hackhub.testsupport.coverage;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Test configuration that installs endpoint coverage tracking for MockMvc tests.
 */
@TestConfiguration
public class EndpointCoverageTestConfiguration implements WebMvcConfigurer {

	@NonNull
	private final EndpointCoverageInterceptor endpointCoverageInterceptor;

	public EndpointCoverageTestConfiguration(
		@NonNull EndpointCoverageInterceptor endpointCoverageInterceptor
	) {
		this.endpointCoverageInterceptor = endpointCoverageInterceptor;
	}

	@Override
	public void addInterceptors(@NonNull InterceptorRegistry registry) {
		registry.addInterceptor(endpointCoverageInterceptor);
	}
}
