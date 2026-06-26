# HackHub Class Diagram Text Description

This document describes the production class diagram for `src/main/java` only.
It excludes tests and test support code. The goal is to make the class diagram
clear before drawing it in a UML tool.

## Class Inventory

### Root And Configuration

- `HackHubApplication`: Spring Boot entry point.
- `OpenApiConfig`: OpenAPI and bearer-auth documentation configuration.

### API Layer

- `AuthController`: exposes registration, login, and current-user endpoints.
- `HackathonController`: exposes hackathon browsing, creation, staff assignment,
  status update, team registration, submission, listing, and winner endpoints.
- `TeamController`: exposes team creation and current-user team lookup.
- `InvitationController`: exposes team invitation creation, accept, and decline.
- `EvaluationController`: exposes submission evaluation and evaluation listing.
- `MentorController`: exposes support request and mentor call proposal endpoints.
- `RuleViolationController`: exposes rule violation report creation and listing.
- `GlobalExceptionHandler`: converts application and framework exceptions into
  standardized API errors.
- `ApiError`: standardized error response record.
- `BadRequestException`, `ConflictException`, `ForbiddenException`,
  `NotFoundException`: custom runtime exceptions.

### API DTO Records

- Auth DTOs: `RegisterRequest`, `LoginRequest`, `AuthResponse`, `UserResponse`.
- Hackathon DTOs: `CreateHackathonRequest`, `UpdateHackathonStatusRequest`,
  `RegisterTeamToHackathonRequest`, `DeclareWinnerRequest`,
  `HackathonResponse`, `HackathonRegistrationResponse`.
- Team and invitation DTOs: `CreateTeamRequest`, `TeamResponse`,
  `CreateInvitationRequest`, `InvitationResponse`.
- Submission and evaluation DTOs: `UpsertSubmissionRequest`,
  `SubmissionResponse`, `CreateEvaluationRequest`, `EvaluationResponse`.
- Mentorship DTOs: `CreateSupportRequestRequest`, `ProposeCallRequest`,
  `SupportRequestResponse`, `CallProposalResponse`.
- Rule violation DTOs: `ReportViolationRequest`, `RuleViolationReportResponse`.
- Payment DTOs: `PaymentTransactionResponse`.

### Application Layer

- `AuthService`: handles registration, login, token generation, and current-user
  lookup.
- `HackathonService`: manages hackathon creation, staff assignment, lifecycle
  transitions, and lifecycle rule checks.
- `TeamService`: creates teams and enforces one-team-per-user.
- `InvitationService`: creates, accepts, and declines team invitations.
- `HackathonRegistrationService`: registers teams to hackathons and enforces
  membership, size, deadline, status, and duplicate-registration rules.
- `SubmissionService`: creates or updates the current team's submission and lets
  staff list submissions.
- `EvaluationService`: lets assigned judges evaluate submissions and lets staff
  list evaluations.
- `MentorService`: creates support requests, lists mentor support requests, and
  proposes mentor calls through a calendar client.
- `RuleViolationService`: lets assigned mentors report registered teams and lets
  organizers list reports.
- `OrganizerService`: declares the winning team and triggers prize payment.
- `PaymentPrizeService`: creates payment transactions through a payment client.
- `StaffAccessService`: centralizes organizer, judge, mentor, and staff checks.

### Mapper Layer

- `UserMapper`: converts `User` to `UserResponse`.
- `TeamMapper`: converts `Team` to `TeamResponse`.
- `HackathonMapper`: converts `Hackathon` to `HackathonResponse`.
- `SubmissionMapper`: converts `Submission` to `SubmissionResponse`.
- `MapperFieldAccess`: internal reflection helper used by mappers.

### Domain Layer

- `User`: account entity.
- `Team`: participant group entity.
- `Hackathon`: event entity.
- `TeamInvitation`: invitation entity connecting a team, invited user, and
  inviting user.
- `HackathonRegistration`: join entity connecting a hackathon and team.
- `Submission`: team project submission entity.
- `Evaluation`: judge evaluation of a submission.
- `SupportRequest`: mentor-help request created by a registered team member.
- `MentorCallProposal`: proposed mentor call with external booking data.
- `RuleViolationReport`: report about a team's rule violation.
- `PaymentTransaction`: payment transaction for a hackathon winner.
- Enums: `Role`, `HackathonStatus`, `InvitationStatus`,
  `SupportRequestStatus`, `PaymentStatus`.

