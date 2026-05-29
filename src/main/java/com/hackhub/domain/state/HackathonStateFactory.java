package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;
import org.springframework.stereotype.Component;

/**
 * Factory responsible for providing the appropriate {@link HackathonState}
 * instance for a given {@link HackathonStatus}.
 *
 * <p>This class centralizes the mapping between hackathon statuses and their
 * corresponding state implementations, supporting the State design pattern
 * used to manage the hackathon lifecycle.</p>
 */
@Component
public class HackathonStateFactory {

	/**
	 * State representing a hackathon that is open for registrations.
	 */
	private final HackathonState registrationOpen = new RegistrationOpenState();

	/**
	 * State representing a hackathon currently in progress.
	 */
	private final HackathonState inProgress = new InProgressState();

	/**
	 * State representing a hackathon currently under evaluation.
	 */
	private final HackathonState evaluation = new EvaluationState();

	/**
	 * State representing a completed hackathon.
	 */
	private final HackathonState finished = new FinishedState();

	/**
	 * Returns the state instance associated with the specified status.
	 *
	 * @param status the hackathon status
	 * @return the corresponding state implementation
	 */
	public HackathonState fromStatus(HackathonStatus status) {
		return switch (status) {
			case REGISTRATION_OPEN -> registrationOpen;
			case IN_PROGRESS -> inProgress;
			case EVALUATION -> evaluation;
			case FINISHED -> finished;
		};
	}
}