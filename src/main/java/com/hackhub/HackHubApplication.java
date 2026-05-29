package com.hackhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * Main entry point of the HackHub application.
 *
 * <p>This class bootstraps the Spring Boot application and logs useful
 * startup information once the application context has been initialized.</p>
 *
 * <p>At startup, the application displays:</p>
 * <ul>
 *   <li>The application base URL.</li>
 *   <li>The H2 console URL.</li>
 *   <li>The API base URL.</li>
 * </ul>
 *
 * <p>The displayed URLs are derived from the active Spring environment
 * configuration.</p>
 */
@SpringBootApplication
public class HackHubApplication {

	/**
	 * Logger used to output application startup information.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(HackHubApplication.class);

	/**
	 * Starts the HackHub application.
	 *
	 * <p>This method initializes the Spring Boot context, retrieves relevant
	 * runtime configuration properties, and logs the main application URLs
	 * for developer convenience.</p>
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		Environment env = SpringApplication.run(HackHubApplication.class, args).getEnvironment();

		String port = env.getProperty("server.port", "8080");
		String contextPath = env.getProperty("server.servlet.context-path", "");
		String baseUrl = "http://localhost:" + port + contextPath;

		LOGGER.info("""
				
			==================================================
			HackHub is running!
			Website:     {}
			H2 Console:   {}
			API Base URL: {}
			==================================================
			""", baseUrl, baseUrl + "/h2-console", baseUrl + "/api"
		);
	}
}