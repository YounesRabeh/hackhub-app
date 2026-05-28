package com.hackhub.unit.domain.state;

import com.hackhub.domain.enums.HackathonStatus;
import com.hackhub.domain.state.HackathonState;
import com.hackhub.domain.state.HackathonStateFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HackathonStateFactoryTest {

	private final HackathonStateFactory factory = new HackathonStateFactory();

	@Test
	void registrationOpenAllowsOnlyTeamRegistrationAndProgressTransition() {
		HackathonState state = factory.fromStatus(HackathonStatus.REGISTRATION_OPEN);

		assertThat(state.canRegisterTeam()).isTrue();
		assertThat(state.canSubmit()).isFalse();
		assertThat(state.canEvaluate()).isFalse();
		assertThat(state.canTransitionTo(HackathonStatus.IN_PROGRESS)).isTrue();
		assertThat(state.canTransitionTo(HackathonStatus.FINISHED)).isFalse();
	}

	@Test
	void inProgressAllowsOnlySubmissionsAndEvaluationTransition() {
		HackathonState state = factory.fromStatus(HackathonStatus.IN_PROGRESS);

		assertThat(state.canRegisterTeam()).isFalse();
		assertThat(state.canSubmit()).isTrue();
		assertThat(state.canEvaluate()).isFalse();
		assertThat(state.canTransitionTo(HackathonStatus.EVALUATION)).isTrue();
		assertThat(state.canTransitionTo(HackathonStatus.FINISHED)).isFalse();
	}

	@Test
	void evaluationAllowsOnlyEvaluationsAndFinishedTransition() {
		HackathonState state = factory.fromStatus(HackathonStatus.EVALUATION);

		assertThat(state.canRegisterTeam()).isFalse();
		assertThat(state.canSubmit()).isFalse();
		assertThat(state.canEvaluate()).isTrue();
		assertThat(state.canTransitionTo(HackathonStatus.FINISHED)).isTrue();
		assertThat(state.canTransitionTo(HackathonStatus.IN_PROGRESS)).isFalse();
	}

	@Test
	void finishedRejectsLifecycleWrites() {
		HackathonState state = factory.fromStatus(HackathonStatus.FINISHED);

		assertThat(state.canRegisterTeam()).isFalse();
		assertThat(state.canSubmit()).isFalse();
		assertThat(state.canEvaluate()).isFalse();
		assertThat(state.allowsWriteOperations()).isFalse();
		assertThat(state.canTransitionTo(HackathonStatus.EVALUATION)).isFalse();
	}
}
