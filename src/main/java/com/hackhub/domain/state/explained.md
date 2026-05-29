# Hackathon State Management

## Overview

This package implements the **State Design Pattern** to manage the lifecycle of a Hackathon.

Instead of scattering status checks throughout the codebase (e.g. `if (status == REGISTRATION_OPEN)`), each hackathon status is represented by a dedicated state object that encapsulates the business rules associated with that phase.

The lifecycle currently consists of the following states:

```
REGISTRATION_OPEN
        |
        v
   IN_PROGRESS
        |
        v
    EVALUATION
        |
        v
     FINISHED
```

Each state defines:

* Which transitions are allowed.
* Whether team registration is allowed.
* Whether submissions are allowed.
* Whether evaluations are allowed.
* Whether write operations are allowed.

---

## Components

### HackathonState

The `HackathonState` interface defines the contract that all states must implement.

```java
public interface HackathonState {
    HackathonStatus status();
    boolean canTransitionTo(HackathonStatus targetStatus);
    boolean canRegisterTeam();
    boolean canSubmit();
    boolean canEvaluate();
    boolean allowsWriteOperations();
}
```

Each implementation represents the behavior of a specific hackathon phase.

---

### RegistrationOpenState

Represents a hackathon that is currently accepting registrations.

Allowed actions:

| Action           | Allowed |
| ---------------- | ------- |
| Register Team    | Yes     |
| Submit Project   | No      |
| Evaluate Project | No      |
| Write Operations | Yes     |

Allowed transitions:

```
REGISTRATION_OPEN -> REGISTRATION_OPEN
REGISTRATION_OPEN -> IN_PROGRESS
```

---

### InProgressState

Represents a hackathon that is currently running.

Allowed actions:

| Action           | Allowed |
| ---------------- | ------- |
| Register Team    | No      |
| Submit Project   | Yes     |
| Evaluate Project | No      |
| Write Operations | Yes     |

Allowed transitions:

```
IN_PROGRESS -> IN_PROGRESS
IN_PROGRESS -> EVALUATION
```

---

### EvaluationState

Represents a hackathon whose submissions are being reviewed by the assigned judge.

Allowed actions:

| Action           | Allowed |
| ---------------- | ------- |
| Register Team    | No      |
| Submit Project   | No      |
| Evaluate Project | Yes     |
| Write Operations | Yes     |

Allowed transitions:

```
EVALUATION -> EVALUATION
EVALUATION -> FINISHED
```

---

### FinishedState

Represents a completed hackathon.

Allowed actions:

| Action           | Allowed |
| ---------------- | ------- |
| Register Team    | No      |
| Submit Project   | No      |
| Evaluate Project | No      |
| Write Operations | No      |

Allowed transitions:

```
FINISHED -> FINISHED
```

This is a terminal state.

---

## HackathonStateFactory

The `HackathonStateFactory` is responsible for mapping a `HackathonStatus` to its corresponding state implementation.

```java
HackathonState state =
    stateFactory.fromStatus(hackathon.getStatus());
```

This avoids direct instantiation of state classes throughout the application and centralizes state resolution.

---

## Usage

Whenever business logic depends on the current status of a hackathon, retrieve the corresponding state through the factory and delegate the decision to the state object.

### Example: Team Registration

```java
HackathonState state =
    stateFactory.fromStatus(hackathon.getStatus());

if (!state.canRegisterTeam()) {
    throw new IllegalStateException(
        "Registrations are not allowed in the current state."
    );
}
```

### Example: Submission

```java
HackathonState state =
    stateFactory.fromStatus(hackathon.getStatus());

if (!state.canSubmit()) {
    throw new IllegalStateException(
        "Submissions are not allowed in the current state."
    );
}
```

### Example: Status Transition

```java
HackathonState currentState =
    stateFactory.fromStatus(hackathon.getStatus());

if (!currentState.canTransitionTo(targetStatus)) {
    throw new IllegalStateException(
        "Invalid status transition."
    );
}
```

---

## Why Use the State Pattern?

Without this package, status-dependent logic would be spread across the application:

```java
if (hackathon.getStatus() == REGISTRATION_OPEN) {
    ...
}
```

This leads to duplicated business rules and makes adding new statuses difficult.

With the State Pattern:

* Business rules are centralized.
* Status behavior is encapsulated.
* New states can be added without modifying existing logic.
* Services depend on abstractions (`HackathonState`) rather than concrete statuses.

This results in cleaner, more maintainable, and more extensible code.
