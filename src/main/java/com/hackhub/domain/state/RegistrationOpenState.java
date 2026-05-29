package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

/**
 * Represents the state of a hackathon that is open for registrations.
 *
 * <p>In this state, users can register their teams for participation.
 * Submissions and evaluations are not yet permitted. The hackathon may
 * remain open for registrations or transition to the in-progress phase
 * when the event begins.</p>
 */
public class RegistrationOpenState implements HackathonState {

	/**
	 * Returns the status represented by this state.
	 *
	 * @return {@link HackathonStatus#REGISTRATION_OPEN}
	 */
	@Override
	public HackathonStatus status() {
		return HackathonStatus.REGISTRATION_OPEN;
	}

	/**
	 * Determines whether a transition to the specified status is allowed.
	 *
	 * <p>A hackathon open for registration may remain in the same state
	 * or transition to the in-progress phase.</p>
	 *
	 * @param targetStatus the desired target status
	 * @return {@code true} if the transition is allowed, {@code false} otherwise
	 */
	@Override
	public boolean canTransitionTo(HackathonStatus targetStatus) {
		return targetStatus == HackathonStatus.REGISTRATION_OPEN ||
			targetStatus == HackathonStatus.IN_PROGRESS;
	}

	/**
	 * Indicates whether team registration is permitted.
	 *
	 * @return {@code true}, as registrations are open in this state
	 */
	@Override
	public boolean canRegisterTeam() {
		return true;
	}

	/**
	 * Indicates whether teams can submit or update their submissions.
	 *
	 * @return {@code false}, as the hackathon has not started yet
	 */
	@Override
	public boolean canSubmit() {
		return false;
	}

	/**
	 * Indicates whether submissions can be evaluated.
	 *
	 * @return {@code false}, as evaluation is only available during the
	 *         evaluation phase
	 */
	@Override
	public boolean canEvaluate() {
		return false;
	}

	/**
	 * Indicates whether write operations affecting the hackathon are allowed.
	 *
	 * @return {@code true}
	 */
	@Override
	public boolean allowsWriteOperations() {
		return true;
	}
}