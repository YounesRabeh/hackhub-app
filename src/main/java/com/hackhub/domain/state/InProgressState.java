package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

public class InProgressState implements HackathonState {

	@Override
	public HackathonStatus status() {
		return HackathonStatus.IN_PROGRESS;
	}

	@Override
	public boolean canTransitionTo(HackathonStatus targetStatus) {
		return targetStatus == HackathonStatus.IN_PROGRESS ||
			targetStatus == HackathonStatus.EVALUATION;
	}

	@Override
	public boolean canRegisterTeam() {
		return false;
	}

	@Override
	public boolean canSubmit() {
		return true;
	}

	@Override
	public boolean canEvaluate() {
		return false;
	}

	@Override
	public boolean allowsWriteOperations() {
		return true;
	}
}
