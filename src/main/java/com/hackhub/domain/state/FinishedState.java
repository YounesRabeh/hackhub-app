package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

public class FinishedState implements HackathonState {

	@Override
	public HackathonStatus status() {
		return HackathonStatus.FINISHED;
	}

	@Override
	public boolean canTransitionTo(HackathonStatus targetStatus) {
		return targetStatus == HackathonStatus.FINISHED;
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
		return false;
	}

	@Override
	public boolean allowsWriteOperations() {
		return false;
	}
}
