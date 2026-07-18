# Component: ForecastQuestion
Scope: Question creation, lifecycle, lock-time enforcement, resolution (marking the outcome, not scoring, that's the Scoring component's job). Season-scoped, following the same TA/Professor permission pattern as Season and Team.

## User Story 1: Create a forecast question
As a TA or Professor, I want to create either a binary or continuous forecast question, so that students can predict yes/no outcomes or exact numeric values.

Acceptance Criteria:

- Given staff creating a question, when they specify questionType: BINARY, then the question requires prompt text, open time, and lock time only, same as before.
- Given staff creating a question, when they specify questionType: CONTINUOUS, then the question additionally requires a unit field (dollars, percent, count, etc.) describing what kind of number students are predicting, matching the "do not abbreviate, understand the data type" guidance from the original rules.
- Given a CONTINUOUS question, then resolution criteria is still free text describing the data source (e.g. "Box Office Mojo domestic opening weekend total"), no live external data integration, staff enter the actual value manually at resolution time, consistent with your Assumption 3.
- Given a question creation request with an invalid or missing questionType, then it is rejected with 400, type is not optional and has no default.


## User Story 2: Edit a forecast question
As a TA or Professor, I want to edit a question's details, so that I can fix mistakes before students start predicting on it.

Acceptance Criteria:

- Given a question with zero predictions submitted, when staff edit its prompt, lock time, or resolution criteria, then the changes are persisted.
- Given a question with at least one prediction already submitted, when staff attempt to edit prompt or resolution criteria, then the request is rejected with 400, editing the question after someone has committed to an answer would be unfair to that student. This is the decision your checklist flagged as open, edits are blocked once any prediction exists, no versioning system for v1.
- Given a question with existing predictions, when staff attempt to push the lock time later (extending the window), decide whether this is allowed since it doesn't disadvantage existing submitters. I'd allow lock-time extension even after predictions exist, but not shortening it or changing the prompt, since extending only adds opportunity, it doesn't invalidate anyone's existing answer.
- Given a TA editing a question outside their assigned season, then the request is rejected with 403.


## User Story 3: View questions for a season
As a user, I want to see the forecast questions available in my season, so that I know what I can predict on.

Acceptance Criteria:

- Given a Student enrolled in a season, when they list questions, then questions with a derived status of OPEN or LOCKED are returned; CANCELLED questions are excluded from the default view.
- Given a question whose lock time has passed but hasn't been explicitly resolved yet, then its derived status is LOCKED, not OPEN, computed from the current time versus lockTime, not from a stored flag.
- Given a TA or Professor, when they list questions for their season, then RESOLVED and CANCELLED questions are also included, staff need the full history, students mainly need what's actionable.
- Given a user not enrolled in and not staff-assigned to the season, then the request is rejected with 403.
- Given a list of questions returned to a student, then each question includes its questionType so the frontend can render the correct input, yes/no toggle for BINARY, numeric field for CONTINUOUS.


## User Story 4: Submit predictions are rejected after lock time
As the system, I want to reject any prediction submitted after a question's lock time, so that no one can predict with knowledge of results or extra time others didn't get.
(This story's enforcement lives in the Prediction component, but the rule itself is defined by ForecastQuestion, so the acceptance criteria belong here as the contract Prediction must honor.)

Acceptance Criteria:

- Given a question whose lock time has not yet passed, a submission attempt succeeds (verified fully in the Prediction component).
- Given a question whose lock time has passed by even one second, a submission attempt is rejected with 400.
- Given a submission arriving at exactly the lock time instant, decide the boundary explicitly: I'd treat lock time as exclusive, now < lockTime required to submit, so a submission at exactly lockTime is rejected. This needs its own test given your Session Workflow doc already calls out boundary-exact submission as a required edge case.


## User Story 5: Cancel a question
As a TA or Professor, I want to cancel a question that turned out to be unanswerable or was created in error, so that it doesn't get scored against anyone.

Acceptance Criteria:

- Given a question in any state prior to RESOLVED, when staff cancel it, then its status becomes CANCELLED and it's excluded from scoring entirely.
- Given a question with existing predictions, when it's cancelled, then those predictions are preserved for record-keeping but explicitly marked as not contributing to any score.
- Given a RESOLVED question, when staff attempt to cancel it, then the request is rejected with 400, cancellation only applies before an outcome has been recorded.

## User Story 6: Resolve a question (revised)
As a TA or Professor, I want to record the actual outcome, whether that's yes/no or a specific number, so that scoring can be triggered.

Acceptance Criteria:

- Given a BINARY question past lock time, when staff resolve it, then they submit a boolean outcome, unchanged from before.
- Given a CONTINUOUS question past lock time, when staff resolve it, then they submit a numeric actual value matching the question's declared unit.
- Given a CONTINUOUS question, when staff submit a non-numeric or malformed value, then the request is rejected with 400 before the question is marked RESOLVED.
- Given either type, resolution remains a one-time action, same rule as before, no silent overwrite.

## Definition of Done for ForecastQuestion

- All acceptance criteria above have a passing test.
- Full test suite green.
- Integration test confirms editing prompt/resolution criteria is blocked once any prediction exists, but lock-time extension is not.
- Integration test confirms the exact-lock-time-instant boundary case.
- Integration test confirms a TA cannot create, edit, cancel, or resolve questions outside their assigned season.
- Integration test confirms resolving before lock time is rejected.
- ADR entries written for: derived vs stored OPEN/LOCKED status, binary-only for v1, no-versioning edit-lock rule, and the lock-time-exclusive boundary decision.
- Integration test confirms a BINARY question rejects numeric-only fields and a CONTINUOUS question rejects yes/no-only fields at creation.
- Integration test confirms resolution enforces the correct outcome type per question.
- ADR entry added: dual question type via discriminator, plus a note flagging the scoring-combination decision as pending for Phase 4.