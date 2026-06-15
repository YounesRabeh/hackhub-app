package com.hackhub.unit.application.service;

import com.hackhub.application.service.StaffAccessService;
import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.enums.Role;
import com.hackhub.domain.model.Hackathon;
import com.hackhub.domain.model.User;
import com.hackhub.testsupport.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StaffAccessService}.
 *
 * <p>These tests prove staff access is scoped to the specific hackathon where
 * the user is assigned. Having the right role in one hackathon must not grant
 * access to another hackathon.</p>
 */
class StaffAccessServiceTest {

	private StaffAccessService staffAccessService;

	/**
	 * Creates the pure service under test.
	 */
	@BeforeEach
	void setUp() {
		staffAccessService = new StaffAccessService();
	}

	/**
	 * Verifies judge access does not cross hackathon boundaries.
	 */
	@Test
	void judgeAccessIsScopedToAssignedHackathon() {
		User judge = TestDataFactory.user(1L, Role.JUDGE);
		Hackathon assignedHackathon = TestDataFactory.hackathon(
			10L,
			TestDataFactory.user(2L, Role.ORGANIZER),
			HackathonStatus.EVALUATION
		);
		Hackathon otherHackathon = TestDataFactory.hackathon(
			11L,
			TestDataFactory.user(3L, Role.ORGANIZER),
			HackathonStatus.EVALUATION
		);
		assignedHackathon.getJudges().add(judge);

		assertThat(staffAccessService.isJudgeOf(judge, assignedHackathon)).isTrue();
		assertThat(staffAccessService.isJudgeOf(judge, otherHackathon)).isFalse();
		assertThat(staffAccessService.canAccessSubmissions(judge, otherHackathon)).isFalse();
	}

	/**
	 * Verifies mentor access does not cross hackathon boundaries.
	 */
	@Test
	void mentorAccessIsScopedToAssignedHackathon() {
		User mentor = TestDataFactory.user(1L, Role.MENTOR);
		Hackathon assignedHackathon = TestDataFactory.hackathon(
			10L,
			TestDataFactory.user(2L, Role.ORGANIZER),
			HackathonStatus.IN_PROGRESS
		);
		Hackathon otherHackathon = TestDataFactory.hackathon(
			11L,
			TestDataFactory.user(3L, Role.ORGANIZER),
			HackathonStatus.IN_PROGRESS
		);
		assignedHackathon.getMentors().add(mentor);

		assertThat(staffAccessService.isMentorOf(mentor, assignedHackathon)).isTrue();
		assertThat(staffAccessService.isMentorOf(mentor, otherHackathon)).isFalse();
		assertThat(staffAccessService.canAccessSubmissions(mentor, otherHackathon)).isFalse();
	}

	/**
	 * Verifies organizer access is limited to the hackathon they own.
	 */
	@Test
	void organizerAccessIsScopedToOwnedHackathon() {
		User organizer = TestDataFactory.user(1L, Role.ORGANIZER);
		Hackathon ownedHackathon = TestDataFactory.hackathon(
			10L,
			organizer,
			HackathonStatus.REGISTRATION_OPEN
		);
		Hackathon otherHackathon = TestDataFactory.hackathon(
			11L,
			TestDataFactory.user(2L, Role.ORGANIZER),
			HackathonStatus.REGISTRATION_OPEN
		);

		assertThat(staffAccessService.isOrganizerOf(organizer, ownedHackathon)).isTrue();
		assertThat(staffAccessService.isOrganizerOf(organizer, otherHackathon)).isFalse();
		assertThat(staffAccessService.canAccessSubmissions(organizer, otherHackathon)).isFalse();
	}
}