### Hackathon State Layer

- `HackathonState`: interface for lifecycle behavior.
- `RegistrationOpenState`: lifecycle state allowing team registration.
- `InProgressState`: lifecycle state allowing submissions and support requests.
- `EvaluationState`: lifecycle state allowing evaluations and winner selection.
- `FinishedState`: terminal lifecycle state.
- `HackathonStateFactory`: creates the correct `HackathonState` for a
  `HackathonStatus`.

### Infrastructure Layer

- `UserRepository`, `TeamRepository`, `HackathonRepository`,
  `TeamInvitationRepository`, `HackathonRegistrationRepository`,
  `SubmissionRepository`, `EvaluationRepository`, `SupportRequestRepository`,
  `MentorCallProposalRepository`, `RuleViolationReportRepository`,
  `PaymentTransactionRepository`: Spring Data JPA repository interfaces.
- `DevDataSeeder`: dev-profile seed component that creates demo users and one
  demo hackathon.

### External Integrations

- `PaymentClient`: payment port interface.
- `FakePaymentClient`: fake implementation of `PaymentClient`.
- `PaymentRequest`, `PaymentResponse`: payment client data records.
- `CalendarClient`: calendar port interface.
- `FakeCalendarClient`: fake implementation of `CalendarClient`.
- `CalendarBookingRequest`, `CalendarBookingResponse`: calendar client data
  records.

### Security Layer

- `SecurityConfig`: Spring Security configuration.
- `JwtService`: JWT creation and validation service.
- `JwtAuthenticationFilter`: bearer-token authentication filter.
- `CustomUserDetailsService`: loads users for Spring Security.

## Classes And Fields

Access notation:

- `private`: ordinary private field.
- `private final`: constructor-injected dependency or immutable field.
- `public component`: Java record component with generated public accessor.
- `implicit public`: interface method inherited from or declared in an interface.

