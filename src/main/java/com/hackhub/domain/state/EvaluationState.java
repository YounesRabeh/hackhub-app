package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

public class EvaluationState implements HackathonState {

	@Override
	public HackathonStatus status() {
		return HackathonStatus.EVALUATION;
	}

	@Override
	public boolean canTransitionTo(HackathonStatus targetStatus) {
		return targetStatus == HackathonStatus.EVALUATION ||
			targetStatus == HackathonStatus.FINISHED;
	}

	@Override
	public boolean canRegisterTeam() {
		return false;
	}

	@Override
	public boolean canSubmit() {
		return false;
	}

	@Override
	public boolean canEvaluate() {
		return true;
	}

	@Override
	public boolean allowsWriteOperations() {
		return true;
	}
}
