package com.hackhub.infrastructure.seed;

import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.User;
import com.hackhub.infrastructure.repository.HackathonRepository;
import com.hackhub.infrastructure.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with demo data for local development environments.
 *
 * <p>This component is executed automatically at application startup when the
 * {@code dev} Spring profile is active. It creates a predefined set of users
 * with different roles and a sample hackathon to simplify development,
 * testing, and demonstrations.</p>
 *
 * <p>The seeding process is idempotent: existing users and hackathons are not
 * recreated if they are already present in the database.</p>
 *
 * <p>All seeded users share the same demo password, which is encoded using the
 * configured {@link PasswordEncoder} before being persisted.</p>
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

	/**
	 * Plain-text password assigned to all seeded demo users.
	 */
	private static final String DEMO_PASSWORD = "Password123!";

	private final UserRepository userRepository;
	private final HackathonRepository hackathonRepository;
	private final PasswordEncoder passwordEncoder;


	/**
	 * Executes the development data seeding process.
	 *
	 * <p>Creates a predefined set of demo users if they do not already exist
	 * and creates a sample hackathon if the database does not contain any
	 * hackathons.</p>
	 *
	 * @param args application startup arguments
	 */	
	@Override
	public void run(String... args) {
		Map<String, Role> users = Map.ofEntries(
			Map.entry("organizer@example.com", Role.ORGANIZER),
			Map.entry("judge@example.com", Role.JUDGE),
			Map.entry("mentor1@example.com", Role.MENTOR),
			Map.entry("mentor2@example.com", Role.MENTOR),
			Map.entry("user1@example.com", Role.USER),
			Map.entry("user2@example.com", Role.USER),
			Map.entry("user3@example.com", Role.USER)
		);

		users.forEach(this::seedUserIfMissing);
		seedDemoHackathonIfMissing();
	}

	/**
	 * Creates a demo user with the specified email and role if no user with
	 * the same email already exists.
	 *
	 * @param email the email address of the user to create
	 * @param role the role assigned to the user
	 */
	private void seedUserIfMissing(String email, Role role) {
		if (userRepository.existsByEmail(email)) {
			return;
		}

		User user = new User();
		user.setEmail(email);
		user.setRole(role);
		user.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
		userRepository.save(user);
	}

	/**
	 * Creates a sample hackathon if no hackathons currently exist in the
	 * database.
	 *
	 * <p>The hackathon is associated with the seeded organizer account and is
	 * initialized in the {@link HackathonStatus#REGISTRATION_OPEN} state with
	 * dates relative to the current application startup time.</p>
	 */
	private void seedDemoHackathonIfMissing() {
		if (hackathonRepository.count() > 0) {
			return;
		}

		User organizer = userRepository
			.findByEmail("organizer@example.com")
			.orElse(null);
		if (organizer == null) {
			return;
		}

		LocalDateTime now = LocalDateTime.now();
		Hackathon hackathon = new Hackathon();
		hackathon.setTitle("HackHub Demo Hackathon");
		hackathon.setDescription("Demo hackathon seeded for local development.");
		hackathon.setRegistrationDeadline(now.plusDays(10));
		hackathon.setSubmissionDeadline(now.plusDays(20));
		hackathon.setStartAt(now.plusDays(12));
		hackathon.setEndAt(now.plusDays(25));
		hackathon.setStatus(HackathonStatus.REGISTRATION_OPEN);
		hackathon.setPrizeAmount(new BigDecimal("1000.00"));
		hackathon.setOrganizer(organizer);
		hackathonRepository.save(hackathon);
	}
}
