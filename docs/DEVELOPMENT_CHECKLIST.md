# Codex Workflow for HackHub Backend

# `docs/DEVELOPMENT_CHECKLIST.md`

# HackHub Development Checklist


## Global Rules

-  Work on one section at a time.
-  Do not implement frontend code.
-  The application is backend-only.
-  All interactions happen through REST endpoints.
-  Keep controllers thin.
-  Put business rules inside services.
-  Do not expose JPA entities directly from controllers.
-  Use DTOs for all requests and responses.
-  Use transactions on write service methods.
-  Check both user role and hackathon assignment.
-  Use fake external services first.
-  Keep the project compilable after every step.
-  Update this checklist after every completed step.
-  Update the 'NEXT SECTION TO BE DONE' at each finished section to keep track 

//Codex-variable:
NEXT SECTION TO BE DONE: 18
---

## 1. Spring Boot Project Setup

- [x] Create Spring Boot project.
- [x] Use Java 25.
- [x] Add Spring Web.
- [x] Add Spring Data JPA.
- [x] Add Spring Validation.
- [x] Add Spring Security.
- [x] Add H2 database.
- [x] Add PostgreSQL dependency.
- [x] Add JUnit 5 and Mockito.
- [x] Add MockMvc support.
- [x] Add springdoc-openapi.
- [x] Add local Gradle wrapper.
- [x] Configure `application.yml`.
- [x] Configure H2 dev profile.
- [x] Create base package `com.hackhub`.
- [x] Create main class `HackHubApplication`.
- [x] Verify the app starts.

Acceptance criteria:

- [x] App starts successfully.
- [x] H2 console works in dev profile.
- [x] Swagger/OpenAPI works.
- [x] Project builds with `./gradlew build`.
- [x] Project compiles.

---

## 2. Package Structure

- [x] Create `api.controller`.
- [x] Create `api.dto.request`.
- [x] Create `api.dto.response`.
- [x] Create `api.exception`.
- [x] Create `application.service`.
- [x] Create `application.mapper`.
- [x] Create `domain.model`.
- [x] Create `domain.enums`.
- [x] Create `domain.state`.
- [x] Create `infrastructure.repository`.
- [x] Create `infrastructure.external.calendar`.
- [x] Create `infrastructure.external.payment`.
- [x] Create `security`.

Acceptance criteria:

- [x] Package structure matches the architecture.
- [x] Empty project still compiles.

---

## 3. Domain Enums

Create these enums:

- [x] `Role`
- [x] `HackathonStatus`
- [x] `InvitationStatus`
- [x] `SupportRequestStatus`
- [x] `PaymentStatus`

Required values:

```java
public enum Role {
    USER,
    ORGANIZER,
    MENTOR,
    JUDGE,
    ADMIN
}
```

```java
public enum HackathonStatus {
    REGISTRATION_OPEN,
    IN_PROGRESS,
    EVALUATION,
    FINISHED
}
```

```java
public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}
```

```java
public enum SupportRequestStatus {
    OPEN,
    CALL_PROPOSED,
    CLOSED
}
```

```java
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED
}
```

Acceptance criteria:

* [x] All enums compile.
* [x] Enums are placed in `domain.enums`.

---

## 4. JPA Entities

Create these entities:

* [x] `User`
* [x] `Hackathon`
* [x] `Team`
* [x] `TeamInvitation`
* [x] `HackathonRegistration`
* [x] `Submission`
* [x] `Evaluation`
* [x] `SupportRequest`
* [x] `MentorCallProposal`
* [x] `RuleViolationReport`
* [x] `PaymentTransaction`

Acceptance criteria:

* [x] All entities compile.
* [x] Relationships are mapped correctly.
* [x] Unique constraints are added where needed.
* [x] No controller returns entities directly.

---

## 5. Repositories

Create repositories for:

* [x] `UserRepository`
* [x] `HackathonRepository`
* [x] `TeamRepository`
* [x] `TeamInvitationRepository`
* [x] `HackathonRegistrationRepository`
* [x] `SubmissionRepository`
* [x] `EvaluationRepository`
* [x] `SupportRequestRepository`
* [x] `RuleViolationReportRepository`
* [x] `PaymentTransactionRepository`

