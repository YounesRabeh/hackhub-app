package com.hackhub.testsupport.coverage;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Thread-safe registry of controller endpoints hit during endpoint coverage tests.
 */
@Component
public class EndpointCoverageRegistry {

	private final Set<String> hitEndpoints = ConcurrentHashMap.newKeySet();

	public void recordHit(String httpMethod, String endpointPattern) {
		hitEndpoints.add(httpMethod + " " + endpointPattern);
	}

	public Set<String> snapshotHitEndpoints() {
		return new TreeSet<>(hitEndpoints);
	}
}
