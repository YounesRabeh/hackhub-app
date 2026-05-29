package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

/**
 * Represents the state of a hackathon that is currently under evaluation.
 *
 * <p>In this state, team registrations and submission updates are no longer
 * permitted. The assigned judge can evaluate submitted projects, and the
 * hackathon may transition to the finished state once all evaluations have
 * been completed.</p>
 */
public class EvaluationState implements HackathonState {

	/**
	 * Returns the status represented by this state.
	 *
	 * @return {@link HackathonStatus#EVALUATION}
	 */
	@Override
	public HackathonStatus status() {
		return HackathonStatus.EVALUATION;
	}

	/**
	 * Determines whether a transition to the specified status is allowed.
	 *
	 * <p>A hackathon under evaluation may remain in the same state or
	 * transition to the finished state.</p>
	 *
	 * @param targetStatus the desired target status
	 * @return {@code true} if the transition is allowed, {@code false} otherwise
	 */
	@Override
	public boolean canTransitionTo(HackathonStatus targetStatus) {
		return targetStatus == HackathonStatus.EVALUATION ||
			targetStatus == HackathonStatus.FINISHED;
	}

	/**
	 * Indicates whether team registration is permitted.
	 *
	 * @return {@code false}, as registrations are closed during evaluation
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
	 * @return {@code true}, as evaluation activities are performed in this state
	 */
	@Override
	public boolean canEvaluate() {
		return true;
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