Important methods:

```java
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
Optional<Team> findByMembersContaining(User user);
boolean existsByHackathonAndTeam(Hackathon hackathon, Team team);
Optional<Submission> findByHackathonAndTeam(Hackathon hackathon, Team team);
Optional<Evaluation> findBySubmission(Submission submission);
```

Acceptance criteria:

* [x] Repositories compile.
* [x] App starts and creates schema.

---

## 6. DTOs and Mappers

Create request DTOs:

* [x] `RegisterRequest`
* [x] `LoginRequest`
* [x] `CreateHackathonRequest`
* [x] `UpdateHackathonStatusRequest`
* [x] `CreateTeamRequest`
* [x] `CreateInvitationRequest`
* [x] `RegisterTeamToHackathonRequest`
* [x] `UpsertSubmissionRequest`
* [x] `CreateEvaluationRequest`
* [x] `CreateSupportRequestRequest`
* [x] `ProposeCallRequest`
* [x] `ReportViolationRequest`
* [x] `DeclareWinnerRequest`

Create response DTOs:

* [x] `AuthResponse`
* [x] `UserResponse`
* [x] `HackathonResponse`
* [x] `TeamResponse`
* [x] `InvitationResponse`
* [x] `SubmissionResponse`
* [x] `EvaluationResponse`
* [x] `SupportRequestResponse`
* [x] `CallProposalResponse`
* [x] `RuleViolationReportResponse`
* [x] `PaymentTransactionResponse`

Create mappers:

* [x] `UserMapper`
* [x] `HackathonMapper`
* [x] `TeamMapper`
* [x] `SubmissionMapper`

Acceptance criteria:

* [x] Controllers and services use DTOs.
* [x] Entities are not exposed directly.
* [x] Validation annotations are used.

---

## 7. Exception Handling

Create:

* [x] `ApiError`
* [x] `GlobalExceptionHandler`
* [x] `NotFoundException`
* [x] `ForbiddenException`
* [x] `BadRequestException`
* [x] `ConflictException`

Map errors:

* [x] `NotFoundException` -> 404
* [x] `ForbiddenException` -> 403
* [x] `BadRequestException` -> 400
* [x] `ConflictException` -> 409
* [x] Validation errors -> 400
* [x] Authentication errors -> 401

Acceptance criteria:

* [x] Errors return structured JSON.
* [x] Validation errors are readable.
* [x] Business errors are explicit.

---

## 8. Authentication and Security

Implement:

* [x] `AuthService`
* [x] `AuthController`
* [x] `JwtService`
* [x] `JwtAuthenticationFilter`
* [x] `CustomUserDetailsService`
* [x] `SecurityConfig`

Endpoints:

* [x] `POST /api/auth/register`
* [x] `POST /api/auth/login`

Public endpoints:

* [x] `POST /api/auth/register`
* [x] `POST /api/auth/login`
* [x] `GET /api/hackathons`
* [x] `GET /api/hackathons/{hackathonId}`

Acceptance criteria:

* [x] Users can register.
* [x] Users can login.
* [x] Login returns JWT.
* [x] Protected endpoints reject missing token.
* [x] Current authenticated user can be loaded.

---

## 9. Hackathon Core

Implement:

* [x] `HackathonService`
* [x] `HackathonController`
* [x] Public hackathon listing.
* [x] Public hackathon detail.
* [x] Organizer hackathon creation.
* [x] Add mentor to hackathon.
* [x] Change hackathon status.

Endpoints:

* [x] `GET /api/hackathons`
* [x] `GET /api/hackathons/{hackathonId}`
* [x] `POST /api/hackathons`
* [x] `POST /api/hackathons/{hackathonId}/mentors/{mentorId}`
* [x] `PATCH /api/hackathons/{hackathonId}/status`

Acceptance criteria:

* [x] Visitors can browse hackathons.
* [x] Only organizers can create hackathons.
* [ ] Judge must have role `JUDGE`.
* [x] Mentors must have role `MENTOR`.
* [x] Invalid dates are rejected.
* [x] Invalid status transitions are rejected.

