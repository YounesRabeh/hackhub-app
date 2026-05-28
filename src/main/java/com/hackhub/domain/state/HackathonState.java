package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

public interface HackathonState {

	HackathonStatus status();

	boolean canTransitionTo(HackathonStatus targetStatus);

	boolean canRegisterTeam();

	boolean canSubmit();

	boolean canEvaluate();

	boolean allowsWriteOperations();
}
