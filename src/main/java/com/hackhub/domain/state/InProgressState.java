package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

/**
 * Represents the state of a hackathon that is currently in progress.
 *
 * <p>In this state, team registrations are closed, but registered teams
 * are allowed to submit or update their submissions. The hackathon can
 * transition to the evaluation phase once the event has concluded.</p>
 */
public class InProgressState implements HackathonState {

	/**
	 * Returns the status represented by this state.
	 *
	 * @return {@link HackathonStatus#IN_PROGRESS}
	 */
	@Override
	public HackathonStatus status() {
		return HackathonStatus.IN_PROGRESS;
	}

	/**
	 * Determines whether a transition to the specified status is allowed.
	 *
	 * <p>A hackathon in progress may remain in the same state or move
	 * to the evaluation phase.</p>
	 *
	 * @param targetStatus the desired target status
	 * @return {@code true} if the transition is allowed, {@code false} otherwise
	 */
	@Override
	public boolean canTransitionTo(HackathonStatus targetStatus) {
		return targetStatus == HackathonStatus.IN_PROGRESS ||
			targetStatus == HackathonStatus.EVALUATION;
	}

	/**
	 * Indicates whether team registration is permitted.
	 *
	 * @return {@code false}, as registrations are closed once the hackathon
	 *         has started
	 */
	@Override
	public boolean canRegisterTeam() {
		return false;
	}

	/**
	 * Indicates whether teams can submit or update their submissions.
	 *
	 * @return {@code true}, as submissions are allowed during the event
	 */
	@Override
	public boolean canSubmit() {
		return true;
	}

	/**
	 * Indicates whether submissions can be evaluated.
	 *
	 * @return {@code false}, as evaluation begins only after the hackathon
	 *         enters the evaluation phase
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