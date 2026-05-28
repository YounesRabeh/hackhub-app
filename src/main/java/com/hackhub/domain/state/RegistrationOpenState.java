package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

public class RegistrationOpenState implements HackathonState {

	@Override
	public HackathonStatus status() {
		return HackathonStatus.REGISTRATION_OPEN;
	}

	@Override
	public boolean canTransitionTo(HackathonStatus targetStatus) {
		return targetStatus == HackathonStatus.REGISTRATION_OPEN ||
			targetStatus == HackathonStatus.IN_PROGRESS;
	}

	@Override
	public boolean canRegisterTeam() {
		return true;
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
		return true;
	}
}