| Class | Type | Important fields, components, or dependencies with access |
| --- | --- | --- |
| `HackHubApplication` | Spring Boot application | no domain fields |
| `OpenApiConfig` | configuration | `public static final BEARER_AUTH` |
| `AuthController` | REST controller | `private final AuthService authService` |
| `HackathonController` | REST controller | `private final HackathonService hackathonService`; `private final HackathonRegistrationService hackathonRegistrationService`; `private final SubmissionService submissionService`; `private final OrganizerService organizerService` |
| `TeamController` | REST controller | `private final TeamService teamService` |
| `InvitationController` | REST controller | `private final InvitationService invitationService` |
| `EvaluationController` | REST controller | `private final EvaluationService evaluationService` |
| `MentorController` | REST controller | `private final MentorService mentorService` |
| `RuleViolationController` | REST controller | `private final RuleViolationService ruleViolationService` |
| `GlobalExceptionHandler` | controller advice | stateless handler methods; no persistent fields |
| `ApiError` | record | public components: `timestamp`, `status`, `error`, `message`, `path`, `details` |
| `BadRequestException` | exception | inherited `RuntimeException` state |
| `ConflictException` | exception | inherited `RuntimeException` state |
| `ForbiddenException` | exception | inherited `RuntimeException` state |
| `NotFoundException` | exception | inherited `RuntimeException` state |
| `RegisterRequest` | record | public components: `email`, `password` |
| `LoginRequest` | record | public components: `email`, `password` |
| `AuthResponse` | record | public components: `token`, `user` |
| `UserResponse` | record | public components: `id`, `email`, `role` |
| `CreateHackathonRequest` | record | public components: `title`, `description`, `registrationDeadline`, `submissionDeadline`, `startAt`, `endAt`, `prizeAmount` |
| `UpdateHackathonStatusRequest` | record | public component: `status` |
| `RegisterTeamToHackathonRequest` | record | public component: `teamId` |
| `DeclareWinnerRequest` | record | public component: `winnerTeamId` |
| `HackathonResponse` | record | public components: `id`, `title`, `description`, dates, `status`, `prizeAmount`, `organizerId`, `winnerTeamId` |
| `HackathonRegistrationResponse` | record | public components: `id`, `hackathonId`, `teamId`, `registeredAt` |
| `CreateTeamRequest` | record | public component: `name` |
| `TeamResponse` | record | public components: `id`, `name`, `createdByUserId`, `memberIds` |
| `CreateInvitationRequest` | record | public component: `invitedUserId` |
| `InvitationResponse` | record | public components: `id`, `teamId`, `invitedUserId`, `invitedByUserId`, `status`, `createdAt`, `respondedAt` |
| `UpsertSubmissionRequest` | record | public components: `projectName`, `repositoryUrl`, `demoUrl`, `description` |
| `SubmissionResponse` | record | public components: `id`, `hackathonId`, `teamId`, `projectName`, `repositoryUrl`, `demoUrl`, `description`, `submittedAt`, `updatedAt` |
| `CreateEvaluationRequest` | record | public components: `score`, `comment` |
| `EvaluationResponse` | record | public components: `id`, `submissionId`, `judgeId`, `score`, `comment`, `evaluatedAt` |
| `CreateSupportRequestRequest` | record | public components: `title`, `message` |
| `ProposeCallRequest` | record | public component: `scheduledAt` |
| `SupportRequestResponse` | record | public components: `id`, `hackathonId`, `teamId`, `createdByUserId`, `assignedMentorId`, `title`, `message`, `status`, `createdAt`, `closedAt` |
| `CallProposalResponse` | record | public components: `id`, `supportRequestId`, `mentorId`, `scheduledAt`, `externalCallId`, `bookingUrl`, `createdAt` |
| `ReportViolationRequest` | record | public components: `reportedTeamId`, `description` |
| `RuleViolationReportResponse` | record | public components: `id`, `hackathonId`, `reportedTeamId`, `reportedByUserId`, `description`, `createdAt` |
| `PaymentTransactionResponse` | record | public components: `id`, `hackathonId`, `winnerTeamId`, `amount`, `externalPaymentId`, `status`, `createdAt`, `completedAt` |
| `AuthService` | service | `private final UserRepository`; `private final PasswordEncoder`; `private final AuthenticationManager`; `private final JwtService`; `private final UserMapper` |
| `HackathonService` | service | `private final HackathonRepository`; `private final UserRepository`; `private final HackathonMapper`; `private final HackathonStateFactory` |
| `TeamService` | service | `private final TeamRepository`; `private final UserRepository`; `private final TeamMapper` |
| `InvitationService` | service | `private final TeamInvitationRepository`; `private final TeamRepository`; `private final UserRepository` |
| `HackathonRegistrationService` | service | `private static final MAX_TEAM_SIZE`; `private final HackathonRegistrationRepository`; `private final HackathonRepository`; `private final TeamRepository`; `private final UserRepository`; `private final HackathonService`; `private final StaffAccessService` |
| `SubmissionService` | service | `private final SubmissionRepository`; `private final HackathonRepository`; `private final HackathonRegistrationRepository`; `private final TeamRepository`; `private final UserRepository`; `private final SubmissionMapper`; `private final HackathonService`; `private final StaffAccessService` |
| `EvaluationService` | service | `private final EvaluationRepository`; `private final SubmissionRepository`; `private final HackathonRepository`; `private final UserRepository`; `private final StaffAccessService`; `private final HackathonService` |
| `MentorService` | service | `private final SupportRequestRepository`; `private final HackathonRepository`; `private final TeamRepository`; `private final HackathonRegistrationRepository`; `private final UserRepository`; `private final StaffAccessService`; `private final MentorCallProposalRepository`; `private final CalendarClient` |
| `RuleViolationService` | service | `private final RuleViolationReportRepository`; `private final HackathonRepository`; `private final TeamRepository`; `private final HackathonRegistrationRepository`; `private final UserRepository`; `private final StaffAccessService` |
| `OrganizerService` | service | `private final HackathonRepository`; `private final TeamRepository`; `private final SubmissionRepository`; `private final EvaluationRepository`; `private final UserRepository`; `private final PaymentPrizeService`; `private final StaffAccessService` |
| `PaymentPrizeService` | service | `private final PaymentClient`; `private final PaymentTransactionRepository` |
| `StaffAccessService` | service | stateless; checks `User` and `Hackathon` relationships |
| `UserMapper` | mapper | stateless mapper; uses `MapperFieldAccess` |
| `TeamMapper` | mapper | stateless mapper; uses `MapperFieldAccess` |
| `HackathonMapper` | mapper | stateless mapper; uses `MapperFieldAccess` |
| `SubmissionMapper` | mapper | stateless mapper; uses `MapperFieldAccess` |
| `MapperFieldAccess` | helper | no fields; private constructor; static field-read helper |
| `User` | entity | `private id`; `private email`; `private passwordHash`; `private role` |
| `Team` | entity | `private id`; `private name`; `private createdBy`; `private members` |
| `Hackathon` | entity | `private id`; `private title`; `private description`; date fields; `private status`; `private prizeAmount`; `private organizer`; `private winnerTeam`; `private judges`; `private mentors` |
| `TeamInvitation` | entity | `private id`; `private team`; `private invitedUser`; `private invitedByUser`; `private status`; `private createdAt`; `private respondedAt` |
| `HackathonRegistration` | entity | `private id`; `private hackathon`; `private team`; `private registeredAt` |
| `Submission` | entity | `private id`; `private hackathon`; `private team`; `private projectName`; `private repositoryUrl`; `private demoUrl`; `private description`; `private submittedAt`; `private updatedAt` |
| `Evaluation` | entity | `private id`; `private submission`; `private judge`; `private score`; `private comment`; `private evaluatedAt` |
| `SupportRequest` | entity | `private id`; `private hackathon`; `private team`; `private createdByUser`; `private assignedMentor`; `private title`; `private message`; `private status`; `private createdAt`; `private closedAt` |
| `MentorCallProposal` | entity | `private id`; `private supportRequest`; `private mentor`; `private scheduledAt`; `private externalCallId`; `private bookingUrl`; `private createdAt` |
| `RuleViolationReport` | entity | `private id`; `private hackathon`; `private reportedTeam`; `private reportedByUser`; `private description`; `private createdAt` |
| `PaymentTransaction` | entity | `private id`; `private hackathon`; `private winnerTeam`; `private amount`; `private externalPaymentId`; `private status`; `private createdAt`; `private completedAt` |
| `Role` | enum | values: `USER`, `ORGANIZER`, `MENTOR`, `JUDGE`, `ADMIN` |
| `HackathonStatus` | enum | values: `REGISTRATION_OPEN`, `IN_PROGRESS`, `EVALUATION`, `FINISHED` |
| `InvitationStatus` | enum | values: `PENDING`, `ACCEPTED`, `DECLINED` |
| `SupportRequestStatus` | enum | values: `OPEN`, `CALL_PROPOSED`, `CLOSED` |
| `PaymentStatus` | enum | values: `PENDING`, `COMPLETED`, `FAILED` |
| `HackathonState` | interface | implicit public lifecycle methods |
| `RegistrationOpenState` | state implementation | no fields |
| `InProgressState` | state implementation | no fields |
| `EvaluationState` | state implementation | no fields |
| `FinishedState` | state implementation | no fields |
| `HackathonStateFactory` | factory component | `private final registrationOpen`; `private final inProgress`; `private final evaluation`; `private final finished` |
| Repository interfaces | infrastructure | no fields; extend `JpaRepository<Entity, Long>` and declare finder methods |
| `PaymentClient` | external interface | implicit public `payPrize` |
| `FakePaymentClient` | external component | no dependency fields; creates fake payment response |
| `PaymentRequest` | record | public components: `hackathonTitle`, `winnerTeamId`, `amount` |
| `PaymentResponse` | record | public components: `externalPaymentId`, `success` |
| `CalendarClient` | external interface | implicit public `bookCall` |
| `FakeCalendarClient` | external component | no dependency fields; creates fake calendar response |
| `CalendarBookingRequest` | record | public components: `topic`, `scheduledAt`, `requesterEmail`, `mentorEmail` |
| `CalendarBookingResponse` | record | public components: `externalCallId`, `bookingUrl` |
| `DevDataSeeder` | dev seed component | `private static final DEMO_PASSWORD`; `private final UserRepository`; `private final HackathonRepository`; `private final PasswordEncoder` |
| `SecurityConfig` | security config | `private final JwtAuthenticationFilter`; `private final Environment` |
| `JwtService` | security service | `private final secret`; `private final expirationMs` |
| `JwtAuthenticationFilter` | servlet filter | `private final JwtService`; `private final CustomUserDetailsService` |
| `CustomUserDetailsService` | security service | `private final UserRepository` |

