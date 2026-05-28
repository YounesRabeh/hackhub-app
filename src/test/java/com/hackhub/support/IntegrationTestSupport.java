package com.hackhub.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class IntegrationTestSupport {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	protected String uniqueEmail() {
		return "user+" + UUID.randomUUID() + "@example.com";
	}

	protected ResultActions postJson(String uri, Object body) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.post(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body))
		);
	}

	protected ResultActions getWithBearer(String uri, String token) throws Exception {
		return mockMvc.perform(
			MockMvcRequestBuilders
				.get(uri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
		);
	}

	protected String extractToken(MvcResult result) throws Exception {
		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.path("token").asText();
	}

	protected Map<String, String> registerPayload(String email, String password) {
		return Map.of("email", email, "password", password);
	}

	protected Map<String, String> loginPayload(String email, String password) {
		return Map.of("email", email, "password", password);
	}
}
