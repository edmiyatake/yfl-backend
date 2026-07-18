# Component: User/Role Management
Scope: User record lifecycle, role assignment and promotion, profile management. Builds directly on Auth, every story here assumes a valid authenticated caller.

## User Story 1: Auto-provision a user record on first login
As a new user, I want an account to be created automatically the first time I verify my login code, so that I don't need a 
separate registration step.

Acceptance Criteria:

- Given an email that has never logged in before, when it successfully verifies an OTP code for the first time, then a new 
User record is created with role STUDENT by default.
- Given an email that already has a User record, when it verifies a code, then no duplicate record is created and the 
existing user's role is unchanged.
- Given a newly created user, then their record includes email and creation timestamp at minimum, with name and other profile 
fields left null until the user fills them in.


## User Story 2: Bootstrap the first Staff account
As the system operator, I want to seed one or more Professor accounts before launch, so that there's a Staff member able to 
promote others once the app is live.

Acceptance Criteria:

- Given a deploy-time seed (migration or environment-variable-driven startup step), when it runs, then the specified email(s)
are created or updated with role PROFESSOR.
- Given this seeding mechanism, then it is not exposed as an API endpoint under any circumstance, it only runs at deploy or 
startup time, so there's no in-app path to self-elevate to Staff.
- Given a normal application restart after initial seeding, then the seed step is idempotent, it doesn't fail or duplicate
if the Professor account already exists.


## User Story 3: Promote a user's role
As a Staff member, I want to promote a Student to TA (or another user to Professor), so that trusted users can help manage 
seasons.

Acceptance Criteria:

- Given a Staff member calling the promote endpoint with a target user ID and a new role, when the target is a valid user, 
then the role is updated and persisted.
- Given a Student attempting to call the promote endpoint, then the request is rejected with 403, regardless of what role 
they're trying to assign, including to themselves.
- Given a Staff member attempting to promote themselves, then decide explicitly whether this is allowed (a Professor 
promoting another account to Professor is legitimate; a TA promoting themselves to Professor probably shouldn't be, worth a 
rule here rather than leaving it open).
- Given a role change, then it's recorded in an audit log with who made the change, to whom, and when, since this is a 
sensitive action.


## User Story 4: Demote or revoke a role
As a Staff member, I want to demote a TA back to Student, so that access can be corrected if it was granted in error or is 
no longer needed.

Acceptance Criteria:

- Given a Staff member calling demote on a TA, then the role is updated to STUDENT.
- Given a Staff member attempting to demote a Professor, decide explicitly whether this requires a higher bar (e.g. only 
another Professor can demote a Professor) to prevent a single TA-turned-Professor from locking others out.
- Given a demotion, then any season-scoped staff assignments tied to that role are also handled consistently, don't leave 
a demoted TA still listed as roster manager for a season.


## User Story 5: View users and roles
As a Staff member, I want to see a list of users and their current roles, so that I know who has access to what.

Acceptance Criteria:

- Given a Staff member, when they list users, then the response includes each user's email, role, and account status.
- Given a Student attempting to list all users, then the request is rejected with 403, students should not be able to 
enumerate the full user base.
- Given a large user base (thousands, per your 5,000-user target), then the list endpoint is paginated, not returned as 
one unbounded response.


## User Story 6: View and update own profile
As a user, I want to view and update my own profile information, so that my display name and details are accurate.

Acceptance Criteria:

- Given any authenticated user, when they fetch their own profile, then their data is returned.
- Given any authenticated user, when they attempt to fetch another user's profile by ID, then the request is rejected, 
students cannot view other students' profiles; Staff visibility into student profiles is a separate, explicit permission, 
not implied.
- Given a profile update, then role and email cannot be changed through this endpoint, those are handled by their own 
dedicated, more restricted flows.


User Story 7: Deactivate a user account
As a Staff member, I want to deactivate a user's account, so that someone who leaves the program loses access without 
deleting their historical predictions and scores.

Acceptance Criteria:

- Given a Staff member deactivating a user, then the user's status is set to inactive and any active sessions/refresh 
tokens for that user are revoked immediately.
- Given a deactivated user attempting to log in, when they request or verify an OTP code, then the request is rejected, 
they can authenticate but the account is gated.
- Given a deactivated user's historical data (past predictions, scores), then it is preserved and still shows correctly 
on leaderboards, deactivation is not deletion.


## Definition of Done for User/Role Management

- All acceptance criteria above have a passing test.
- Full test suite green.
- Integration test confirms a Student cannot promote, demote, deactivate, or list all users.
- Integration test confirms the bootstrap seeding mechanism has no reachable API path.
- Role changes and deactivations are audit-logged with actor, target, and timestamp.
- Pagination is implemented on the user list endpoint before load testing in Phase 5.
- ADR entries written for: self-promotion rule, Professor-demotion rule, and the bootstrap mechanism chosen.