## Relationship Table

UML vocabulary used here:

- Association: one class has a domain-level reference to another.
- Aggregation: one class groups independent objects that can exist without it.
- Composition: strong lifecycle ownership. This project mostly avoids strict
  composition because JPA entities are independently persisted.
- Dependency: one class uses another through constructor injection, method
  parameters, return values, or local calls.
- Generalization: inheritance or interface implementation.
- Realization: a class implements an interface.

| Source | Target | UML relationship | Multiplicity | In-code evidence | Diagram note |
| --- | --- | --- | --- | --- | --- |
| `Team` | `User` as `createdBy` | Association | `Team 1 -> 1 User`; `User 1 -> 0..* Team` | `@ManyToOne` | A team has exactly one creator. The creator user exists independently. |
| `Team` | `User` as `members` | Aggregation | `Team 1 o-- 0..* User`; `User 0..* -- 0..* Team` | `@ManyToMany` with `team_members` | Team groups users, but does not own their lifecycle. |
| `Hackathon` | `User` as `organizer` | Association | `Hackathon 1 -> 1 User`; `User 1 -> 0..* Hackathon` | `@ManyToOne` | The organizer is a user with organizer role. |
| `Hackathon` | `User` as `judges` | Aggregation | `Hackathon 1 o-- 0..* User`; `User 0..* -- 0..* Hackathon` | `@ManyToMany` with `hackathon_judges` | Staff assignment. Users are independent from the hackathon. |
| `Hackathon` | `User` as `mentors` | Aggregation | `Hackathon 1 o-- 0..* User`; `User 0..* -- 0..* Hackathon` | `@ManyToMany` with `hackathon_mentors` | Staff assignment. Users are independent from the hackathon. |
| `Hackathon` | `Team` as `winnerTeam` | Association | `Hackathon 1 -> 0..1 Team`; `Team 1 -> 0..* Hackathon` | nullable `@ManyToOne` | Winner is optional until declared. |
| `Hackathon` | `HackathonStatus` | Association to enum | `Hackathon 1 -> 1 HackathonStatus` | `@Enumerated` | Status drives lifecycle behavior. |
| `TeamInvitation` | `Team` | Association | `Team 1 -> 0..* TeamInvitation`; `TeamInvitation 1 -> 1 Team` | `@ManyToOne` | Invitation belongs to a target team. |
| `TeamInvitation` | `User` as `invitedUser` | Association | `User 1 -> 0..* TeamInvitation`; `TeamInvitation 1 -> 1 User` | `@ManyToOne` | User receiving the invitation. |
| `TeamInvitation` | `User` as `invitedByUser` | Association | `User 1 -> 0..* TeamInvitation`; `TeamInvitation 1 -> 1 User` | `@ManyToOne` | User who sent the invitation. |
| `TeamInvitation` | `InvitationStatus` | Association to enum | `TeamInvitation 1 -> 1 InvitationStatus` | `@Enumerated` | Values are pending, accepted, declined. |
| `HackathonRegistration` | `Hackathon` | Association, join entity | `Hackathon 1 -> 0..* HackathonRegistration` | `@ManyToOne` | Registration records participation of teams in hackathons. |
| `HackathonRegistration` | `Team` | Association, join entity | `Team 1 -> 0..* HackathonRegistration` | `@ManyToOne` | A team can register for multiple hackathons, but once per hackathon. |
| `Submission` | `Hackathon` | Association | `Hackathon 1 -> 0..* Submission`; `Submission 1 -> 1 Hackathon` | `@ManyToOne` | Submission is scoped to a hackathon. |
| `Submission` | `Team` | Association | `Team 1 -> 0..* Submission`; `Submission 1 -> 1 Team` | `@ManyToOne` | Submission is owned functionally by a team for one hackathon. |
| `Evaluation` | `Submission` | Association | `Submission 1 -> 0..1 Evaluation`; `Evaluation 1 -> 1 Submission` | `@OneToOne`, unique `submission_id` | Each submission can have at most one evaluation. |
| `Evaluation` | `User` as `judge` | Association | `User 1 -> 0..* Evaluation`; `Evaluation 1 -> 1 User` | `@ManyToOne` | Evaluation is written by an assigned judge user. |
| `SupportRequest` | `Hackathon` | Association | `Hackathon 1 -> 0..* SupportRequest`; `SupportRequest 1 -> 1 Hackathon` | `@ManyToOne` | Support is requested during a specific hackathon. |
| `SupportRequest` | `Team` | Association | `Team 1 -> 0..* SupportRequest`; `SupportRequest 1 -> 1 Team` | `@ManyToOne` | Support belongs to the requesting team. |
| `SupportRequest` | `User` as `createdByUser` | Association | `User 1 -> 0..* SupportRequest`; `SupportRequest 1 -> 1 User` | `@ManyToOne` | The participant who opened the request. |
| `SupportRequest` | `User` as `assignedMentor` | Association | `User 1 -> 0..* SupportRequest`; `SupportRequest 1 -> 0..1 User` | nullable `@ManyToOne` | Optional mentor assignment. |
| `SupportRequest` | `SupportRequestStatus` | Association to enum | `SupportRequest 1 -> 1 SupportRequestStatus` | `@Enumerated` | Values are open, call proposed, closed. |
| `MentorCallProposal` | `SupportRequest` | Association | `SupportRequest 1 -> 0..* MentorCallProposal`; `MentorCallProposal 1 -> 1 SupportRequest` | `@ManyToOne` | Call proposal references the support request it addresses. |
| `MentorCallProposal` | `User` as `mentor` | Association | `User 1 -> 0..* MentorCallProposal`; `MentorCallProposal 1 -> 1 User` | `@ManyToOne` | Mentor who proposed the call. |
| `RuleViolationReport` | `Hackathon` | Association | `Hackathon 1 -> 0..* RuleViolationReport`; `RuleViolationReport 1 -> 1 Hackathon` | `@ManyToOne` | Report is scoped to a hackathon. |
| `RuleViolationReport` | `Team` as `reportedTeam` | Association | `Team 1 -> 0..* RuleViolationReport`; `RuleViolationReport 1 -> 1 Team` | `@ManyToOne` | Team accused of breaking rules. |
| `RuleViolationReport` | `User` as `reportedByUser` | Association | `User 1 -> 0..* RuleViolationReport`; `RuleViolationReport 1 -> 1 User` | `@ManyToOne` | Reporter must be an assigned mentor. |
| `PaymentTransaction` | `Hackathon` | Association | `Hackathon 1 -> 0..* PaymentTransaction`; `PaymentTransaction 1 -> 1 Hackathon` | `@ManyToOne` | Payment is created for a finished hackathon. |
| `PaymentTransaction` | `Team` as `winnerTeam` | Association | `Team 1 -> 0..* PaymentTransaction`; `PaymentTransaction 1 -> 1 Team` | `@ManyToOne` | Winner team receives the prize. |
| `PaymentTransaction` | `PaymentStatus` | Association to enum | `PaymentTransaction 1 -> 1 PaymentStatus` | `@Enumerated` | Values are pending, completed, failed. |
| `RegistrationOpenState` | `HackathonState` | Realization | class -> interface | `implements HackathonState` | State pattern implementation for registration phase. |
| `InProgressState` | `HackathonState` | Realization | class -> interface | `implements HackathonState` | State pattern implementation for active hacking phase. |
| `EvaluationState` | `HackathonState` | Realization | class -> interface | `implements HackathonState` | State pattern implementation for judging phase. |
| `FinishedState` | `HackathonState` | Realization | class -> interface | `implements HackathonState` | Terminal state implementation. |
| `HackathonStateFactory` | `HackathonState` implementations | Aggregation and factory dependency | `1 -> 4` cached state objects | private final state fields | Factory caches one instance of each state implementation. |
| `HackathonStateFactory` | `HackathonStatus` | Dependency | many statuses -> one selected state | `fromStatus(HackathonStatus)` | Maps enum status to state object. |
| `HackathonService` | `HackathonStateFactory` | Dependency | `1 -> 1` injected dependency | constructor-injected field | Used to validate transitions and allowed actions. |
| `BadRequestException` | `RuntimeException` | Generalization | subclass -> superclass | `extends RuntimeException` | Custom 400-class domain error. |
| `ConflictException` | `RuntimeException` | Generalization | subclass -> superclass | `extends RuntimeException` | Custom 409-class domain error. |
| `ForbiddenException` | `RuntimeException` | Generalization | subclass -> superclass | `extends RuntimeException` | Custom 403-class domain error. |
| `NotFoundException` | `RuntimeException` | Generalization | subclass -> superclass | `extends RuntimeException` | Custom 404-class domain error. |
| `GlobalExceptionHandler` | custom exceptions | Dependency | handler -> exception types | `@ExceptionHandler` methods | Converts exceptions into `ApiError`. |
| `GlobalExceptionHandler` | `ApiError` | Dependency | handler -> response record | creates `ApiError` in `buildResponse` | Standard API error output. |
| `FakePaymentClient` | `PaymentClient` | Realization | implementation -> interface | `implements PaymentClient` | Fake external payment adapter. |
| `FakeCalendarClient` | `CalendarClient` | Realization | implementation -> interface | `implements CalendarClient` | Fake external calendar adapter. |
| `PaymentClient` | `PaymentRequest`, `PaymentResponse` | Dependency | interface method input/output | `payPrize(PaymentRequest)` | External payment port. |
| `CalendarClient` | `CalendarBookingRequest`, `CalendarBookingResponse` | Dependency | interface method input/output | `bookCall(CalendarBookingRequest)` | External calendar port. |
| `JwtAuthenticationFilter` | `OncePerRequestFilter` | Generalization | subclass -> superclass | `extends OncePerRequestFilter` | Spring Security filter specialization. |
| `CustomUserDetailsService` | `UserDetailsService` | Realization | implementation -> interface | `implements UserDetailsService` | Loads application users for Spring Security. |
| `DevDataSeeder` | `CommandLineRunner` | Realization | implementation -> interface | `implements CommandLineRunner` | Runs seed logic at dev startup. |
| every repository | `JpaRepository<Entity, Long>` | Generalization | repository -> Spring Data base interface | `extends JpaRepository` | Repository pattern through Spring Data JPA. |

