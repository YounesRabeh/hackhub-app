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

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

	private static final String DEMO_PASSWORD = "Password123!";

	private final UserRepository userRepository;
	private final HackathonRepository hackathonRepository;
	private final PasswordEncoder passwordEncoder;

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
