package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HackathonStateFactoryTest {

	private final HackathonStateFactory factory = new HackathonStateFactory();

	@Test
	void registrationOpenStateRules() {
		HackathonState state = factory.fromStatus(HackathonStatus.REGISTRATION_OPEN);
		assertTrue(state.canRegisterTeam());
		assertFalse(state.canSubmit());
		assertFalse(state.canEvaluate());
		assertTrue(state.canTransitionTo(HackathonStatus.IN_PROGRESS));
		assertFalse(state.canTransitionTo(HackathonStatus.FINISHED));
	}

	@Test
	void inProgressStateRules() {
		HackathonState state = factory.fromStatus(HackathonStatus.IN_PROGRESS);
		assertFalse(state.canRegisterTeam());
		assertTrue(state.canSubmit());
		assertFalse(state.canEvaluate());
		assertTrue(state.canTransitionTo(HackathonStatus.EVALUATION));
		assertFalse(state.canTransitionTo(HackathonStatus.FINISHED));
	}

	@Test
	void evaluationStateRules() {
		HackathonState state = factory.fromStatus(HackathonStatus.EVALUATION);
		assertFalse(state.canRegisterTeam());
		assertFalse(state.canSubmit());
		assertTrue(state.canEvaluate());
		assertTrue(state.canTransitionTo(HackathonStatus.FINISHED));
		assertFalse(state.canTransitionTo(HackathonStatus.IN_PROGRESS));
	}

	@Test
	void finishedStateRules() {
		HackathonState state = factory.fromStatus(HackathonStatus.FINISHED);
		assertFalse(state.canRegisterTeam());
		assertFalse(state.canSubmit());
		assertFalse(state.canEvaluate());
		assertFalse(state.allowsWriteOperations());
		assertFalse(state.canTransitionTo(HackathonStatus.EVALUATION));
	}
}