## Service Dependency Table

| Service | Depends on | Relationship type | Why it depends on it |
| --- | --- | --- | --- |
| `AuthService` | `UserRepository` | Dependency | Saves and loads users by email. |
| `AuthService` | `PasswordEncoder` | Dependency | Hashes registration passwords. |
| `AuthService` | `AuthenticationManager` | Dependency | Authenticates login credentials. |
| `AuthService` | `JwtService` | Dependency | Generates JWT tokens. |
| `AuthService` | `UserMapper` | Dependency | Converts `User` to `UserResponse`. |
| `HackathonService` | `HackathonRepository`, `UserRepository` | Dependency | Persists hackathons and loads users for organizer/staff checks. |
| `HackathonService` | `HackathonMapper` | Dependency | Converts hackathon entities to responses. |
| `HackathonService` | `HackathonStateFactory` | Dependency | Applies lifecycle state rules. |
| `TeamService` | `TeamRepository`, `UserRepository` | Dependency | Creates teams and checks existing membership. |
| `TeamService` | `TeamMapper` | Dependency | Converts team entities to responses. |
| `InvitationService` | `TeamInvitationRepository`, `TeamRepository`, `UserRepository` | Dependency | Creates invitations, validates team membership, and loads users. |
| `HackathonRegistrationService` | registration, hackathon, team, and user repositories | Dependency | Loads and persists registration workflow entities. |
| `HackathonRegistrationService` | `HackathonService`, `StaffAccessService` | Dependency | Reuses lifecycle and staff-access checks. |
| `SubmissionService` | submission, hackathon, registration, team, and user repositories | Dependency | Loads and persists submission workflow entities. |
| `SubmissionService` | `SubmissionMapper`, `HackathonService`, `StaffAccessService` | Dependency | Maps responses and enforces lifecycle/staff checks. |
| `EvaluationService` | evaluation, submission, hackathon, and user repositories | Dependency | Loads submissions and persists evaluations. |
| `EvaluationService` | `StaffAccessService`, `HackathonService` | Dependency | Validates judge assignment and evaluation phase. |
| `MentorService` | support request, hackathon, team, registration, user, and call proposal repositories | Dependency | Loads and persists mentorship workflow entities. |
| `MentorService` | `StaffAccessService`, `CalendarClient` | Dependency | Validates mentor access and books fake calls. |
| `RuleViolationService` | report, hackathon, team, registration, and user repositories | Dependency | Loads and persists report workflow entities. |
| `RuleViolationService` | `StaffAccessService` | Dependency | Ensures reporter is an assigned mentor and organizer can list. |
| `OrganizerService` | hackathon, team, submission, evaluation, and user repositories | Dependency | Declares winner after validating hackathon, team, and evaluations. |
| `OrganizerService` | `PaymentPrizeService`, `StaffAccessService` | Dependency | Triggers payment and checks organizer authority. |
| `PaymentPrizeService` | `PaymentClient`, `PaymentTransactionRepository` | Dependency | Calls fake payment client and persists transaction result. |
| `StaffAccessService` | `User`, `Hackathon` | Dependency | Reads domain relationships to answer access questions. |

