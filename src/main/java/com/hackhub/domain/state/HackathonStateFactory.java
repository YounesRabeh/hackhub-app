package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;
import org.springframework.stereotype.Component;

@Component
public class HackathonStateFactory {

	private final HackathonState registrationOpen = new RegistrationOpenState();
	private final HackathonState inProgress = new InProgressState();
	private final HackathonState evaluation = new EvaluationState();
	private final HackathonState finished = new FinishedState();

	public HackathonState fromStatus(HackathonStatus status) {
		return switch (status) {
			case REGISTRATION_OPEN -> registrationOpen;
			case IN_PROGRESS -> inProgress;
			case EVALUATION -> evaluation;
			case FINISHED -> finished;
		};
	}
}
