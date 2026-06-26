package com.hackhub.integration.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackhub.domain.enums.Role;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import com.hackhub.infrastructure.seed.DevDataSeeder;
import com.hackhub.infrastructure.external.calendar.CalendarBookingRequest;
import com.hackhub.infrastructure.external.calendar.CalendarClient;
import com.hackhub.infrastructure.external.calendar.FakeCalendarClient;
import com.hackhub.infrastructure.external.payment.FakePaymentClient;
import com.hackhub.infrastructure.external.payment.PaymentClient;
import com.hackhub.infrastructure.external.payment.PaymentRequest;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DevProfileSystemIntegrationTest {

	private static final String DEMO_PASSWORD = "Password123!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private PaymentClient paymentClient;

	@Autowired
	private CalendarClient calendarClient;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private HackathonRepository hackathonRepository;

	@Autowired
	private DevDataSeeder devDataSeeder;

	@Test
	void openApiAndSwaggerAreAvailableInDevProfile() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.openapi").isString())
			.andExpect(jsonPath("$.paths['/api/auth/me'].get.security[0].bearerAuth").isArray())
			.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
			.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));

		mockMvc.perform(get("/swagger-ui.html"))
			.andExpect(status().is3xxRedirection());
	}

	@Test
	void databaseSchemaIsCreatedForDevProfile() throws Exception {
		assertThat(tableExists("users")).isTrue();
		assertThat(tableExists("teams")).isTrue();
		assertThat(tableExists("hackathons")).isTrue();
		assertThat(tableExists("payment_transactions")).isTrue();
		assertThat(tableExists("mentor_call_proposals")).isTrue();
	}

	@Test
	void seededDevUsersCanLogInWithDocumentedCredentials() throws Exception {
		Map<String, Role> documentedUsers = Map.ofEntries(
			Map.entry("organizer@example.com", Role.ORGANIZER),
			Map.entry("judge@example.com", Role.JUDGE),
			Map.entry("mentor1@example.com", Role.MENTOR),
			Map.entry("mentor2@example.com", Role.MENTOR),
			Map.entry("user1@example.com", Role.USER),
			Map.entry("user2@example.com", Role.USER),
			Map.entry("user3@example.com", Role.USER)
		);

		for (Map.Entry<String, Role> entry : documentedUsers.entrySet()) {
			mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(Map.of(
					"email", entry.getKey(),
					"password", DEMO_PASSWORD
				))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isString())
				.andExpect(jsonPath("$.user.email").value(entry.getKey()))
				.andExpect(jsonPath("$.user.role").value(entry.getValue().name()));
		}
	}

	@Test
	void seededDemoHackathonAppearsOnceWhenDevDatabaseStartsEmpty() throws Exception {
		assertThat(hackathonRepository.count()).isEqualTo(1);

		mockMvc.perform(get("/api/hackathons"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(1))
			.andExpect(jsonPath("$[0].title").value("HackHub Demo Hackathon"))
			.andExpect(jsonPath("$[0].description").value("Demo hackathon seeded for local development."))
			.andExpect(jsonPath("$[0].status").value("REGISTRATION_OPEN"))
			.andExpect(jsonPath("$[0].prizeAmount").value(comparesEqualTo(1000.0)))
			.andExpect(jsonPath("$[0].winnerTeamId").isEmpty());
	}

	@Test
	void devSeederCanRunRepeatedlyWithoutDuplicateSeedData() throws Exception {
		long userCount = userRepository.count();
		long hackathonCount = hackathonRepository.count();

		devDataSeeder.run();
		devDataSeeder.run();

		assertThat(userRepository.count()).isEqualTo(userCount);
		assertThat(hackathonRepository.count()).isEqualTo(hackathonCount);
		assertThat(userRepository.findByEmail("organizer@example.com")).isPresent();
		assertThat(hackathonRepository.findAll())
			.extracting("title")
			.containsExactly("HackHub Demo Hackathon");
	}

	@Test
	void fakeExternalClientsAreOnlyClientImplementationsInDevProfile() {
		assertThat(applicationContext.getBeanNamesForType(PaymentClient.class)).hasSize(1);
		assertThat(applicationContext.getBeanNamesForType(CalendarClient.class)).hasSize(1);
		assertThat(paymentClient).isInstanceOf(FakePaymentClient.class);
		assertThat(calendarClient).isInstanceOf(FakeCalendarClient.class);

		PaymentRequest paymentRequest = new PaymentRequest(
			"Dev Profile Hackathon",
			1L,
			new BigDecimal("1000.00")
		);
		assertThat(paymentClient.payPrize(paymentRequest).externalPaymentId())
			.startsWith("fake-pay-")
			.isEqualTo(paymentClient.payPrize(paymentRequest).externalPaymentId());

		CalendarBookingRequest bookingRequest = new CalendarBookingRequest(
			"Dev support call",
			LocalDateTime.now().plusDays(1),
			"user@example.com",
			"mentor@example.com"
		);
		var bookingResponse = calendarClient.bookCall(bookingRequest);
		assertThat(bookingResponse.externalCallId())
			.startsWith("fake-call-")
			.isEqualTo(calendarClient.bookCall(bookingRequest).externalCallId());
		assertThat(bookingResponse.bookingUrl())
			.isEqualTo("https://calendar.fake.local/bookings/" + bookingResponse.externalCallId());
	}

	private boolean tableExists(String tableName) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metadata = connection.getMetaData();
			try (ResultSet tables = metadata.getTables(null, null, tableName.toUpperCase(), null)) {
				return tables.next();
			}
		}
	}
}
