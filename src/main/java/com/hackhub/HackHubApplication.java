package com.hackhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class HackHubApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(HackHubApplication.class);

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