---

## 10. State Pattern

Implement:

* [x] `HackathonState`
* [x] `RegistrationOpenState`
* [x] `InProgressState`
* [x] `EvaluationState`
* [x] `FinishedState`
* [x] `HackathonStateFactory`

Rules:

* [x] `REGISTRATION_OPEN` allows team registration.
* [x] `IN_PROGRESS` allows submissions.
* [x] `EVALUATION` allows evaluations and winner declaration.
* [x] `FINISHED` rejects write operations.

Acceptance criteria:

* [x] State Pattern is used by services.
* [x] README documents the pattern.
* [x] Invalid lifecycle actions are rejected.

---

## 11. Team Management

Implement:

* [x] `TeamService`
* [x] `TeamController`
* [x] Create team.
* [x] Get current user team.
* [x] Validate one-team-only rule.

Endpoints:

* [x] `POST /api/teams`
* [x] `GET /api/teams/me`

Acceptance criteria:

* [x] User can create a team.
* [x] Creator becomes first member.
* [x] User cannot create a second team.
* [x] User cannot belong to multiple teams.

---

## 12. Team Invitations

Implement:

* [x] `InvitationService`
* [x] `InvitationController`
* [x] Invite user.
* [x] Accept invitation.
* [x] Decline invitation.

Endpoints:

* [x] `POST /api/teams/{teamId}/invitations`
* [x] `POST /api/invitations/{invitationId}/accept`
* [x] `POST /api/invitations/{invitationId}/decline`

Acceptance criteria:

* [x] Only team members can invite users.
* [x] Invited user must not already belong to a team.
* [x] Only invited user can accept.
* [x] Accepted invitation adds user to team.
* [x] Already answered invitations cannot be changed.

---

## 13. Hackathon Registration

Implement team registration to hackathon.

Endpoint:

* [x] `POST /api/hackathons/{hackathonId}/registrations`
* [x] `GET /api/hackathons/{hackathonId}/registrations`

Acceptance criteria:

* [x] Team can register only during `REGISTRATION_OPEN`.
* [x] Registration after deadline is rejected.
* [x] Team exceeding max size is rejected.
* [x] Duplicate registration is rejected.
* [x] Only assigned staff can list registrations.

---

## 14. Submissions

Implement:

* [x] Create or update own team submission.
* [x] Get own team submission.
* [x] Staff list submissions.

Endpoints:

* [x] `PUT /api/hackathons/{hackathonId}/submissions/my-team`
* [x] `GET /api/hackathons/{hackathonId}/submissions/my-team`
* [x] `GET /api/hackathons/{hackathonId}/submissions`

Acceptance criteria:

* [x] Registered team can submit during `IN_PROGRESS`.
* [x] Submission can be updated before deadline.
* [x] Submission cannot be updated after deadline.
* [x] Assigned staff can view submissions.
* [x] Unassigned staff cannot view submissions.

---

## 15. Staff Access Checks

Implement:

* [x] `StaffAccessService`

Methods:

```java
boolean isOrganizerOf(User user, Hackathon hackathon);
boolean isJudgeOf(User user, Hackathon hackathon);
boolean isMentorOf(User user, Hackathon hackathon);
boolean canAccessSubmissions(User user, Hackathon hackathon);
```

Acceptance criteria:

* [x] Role alone is not enough.
* [x] Assignment to the specific hackathon is checked.
* [x] Unassigned staff cannot access protected hackathon data.

---

## 16. Judge Evaluations

Implement:

* [x] `EvaluationService`
* [x] `EvaluationController`

Endpoints:

* [x] `POST /api/submissions/{submissionId}/evaluation`
* [x] `GET /api/hackathons/{hackathonId}/evaluations`

Acceptance criteria:

* [x] Only assigned judge can evaluate.
* [x] Evaluation only allowed in `EVALUATION`.
* [x] Score must be between 0 and 10.
* [x] Each submission has one evaluation.
* [x] Existing evaluation can be updated.

---

## 17. Mentor Support Requests

Implement:

* [x] `MentorService`
* [x] `MentorController`
* [x] Create support request.
* [x] List support requests.

Endpoints:

* [x] `POST /api/hackathons/{hackathonId}/support-requests`
* [x] `GET /api/hackathons/{hackathonId}/support-requests`

Acceptance criteria:

* [x] Registered team member can create support request.
* [x] Hackathon must be `IN_PROGRESS`.
* [x] Only assigned mentors can view support requests.

---

## 18. Calendar Strategy Adapter

Implement:

* [ ] `CalendarClient`
* [ ] `FakeCalendarClient`
* [ ] `CalendarBookingRequest`
* [ ] `CalendarBookingResponse`
* [ ] Mentor call proposal flow.

Endpoint:

* [ ] `POST /api/support-requests/{supportRequestId}/call-proposal`

Acceptance criteria:

* [ ] Assigned mentor can propose a call.
* [ ] Fake calendar client returns external id.
* [ ] Fake calendar client returns booking URL.
* [ ] Support request status becomes `CALL_PROPOSED`.
* [ ] Strategy Pattern is documented.

---

## 19. Rule Violation Reports

Implement:

* [ ] Create rule violation report.
* [ ] Organizer list reports.

Endpoints:

* [ ] `POST /api/hackathons/{hackathonId}/rule-violations`
* [ ] `GET /api/hackathons/{hackathonId}/rule-violations`

Acceptance criteria:

* [ ] Only assigned mentors can report violations.
* [ ] Reported team must be registered to hackathon.
* [ ] Only organizer can view reports.

---

## 20. Winner Declaration

Implement:

* [ ] `OrganizerService`
* [ ] Declare winner.
* [ ] Validate all submissions evaluated.
* [ ] Set winner team.
* [ ] Set hackathon status to `FINISHED`.

Endpoint:

* [ ] `POST /api/hackathons/{hackathonId}/winner`

Acceptance criteria:

* [ ] Only organizer can declare winner.
* [ ] Hackathon must be in `EVALUATION`.
* [ ] Team must be registered.
* [ ] Team must have a submission.
* [ ] All submissions must be evaluated.
* [ ] Winner cannot be declared twice.
* [ ] Hackathon becomes `FINISHED`.

---

## 21. Payment Strategy Adapter

Implement:

* [ ] `PaymentClient`
* [ ] `FakePaymentClient`
* [ ] `PaymentRequest`
* [ ] `PaymentResponse`
* [ ] `PaymentPrizeService`

Acceptance criteria:

* [ ] Declaring winner triggers fake payment.
* [ ] Payment transaction is created.
* [ ] Payment transaction stores amount.
* [ ] Payment transaction stores external payment id.
* [ ] Strategy Pattern is documented.

---

## 22. Seed Data

Create dev seed data.

Users:

* [ ] `organizer@example.com` with role `ORGANIZER`
* [ ] `judge@example.com` with role `JUDGE`
* [ ] `mentor1@example.com` with role `MENTOR`
* [ ] `mentor2@example.com` with role `MENTOR`
* [ ] `user1@example.com` with role `USER`
* [ ] `user2@example.com` with role `USER`
* [ ] `user3@example.com` with role `USER`

Optional:

* [ ] Demo hackathon in `REGISTRATION_OPEN`.

Acceptance criteria:

* [ ] Seed data loads only in dev profile.
* [ ] Passwords are hashed.
* [ ] Demo users can login.

---

## 23. Unit Tests

Test:

* [ ] `TeamService`
* [ ] `HackathonService`
* [ ] `SubmissionService`
* [ ] `EvaluationService`
* [ ] `MentorService`
* [ ] `OrganizerService`
* [ ] `PaymentPrizeService`

Acceptance criteria:

* [ ] Important business rules are tested.
* [ ] Invalid actions throw correct exceptions.
* [ ] External clients are mocked.

---

## 24. Integration Tests

Use MockMvc.

Test:

