package com.hackhub.testsupport.coverage;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
@RequiredArgsConstructor
public class EndpointCoverageInterceptor implements HandlerInterceptor {

	private static final String CONTROLLER_PACKAGE_PREFIX = "com.hackhub.api.controller";

	private final EndpointCoverageRegistry coverageRegistry;

	@Override
	public boolean preHandle(
		HttpServletRequest request,
		HttpServletResponse response,
		Object handler
	) {
		if (!(handler instanceof HandlerMethod handlerMethod)) {
			return true;
		}

		if (!handlerMethod.getBeanType().getPackageName().startsWith(CONTROLLER_PACKAGE_PREFIX)) {
			return true;
		}

		Object bestMatchingPattern = request.getAttribute(
			HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE
		);
		if (bestMatchingPattern instanceof String pattern) {
			String httpMethod = request.getMethod().toUpperCase(Locale.ROOT);
			coverageRegistry.recordHit(httpMethod, pattern);
		}

		return true;
	}
}
