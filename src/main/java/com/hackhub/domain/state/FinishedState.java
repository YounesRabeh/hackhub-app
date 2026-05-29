package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

/**
 * Represents the terminal state of a hackathon.
 *
 * <p>In this state, the hackathon lifecycle has been completed. No further
 * registrations, submissions, evaluations, or modifications are permitted.
 * The hackathon remains permanently in the finished state.</p>
 */
public class FinishedState implements HackathonState {

	/**
	 * Returns the status represented by this state.
	 *
	 * @return {@link HackathonStatus#FINISHED}
	 */
	@Override
	public HackathonStatus status() {
		return HackathonStatus.FINISHED;
	}

	/**
	 * Determines whether a transition to the specified status is allowed.
	 *
	 * <p>A finished hackathon cannot transition to any other state and may
	 * only remain in the finished state.</p>
	 *
	 * @param targetStatus the desired target status
	 * @return {@code true} if the target status is {@code FINISHED},
	 *         {@code false} otherwise
	 */
	@Override
	public boolean canTransitionTo(HackathonStatus targetStatus) {
		return targetStatus == HackathonStatus.FINISHED;
	}

	/**
	 * Indicates whether team registration is permitted.
	 *
	 * @return {@code false}, as the hackathon has concluded
	 */
	@Override
	public boolean canRegisterTeam() {
		return false;
	}

	/**
	 * Indicates whether teams can submit or update their submissions.
	 *
	 * @return {@code false}, as the submission phase has ended
	 */
	@Override
	public boolean canSubmit() {
		return false;
	}

	/**
	 * Indicates whether submitted projects can be evaluated.
	 *
	 * @return {@code false}, as the evaluation process has been completed
	 */
	@Override
	public boolean canEvaluate() {
		return false;
	}

	/**
	 * Indicates whether write operations affecting the hackathon are allowed.
	 *
	 * @return {@code false}, as the hackathon is immutable once finished
	 */
	@Override
	public boolean allowsWriteOperations() {
		return false;
	}
}