## Controller Dependency Table

| Controller | Depends on | Request/response boundary |
| --- | --- | --- |
| `AuthController` | `AuthService` | Uses `RegisterRequest`, `LoginRequest`, `AuthResponse`, `UserResponse`. |
| `HackathonController` | `HackathonService`, `HackathonRegistrationService`, `SubmissionService`, `OrganizerService` | Uses hackathon, registration, submission, and winner DTOs. |
| `TeamController` | `TeamService` | Uses `CreateTeamRequest`, `TeamResponse`. |
| `InvitationController` | `InvitationService` | Uses `CreateInvitationRequest`, `InvitationResponse`. |
| `EvaluationController` | `EvaluationService` | Uses `CreateEvaluationRequest`, `EvaluationResponse`. |
| `MentorController` | `MentorService` | Uses `CreateSupportRequestRequest`, `ProposeCallRequest`, `SupportRequestResponse`, `CallProposalResponse`. |
| `RuleViolationController` | `RuleViolationService` | Uses `ReportViolationRequest`, `RuleViolationReportResponse`. |

## Repository Ownership Table

| Repository | Entity | Extra relationship meaning |
| --- | --- | --- |
| `UserRepository` | `User` | Lookup by email and email existence checks. |
| `TeamRepository` | `Team` | Finds teams by member and checks member ownership. |
| `HackathonRepository` | `Hackathon` | Basic hackathon persistence. |
| `TeamInvitationRepository` | `TeamInvitation` | Finds invitation by team and invited user. |
| `HackathonRegistrationRepository` | `HackathonRegistration` | Checks and lists team registrations for hackathons. |
| `SubmissionRepository` | `Submission` | Finds one submission by hackathon and team, lists by hackathon. |
| `EvaluationRepository` | `Evaluation` | Finds evaluation by submission and by submission hackathon. |
| `SupportRequestRepository` | `SupportRequest` | Lists support requests by hackathon. |
| `MentorCallProposalRepository` | `MentorCallProposal` | Basic call proposal persistence. |
| `RuleViolationReportRepository` | `RuleViolationReport` | Lists violation reports by hackathon. |
| `PaymentTransactionRepository` | `PaymentTransaction` | Basic payment transaction persistence. |

