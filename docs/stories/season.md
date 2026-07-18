# Component: Season
This will cover how seasons work in the game. The original system was one school quarter or 8 weeks. I will also explicitly
determine how Season CRUD, lifecycle state transitions, TA assignment, student enrollment, roster visibility are handled.

## User Story 1: Create a season
As a Professor, I want to create a new season, so that a new competition cycle can begin.

Acceptance Criteria:

- Given a Professor, when they create a season with a name and start/end dates, then it is persisted with status DRAFT.
- Given a TA or Student attempting to create a season, then the request is rejected with 403.
- Given a season creation request missing required fields (name, start date), then it is rejected with 400.
- Given a season creation request, then it accepts and persists startingBalance, minInvestment, maxInvestment, maxDebt, 
interestRate, and improvementWindow, defaulting to the values from the original YFL materials ($1B, $1M, $20M, $10M, 10%, 10)
when the Professor doesn't override them.
- Given a season creation request, then joinCode is generated automatically (not supplied by the Professor) and 
allowedDomains is accepted as an optional list, empty means no domain restriction, an open/public season.
- Given financial config values, then minInvestment must be less than maxInvestment, and both must be positive, 
rejected with 400 otherwise, this is the same validation discipline you're already applying to lock times in ForecastQuestion.


## User Story 2: Edit season metadata
As a Professor, I want to edit a season's name, dates, or status, so that I can correct or update details before or 
during the season.

Acceptance Criteria:

- Given a Professor, when they edit a DRAFT or ACTIVE season's metadata, then the changes are persisted.
- Given a Professor attempting to edit a CLOSED season, then the request is rejected with 400, closed seasons are immutable 
except through an explicit reopen action, if you decide to support one.
- Given a TA attempting to edit season metadata, even for their own assigned season, then the request is rejected with 403,
per the scoping decision above.


## User Story 3: Transition a season's lifecycle state
As a Professor, I want to move a season from draft to active to closed, so that the competition follows a clear lifecycle.

Acceptance Criteria:

- Given a season in DRAFT, when a Professor transitions it to ACTIVE, then the status updates and the season becomes visible 
to enrolled students and its assigned TA.
- Given a season in ACTIVE, when a Professor transitions it to CLOSED, then the status updates and no further predictions,
team changes, or enrollments are accepted for that season.
- Given a season in DRAFT, a Professor can transition it directly to CLOSED.
- Given a CLOSED season, when any transition is attempted, then it is rejected with 400, closed is terminal.

## User Story 4: Assign a TA to a season
As a Professor, I want to assign a TA to a specific season, so that they can help manage it.

Acceptance Criteria:

- Given a Professor, when they assign a user with role TA to a season, then that TA becomes scoped to that season for 
management purposes.
- Given a user who is not role TA, when a Professor attempts to assign them as a season's TA, then the request is rejected 
with 400, promotion to TA (Role Management component) is a separate, prior step.
- Given a TA already assigned to a different season, when a Professor attempts to assign them to a second season, then 
reject with a clear error rather than silently reassigning, so a Professor doesn't accidentally orphan a season's 
TA coverage.
- Given a Student or TA attempting to assign a TA to a season, then the request is rejected with 403, this is Professor-only.

## User Story 5: Enroll a student in a season
As a Student, I want to join an open season, so that I can participate in that season's predictions.

Acceptance Criteria:

- Given a season, then it has a joinCode (short random string) and an allowedDomains list set at creation by the Professor.
- Given a student submitting a valid join code, when their email domain matches one of the season's allowedDomains, 
then they are enrolled.
- Given a valid join code but an email domain that does not match, then the request is rejected with 403, with a message 
that doesn't leak which domains are valid, just that this account isn't eligible.
- Given a season with an additional exact-email allowlist configured, when present, then a matching email must also appear 
on that list, domain match alone is insufficient for that season.
- Given an invalid or expired join code, then the request is rejected with 404 or 400 rather than 403, so "wrong code" and 
"wrong organization" are distinguishable failure states for support purposes, even though the student-facing message can 
stay generic.
- Given the one-active-season-at-a-time rule already established, this still applies on top of the above.

## User Story 6: List and view seasons
As a user, I want to see the seasons relevant to me, so that I know what's active and what I'm part of.

Acceptance Criteria:

- Given a Professor, when they list seasons, then all seasons across the system are returned, regardless of assignment.
- Given a TA, when they list seasons, then only their single assigned season is returned.
- Given a Student, when they list seasons, then seasons open for enrollment plus the season they're currently enrolled 
in (if any) are returned, not every season in the system.
- Given any authenticated user, when they fetch a single season by ID, then the response is scoped by the same visibility 
rules as the list endpoint, a TA can't fetch a season they're not assigned to just by guessing its ID.

## User Story 7: View season roster
As a TA or Professor, I want to see who is enrolled in a season, so that I can track participation without a spreadsheet.

Acceptance Criteria:

- Given a Professor, when they view any season's roster, then all enrolled students are returned.
- Given a TA, when they view their assigned season's roster, then all enrolled students in that season are returned.
- Given a TA attempting to view another season's roster, then the request is rejected with 403.
- Given a Student attempting to view a season's roster, then the request is rejected with 403, this is a Staff-only view 
for now, revisit if students should see who else is enrolled.

## User Story 8: Prevent activating a season with incomplete configuration
As the system, I want to block a season from becoming ACTIVE if its financial configuration is incomplete, so that Prediction never encounters the null-config edge case it currently has to guard against defensively.

Acceptance Criteria:

- Given a season in DRAFT with any of startingBalance, minInvestment, maxInvestment, maxDebt, or interestRate unset,
when a Professor attempts to transition it to ACTIVE, then the request is rejected with 400 naming which fields are missing.
- Create a default setting!
- Given a season with all financial config fields set (whether by explicit input or by the defaults from Story 1), 
the ACTIVE transition proceeds normally.


## Definition of Done for Season

- All acceptance criteria above have a passing test.
- Full test suite green.
- Integration test confirms a TA cannot manage, edit, or view a season they're not assigned to.
- Integration test confirms a Student cannot be enrolled in two seasons simultaneously.
- Integration test confirms CLOSED is a terminal state with no further transitions accepted.
- ADR entries written for: season lifecycle transition rules (allowed vs blocked paths), and the one-TA-per-season 
enforcement approach (reject vs reassign).