* [ ] `POST /api/auth/register`
* [ ] `POST /api/auth/login`
* [ ] `GET /api/hackathons`
* [ ] `POST /api/hackathons`
* [ ] `POST /api/teams`
* [ ] `POST /api/teams/{teamId}/invitations`
* [ ] `POST /api/invitations/{id}/accept`
* [ ] `POST /api/hackathons/{id}/registrations`
* [ ] `PUT /api/hackathons/{id}/submissions/my-team`
* [ ] `POST /api/submissions/{id}/evaluation`
* [ ] `POST /api/hackathons/{id}/winner`

Acceptance criteria:

* [ ] Main flow works through HTTP.
* [ ] Security works.
* [ ] Unauthorized requests are rejected.
* [ ] Forbidden requests are rejected.

---

## 25. README

Document:

* [ ] Project description.
* [ ] How to run.
* [ ] How to test.
* [ ] API overview.
* [ ] Seeded users.
* [ ] Example request flow.
* [ ] Design patterns used.
* [ ] State Pattern explanation.
* [ ] Strategy Pattern explanation.

Acceptance criteria:

* [ ] README is clear enough for project evaluation.
* [ ] README explains that the app is backend-only.
* [ ] README explains how to test with HTTP requests.

---

# Prompt Template for Codex

Use this prompt for each step:

```text
Read the files in the docs directory.

Implement only this checklist section:

SECTION NUMBER AND TITLE HERE

Rules:
- Do not implement later sections.
- Do not add frontend code.
- Keep the app backend-only.
- Keep controllers thin.
- Put business rules in services.
- Use DTOs for requests and responses.
- Keep the project compilable.
- Update docs/DEVELOPMENT_CHECKLIST.md after finishing.

After editing, explain:
1. What files you created or modified.
2. What business rules were implemented.
3. How to run or test this step.
4. What remains for the next checklist section.
```

---

# Example First Prompt

```text
Read docs/GOAL.md and docs/DEVELOPMENT_CHECKLIST.md.

Implement only section 1: Spring Boot Project Setup.

Rules:
- Do not implement entities yet.
- Do not implement controllers yet.
- Do not implement authentication yet.
- Only create the base Spring Boot project, dependencies, configuration, and empty package structure if needed.
- Keep the project compilable.
- Update docs/DEVELOPMENT_CHECKLIST.md after finishing.

After editing, explain:
1. What files you created or modified.
2. How to run the app.
3. How to verify that the setup works.
4. What remains for section 2.
```

---

# Example Second Prompt

```text
Read docs/GOAL.md and docs/DEVELOPMENT_CHECKLIST.md.

Implement only section 2: Package Structure.

Rules:
- Do not implement business logic yet.
- Do not implement entities yet unless the checklist section explicitly says so.
- Create only the package structure required by the architecture.
- Keep the project compilable.
- Update docs/DEVELOPMENT_CHECKLIST.md after finishing.

After editing, explain:
1. What packages were created.
2. Whether the project still compiles.
3. What remains for section 3.
```

---

# Example Third Prompt

```text
Read docs/GOAL.md and docs/DEVELOPMENT_CHECKLIST.md.

Implement only section 3: Domain Enums.

Rules:
- Do not implement entities yet.
- Do not implement repositories yet.
- Add only the required enums.
- Keep the project compilable.
- Update docs/DEVELOPMENT_CHECKLIST.md after finishing.

After editing, explain:
1. What enum files were created.
2. Where they are located.
3. How to compile the project.
4. What remains for section 4.
```

---

# Why This Workflow Is Better

A single huge prompt is risky because:

* the codebase will become too large for context;
* the AI may forget earlier requirements;
* the AI may implement too much at once;
* debugging becomes harder;
* business rules may be skipped;
* generated code may not compile;
* changes are harder to review.

A file-based checklist workflow is better because:

* the project keeps its own memory;
* each step is smaller;
* each step can be compiled;
* each step can be committed;
* the AI can read the current files;
* progress is easier to track;
* context window issues are reduced.

---

# Recommended Git Workflow

After each successful section:

```bash
git status
git add .
git commit -m "[ADD] / [IMP] (Odoo style) <section name>"
```

Example:

```bash
git add .
git commit -m "[ADD] domain enums"
```

This makes it easier to go back if the AI breaks something later.