## Pattern Notes

| Pattern or architectural style | Where it appears | Classes involved | Notes |
| --- | --- | --- | --- |
| State pattern | Hackathon lifecycle | `HackathonState`, `RegistrationOpenState`, `InProgressState`, `EvaluationState`, `FinishedState`, `HackathonStateFactory`, `HackathonService` | Explicitly used. Each state controls allowed operations and transitions. |
| Factory pattern | State object selection | `HackathonStateFactory` | Creates or returns the state implementation for a `HackathonStatus`. |
| Repository pattern | Persistence access | all `*Repository` interfaces | Implemented through Spring Data JPA. Services depend on repositories instead of direct database logic. |
| Dependency injection | Application wiring | controllers, services, security, external clients | Constructor-injected `private final` fields define most dependencies. |
| DTO pattern | API boundary | request and response records | Controllers and services use DTOs to avoid exposing entities directly. |
| Mapper pattern | Entity-to-response conversion | `UserMapper`, `TeamMapper`, `HackathonMapper`, `SubmissionMapper` | Converts domain objects into response records. |
| Ports and adapters / strategy-like interface | External services | `PaymentClient`, `FakePaymentClient`, `CalendarClient`, `FakeCalendarClient` | Services depend on interfaces, so fake clients can be replaced by real providers. |
| Security filter chain | Authentication | `SecurityConfig`, `JwtAuthenticationFilter`, `JwtService`, `CustomUserDetailsService` | JWT filter authenticates bearer tokens before controller logic. |
| Visitor pattern | Not used | none | No visitor-style double-dispatch structure exists in the production code. |
| Composition | Mostly not used as strict UML composition | domain entities | Entities reference each other but are independently persisted. Association or aggregation is more accurate. |
