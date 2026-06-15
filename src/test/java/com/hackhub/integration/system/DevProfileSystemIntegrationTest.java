package com.hackhub.integration.system;

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
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DevProfileSystemIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private PaymentClient paymentClient;

	@Autowired
	private CalendarClient calendarClient;

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
	void fakeExternalClientsAreOnlyClientImplementationsInDevProfile() {
		assertThat(applicationContext.getBeanNamesForType(PaymentClient.class)).hasSize(1);
		assertThat(applicationContext.getBeanNamesForType(CalendarClient.class)).hasSize(1);
		assertThat(paymentClient).isInstanceOf(FakePaymentClient.class);
		assertThat(calendarClient).isInstanceOf(FakeCalendarClient.class);

		assertThat(paymentClient.payPrize(
			new PaymentRequest("Dev Profile Hackathon", 1L, new BigDecimal("1000.00"))
		).externalPaymentId()).startsWith("fake-pay-");
		assertThat(calendarClient.bookCall(new CalendarBookingRequest(
			"Dev support call",
			LocalDateTime.now().plusDays(1),
			"user@example.com",
			"mentor@example.com"
		)).bookingUrl()).startsWith("https://calendar.fake.local/bookings/fake-call-");
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
