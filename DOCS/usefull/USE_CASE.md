# Case Diagram


The backend roles are:

- `USER`: participant/team member.
- `ORGANIZER`: creates and manages hackathons.
- `JUDGE`: evaluates submissions.
- `MENTOR`: supports teams and reports violations.

`ADMIN` exists in the role enum, but the current API does not expose admin-specific use cases.

## Use Case Diagram as Tables

The use case diagram is represented with regular Markdown tables so it does not require PlantUML, Mermaid, or any preview extension.

## Actor Relationships

| Actor | Relationship | Related actor/system | Meaning |
| --- | --- | --- | --- |
| Guest | Uses public API | HackHub API | Can register, login, list hackathons, and view hackathon details without a JWT. |
| Authenticated User | General actor | Participant, Organizer, Judge, Mentor | Any logged-in user with a valid JWT can access common authenticated use cases such as getting their profile. |
| Participant (`USER`) | Specializes | Authenticated User | A logged-in normal user who can create teams, join teams, register teams, submit projects, and request support. |
| Organizer | Specializes | Authenticated User | A logged-in organizer who owns hackathons and manages lifecycle, staff assignment, registrations, reports, and winners. |
| Judge | Specializes | Authenticated User | A logged-in judge who can review submissions and evaluate them when assigned to the hackathon. |
| Mentor | Specializes | Authenticated User | A logged-in mentor who can view assigned hackathon support work, propose calls, and report violations. |
| Fake Payment Client | External system | HackHub API | Called when the organizer declares a winner; returns a fake payment ID and success status. |
| Fake Calendar Client | External system | HackHub API | Called when an assigned mentor proposes a call; returns a fake call ID and booking URL. |

## Actor Use Cases

| Actor | Use cases |
| --- | --- |
| Guest | Register account, Login, List hackathons, View hackathon details |
| Authenticated User | Get current profile, List hackathons, View hackathon details |
| Participant (`USER`) | Create team, View my team, Invite user to team, Accept invitation, Decline invitation, Register team to hackathon, Create/update team submission, View my team submission, Create support request |
| Organizer | Create hackathon, Assign mentor, Assign judge, Update hackathon status, List hackathon registrations, List hackathon submissions, List hackathon evaluations, List rule violation reports, Declare winner |
| Judge | List hackathon registrations, List hackathon submissions, Evaluate submission, List hackathon evaluations |
| Mentor | List hackathon registrations, List hackathon submissions, List hackathon evaluations, List support requests, Propose mentor call, Report rule violation |
| Fake Payment Client | Pay winner prize |
| Fake Calendar Client | Book calendar call |

## Use Case Relationships

| Source use case | Relationship | Target use case | Meaning |
| --- | --- | --- | --- |
| Declare winner | Includes | Pay winner prize | When the organizer declares a winner, the system also creates a fake prize payment. |
| Propose mentor call | Includes | Book calendar call | When an assigned mentor proposes a call, the system also creates a fake calendar booking. |

## Use Case Notes

| Use case | Main actor | Important rules |
| --- | --- | --- |
| Register account | Guest | Creates a `USER` account and returns a JWT. |
| Login | Guest | Returns a JWT for a valid email and password. |
| Get current profile | Authenticated User | Requires `Authorization: Bearer <token>`. |
| List hackathons | Guest, Authenticated User | Public endpoint. |
| View hackathon details | Guest, Authenticated User | Public endpoint. |
| Create hackathon | Organizer | Requires organizer role; new hackathons start in `REGISTRATION_OPEN`. |
| Assign mentor | Organizer | Target user must have role `MENTOR`. |
| Assign judge | Organizer | Target user must have role `JUDGE`. |
| Update hackathon status | Organizer | Valid lifecycle transitions are `REGISTRATION_OPEN -> IN_PROGRESS -> EVALUATION -> FINISHED`. |
| Create team | Participant | User can belong to only one team. |
| View my team | Participant | User must already belong to a team. |
| Invite user to team | Participant | Current user must be a team member; invited user must not already belong to a team. |
| Accept invitation | Participant | Only the invited user can accept a pending invitation. |
| Decline invitation | Participant | Only the invited user can decline a pending invitation. |
| Register team to hackathon | Participant | Current user must be a team member; hackathon must be `REGISTRATION_OPEN`; team size max is 5. |
| List hackathon registrations | Organizer, Judge, Mentor | Actor must be organizer, assigned judge, or assigned mentor for the hackathon. |
| Create/update team submission | Participant | Team must be registered; hackathon must be `IN_PROGRESS`. |
| View my team submission | Participant | Team must be registered and already have a submission. |
| List hackathon submissions | Organizer, Judge, Mentor | Actor must be organizer, assigned judge, or assigned mentor for the hackathon. |
| Evaluate submission | Judge | Actor must be an assigned judge; hackathon must be `EVALUATION`. |
| List hackathon evaluations | Organizer, Judge, Mentor | Actor must be organizer, assigned judge, or assigned mentor for the hackathon. |
| Declare winner | Organizer | Hackathon must be `EVALUATION`; winning team must be registered, have a submission, and all submissions must be evaluated. |
| Pay winner prize | Fake Payment Client | Included by Declare winner; creates a `COMPLETED` payment transaction with `fake-pay-<uuid>`. |
| Create support request | Participant | Team must be registered; hackathon must be `IN_PROGRESS`. |
| List support requests | Mentor | Actor must be an assigned mentor for the hackathon. |
| Propose mentor call | Mentor | Actor must be the assigned mentor of the support request. Current public API creates support requests with no assigned mentor. |
| Book calendar call | Fake Calendar Client | Included by Propose mentor call; creates `fake-call-<uuid>` and a fake booking URL. |
| Report rule violation | Mentor | Actor must be an assigned mentor; reported team must be registered to the hackathon. |
| List rule violation reports | Organizer | Actor must be the organizer of the hackathon. |
