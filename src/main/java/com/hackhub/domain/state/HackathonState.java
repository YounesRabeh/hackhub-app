package com.hackhub.domain.state;

import com.hackhub.domain.enums.HackathonStatus;

/**
 * Defines the behavior of a hackathon state within the hackathon lifecycle.
 *
 * <p>Implementations of this interface encapsulate the rules associated with a
 * specific {@link HackathonStatus}, including allowed state transitions and
 * permitted operations such as team registration, submission management, and
 * evaluation activities.</p>
 *
 * <p>This interface is part of the State design pattern implementation used
 * to manage the lifecycle of a hackathon.</p>
 */
public interface HackathonState {

	/**
	 * Returns the status represented by this state.
	 *
	 * @return the corresponding hackathon status
	 */
	HackathonStatus status();

	/**
	 * Determines whether a transition from the current state to the specified
	 * target status is allowed.
	 *
	 * @param targetStatus the desired target status
	 * @return {@code true} if the transition is allowed, {@code false} otherwise
	 */
	boolean canTransitionTo(HackathonStatus targetStatus);

	/**
	 * Indicates whether team registration is currently permitted.
	 *
	 * @return {@code true} if teams can register, {@code false} otherwise
	 */
	boolean canRegisterTeam();

	/**
	 * Indicates whether teams can submit or update their submissions.
	 *
	 * @return {@code true} if submissions are allowed, {@code false} otherwise
	 */
	boolean canSubmit();

	/**
	 * Indicates whether hackathon submissions can be evaluated.
	 *
	 * @return {@code true} if evaluation is allowed, {@code false} otherwise
	 */
	boolean canEvaluate();

	/**
	 * Indicates whether write operations affecting the hackathon are allowed
	 * in the current state.
	 *
	 * @return {@code true} if write operations are permitted, {@code false}
	 *         otherwise
	 */
	boolean allowsWriteOperations();
}