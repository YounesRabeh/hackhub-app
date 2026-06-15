package com.hackhub.integration.system;

import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.external.calendar.CalendarBookingRequest;
import com.hackhub.infrastructure.external.calendar.CalendarClient;
import com.hackhub.infrastructure.external.calendar.FakeCalendarClient;
import com.hackhub.infrastructure.external.payment.FakePaymentClient;
import com.hackhub.infrastructure.external.payment.PaymentClient;
import com.hackhub.infrastructure.external.payment.PaymentRequest;
import com.hackhub.testsupport.IntegrationTestSupport;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SystemHttpIntegrationTest extends IntegrationTestSupport {

	private static final Set<String> EXPECTED_CONTROLLER_ROUTES = Set.of(
		"GET /api/auth/me",
		"POST /api/auth/login",
		"POST /api/auth/register",
		"POST /api/hackathons",
		"GET /api/hackathons",
		"GET /api/hackathons/{hackathonId}",
		"POST /api/hackathons/{hackathonId}/judges/{judgeId}",
		"POST /api/hackathons/{hackathonId}/mentors/{mentorId}",
		"GET /api/hackathons/{hackathonId}/registrations",
		"POST /api/hackathons/{hackathonId}/registrations",
		"GET /api/hackathons/{hackathonId}/submissions",
		"GET /api/hackathons/{hackathonId}/submissions/my-team",
		"PUT /api/hackathons/{hackathonId}/submissions/my-team",
		"PATCH /api/hackathons/{hackathonId}/status",
		"POST /api/hackathons/{hackathonId}/support-requests",
		"GET /api/hackathons/{hackathonId}/support-requests",
		"POST /api/hackathons/{hackathonId}/rule-violations",
		"GET /api/hackathons/{hackathonId}/rule-violations",
		"POST /api/hackathons/{hackathonId}/winner",
		"POST /api/invitations/{invitationId}/accept",
		"POST /api/invitations/{invitationId}/decline",
		"POST /api/submissions/{submissionId}/evaluation",
		"GET /api/hackathons/{hackathonId}/evaluations",
		"POST /api/support-requests/{supportRequestId}/call-proposal",
		"POST /api/teams",
		"GET /api/teams/me",
		"POST /api/teams/{teamId}/invitations"
	);

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private PaymentClient paymentClient;

	@Autowired
	private CalendarClient calendarClient;

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	void allControllerRoutesAreMappedThroughSpringMvc() {
		Set<String> controllerRoutes = requestMappingHandlerMapping
			.getHandlerMethods()
			.entrySet()
			.stream()
			.filter(entry -> entry
				.getValue()
				.getBeanType()
				.getPackageName()
				.startsWith("com.hackhub.api.controller"))
			.flatMap(entry -> entry
				.getKey()
				.getPatternValues()
				.stream()
				.flatMap(pattern -> entry
					.getKey()
					.getMethodsCondition()
					.getMethods()
					.stream()
					.map(method -> method + " " + pattern)))
			.collect(Collectors.toSet());

		assertThat(controllerRoutes).containsExactlyInAnyOrderElementsOf(EXPECTED_CONTROLLER_ROUTES);
	}

	@Test
	void validationErrorsUseStandardApiErrorShape() throws Exception {
		postJson("/api/auth/register", registerPayload("not-an-email", "short"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.timestamp").isString())
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.error").value("Bad Request"))
			.andExpect(jsonPath("$.message").value("Validation failed"))
			.andExpect(jsonPath("$.path").value("/api/auth/register"))
			.andExpect(jsonPath("$.details", Matchers.hasItem(Matchers.containsString("email"))))
			.andExpect(jsonPath("$.details", Matchers.hasItem(Matchers.containsString("password"))));
	}

	@Test
	void notFoundErrorsUseStandardApiErrorShape() throws Exception {
		get("/api/hackathons/999999")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.timestamp").isString())
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.error").value("Not Found"))
			.andExpect(jsonPath("$.message").value("Hackathon not found"))
			.andExpect(jsonPath("$.path").value("/api/hackathons/999999"))
			.andExpect(jsonPath("$.details").isArray())
			.andExpect(jsonPath("$.details").isEmpty());
	}

	@Test
	void conflictErrorsUseStandardApiErrorShape() throws Exception {
		User user = saveUser(Role.USER);
		String token = tokenFor(user);

		postJsonWithBearer("/api/teams", token, createTeamPayload("System Conflict Team"))
			.andExpect(status().isCreated());

		postJsonWithBearer("/api/teams", token, createTeamPayload("Second System Conflict Team"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.timestamp").isString())
			.andExpect(jsonPath("$.status").value(409))
			.andExpect(jsonPath("$.error").value("Conflict"))
			.andExpect(jsonPath("$.message").value("User already belongs to a team"))
			.andExpect(jsonPath("$.path").value("/api/teams"))
			.andExpect(jsonPath("$.details").isArray())
			.andExpect(jsonPath("$.details").isEmpty());
	}

	@Test
	void unauthorizedErrorsUseStandardApiErrorShape() throws Exception {
		postJson("/api/auth/login", loginPayload(uniqueEmail(), PASSWORD))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.timestamp").isString())
			.andExpect(jsonPath("$.status").value(401))
			.andExpect(jsonPath("$.error").value("Unauthorized"))
			.andExpect(jsonPath("$.message").value("Authentication failed"))
			.andExpect(jsonPath("$.path").value("/api/auth/login"))
			.andExpect(jsonPath("$.details").isArray())
			.andExpect(jsonPath("$.details").isEmpty());
	}

	@Test
	void forbiddenErrorsUseStandardApiErrorShape() throws Exception {
		User user = saveUser(Role.USER);
		String token = tokenFor(user);

		postJsonWithBearer("/api/hackathons", token, validHackathonPayload())
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.timestamp").isString())
			.andExpect(jsonPath("$.status").value(403))
			.andExpect(jsonPath("$.error").value("Forbidden"))
			.andExpect(jsonPath("$.message").value("Only organizers can create hackathons"))
			.andExpect(jsonPath("$.path").value("/api/hackathons"))
			.andExpect(jsonPath("$.details").isArray())
			.andExpect(jsonPath("$.details").isEmpty());
	}

	@Test
	void databaseSchemaIsCreatedForDefaultProfile() throws Exception {
		assertThat(userRepository.count()).isZero();
		assertThat(teamRepository.count()).isZero();
		assertThat(hackathonRepository.count()).isZero();

		assertThat(tableExists("users")).isTrue();
		assertThat(tableExists("teams")).isTrue();
		assertThat(tableExists("hackathons")).isTrue();
		assertThat(tableExists("hackathon_registrations")).isTrue();
		assertThat(tableExists("submissions")).isTrue();
	}

	@Test
	void publicHackathonListWorksWithEmptyDatabase() throws Exception {
		assertThat(hackathonRepository.count()).isZero();

		get("/api/hackathons")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void fakeExternalClientsAreActiveInDefaultTestContext() {
		assertThat(applicationContext.getBeanNamesForType(PaymentClient.class)).hasSize(1);
		assertThat(applicationContext.getBeanNamesForType(CalendarClient.class)).hasSize(1);
		assertThat(paymentClient).isInstanceOf(FakePaymentClient.class);
		assertThat(calendarClient).isInstanceOf(FakeCalendarClient.class);

		assertThat(paymentClient.payPrize(
			new PaymentRequest("System Test Hackathon", 1L, new BigDecimal("1000.00"))
		).externalPaymentId()).startsWith("fake-pay-");
		assertThat(calendarClient.bookCall(new CalendarBookingRequest(
			"System support call",
			LocalDateTime.now().plusDays(1),
			"user@example.com",
			"mentor@example.com"
		)).externalCallId()).startsWith("fake-call-");
	}

	private boolean tableExists(String tableName) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metadata = connection.getMetaData();
			try (ResultSet tables = metadata.getTables(null, null, tableName.toUpperCase(), null)) {
				return tables.next();
			}
		}
	}

	private Map<String, Object> validHackathonPayload() {
		LocalDateTime now = LocalDateTime.now().plusDays(3);
		return Map.of(
			"title", "System Test Hackathon",
			"description", "System-level error shape test",
			"registrationDeadline", now.plusDays(1),
			"submissionDeadline", now.plusDays(5),
			"startAt", now.plusDays(2),
			"endAt", now.plusDays(7),
			"prizeAmount", new BigDecimal("1000.00")
		);
	}
}
