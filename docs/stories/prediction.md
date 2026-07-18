# Component: Prediction
Student submission of predictions on both BINARY and CONTINUOUS questions, immutability enforcement, ownership, and the 
lock-time boundary contract.

## User Story 1: Submit a prediction on a binary question
As a Student, I want to submit a yes/no prediction with a confidence level, so that my forecast can be scored on both correctness and confidence.
Acceptance Criteria:

- Given a BINARY question that is OPEN (before lock time) and a student enrolled in that season, when they submit a yes/no answer with a confidence percentage from 0 to 100, then the prediction is persisted linked to the student and question.
- Given a confidence value outside 0-100, or missing, then the request is rejected with 400.
- Given a student submitting to a CONTINUOUS question through this endpoint (wrong type), then the request is rejected with 400, type mismatch is caught explicitly, not silently coerced.
- Given a student not enrolled in the question's season, then the request is rejected with 403.
- Given a prediction submission, then it must include an investment amount within the season's configured minInvestment to maxInvestment range; outside that range, rejected with 400.
- Given a prediction submission, then it may optionally include a debt amount from $0 up to the season's configured maxDebt; omitted or zero means no leverage used.
- Given a debt amount greater than zero, then the interest cost (season's interestRate applied to the debt amount) is calculated and stored alongside the prediction at submission time, not recalculated later, so it's locked to the rate in effect when the student chose to borrow.
- Given a debt amount that exceeds maxDebt, then the request is rejected with 400.
- Given investment and debt together, then total exposure (investment + debt) is what gets applied against the outcome once resolved, this is the amount Scoring will use, not investment alone.


## User Story 2: Submit a prediction on a continuous question
As a Student, I want to submit a numeric prediction, so that my forecast can be scored on how close it was to the actual value.

Acceptance Criteria:

- Given a CONTINUOUS question that is OPEN and a student enrolled in that season, when they submit a numeric value, then the prediction is persisted linked to the student and question.
- Given a non-numeric or malformed value, then the request is rejected with 400.
- Given a student submitting to a BINARY question through this endpoint, then the request is rejected with 400, same type-mismatch guard as Story 1.
- Given no confidence field is required for continuous predictions, then omitting it succeeds, confidence is a binary-only concept per your original design and current scoring plan.
- Given a prediction submission, then it must include an investment amount within the season's configured minInvestment to maxInvestment range; outside that range, rejected with 400.
- Given a prediction submission, then it may optionally include a debt amount from $0 up to the season's configured maxDebt; omitted or zero means no leverage used.
- Given a debt amount greater than zero, then the interest cost (season's interestRate applied to the debt amount) is calculated and stored alongside the prediction at submission time, not recalculated later, so it's locked to the rate in effect when the student chose to borrow.
- Given a debt amount that exceeds maxDebt, then the request is rejected with 400.
- Given investment and debt together, then total exposure (investment + debt) is what gets applied against the outcome once resolved, this is the amount Scoring will use, not investment alone.

## User Story 3: Reject submissions after lock time
As the system, I want to reject any prediction submitted at or after a question's lock time, so that no one predicts with an unfair time advantage.

Acceptance Criteria:

- Given a question whose lock time has not passed, a submission succeeds.
- Given a question whose lock time has passed, a submission is rejected with 400, regardless of question type.
- Given a submission arriving at exactly the lock time instant, it is rejected, per the exclusive-boundary rule already established in ForecastQuestion Story 4 (now < lockTime required). This is the boundary test your Session Workflow doc specifically calls out, write it explicitly, don't rely on it being covered incidentally by the general late-rejection test.


## User Story 4: Enforce one prediction per student per question
As the system, I want to prevent a student from submitting more than one prediction for the same question, so that the game stays fair and consistent with the one-shot format described in the brief.

Acceptance Criteria:

- Given a student who has not yet predicted on a question, their first submission succeeds.
- Given a student who already has a prediction for that question, a second submission attempt is rejected with 400, no update-in-place, no second row.
- Given this rule, then it holds independent of question type, BINARY and CONTINUOUS both allow exactly one submission per student per question.


## User Story 5: Prevent edits or deletes after submission
As the system, I want predictions to be immutable once submitted, so that no one can revise an answer after seeing how events unfold.

Acceptance Criteria:

- Given a submitted prediction, no PATCH or DELETE endpoint exists for it, this isn't a permission check to write a test around, it's an absence of capability, confirm no route accepts a modification to an existing prediction.
- Given a staff member (TA or Professor) attempting to alter a student's prediction directly, then this is also rejected, immutability applies regardless of role. If a correction is ever genuinely needed (student fat-fingered a submission), that's a question cancellation or a manual DB intervention with an audit trail, not an in-app edit path, don't build a backdoor that undermines the fairness guarantee.


## User Story 6: View own prediction history
As a Student, I want to view all my past predictions, so that I can track how I've been forecasting.

Acceptance Criteria:

- Given a student with prior predictions, when they call the history endpoint, then only their own predictions are returned, across both question types, with the question's prompt, type, their submitted answer, and (once RESOLVED) the actual outcome.
- Given a student with no predictions yet, then an empty list is returned, not an error.
- Given a student attempting to view another student's predictions by ID, then the request is rejected with 403.


## User Story 7: Staff views predictions for a question
As a TA or Professor, I want to see all submitted predictions for a question, so that I can verify participation and prepare for resolution.
Acceptance Criteria:

- Given a Professor, when they view predictions for any question, then all submissions are returned with student identity attached.
- Given a TA, when they view predictions for a question in their assigned season, then the same applies; outside their assigned season it's rejected with 403.
- Given this view is requested before the question's lock time has passed, decide whether staff can see submissions in progress or only after lock. I'd allow staff to see submissions as they come in, since staff aren't players and seeing partial submission counts doesn't create an unfair advantage the way a student seeing it would, this also helps staff gauge participation and nudge stragglers before the deadline.


## User Story 8: Reject predictions on cancelled or resolved questions
As the system, I want to reject any submission attempt on a question that is no longer accepting predictions, so that the OPEN window is the only valid submission window.

Acceptance Criteria:

- Given a question in CANCELLED status, a submission attempt is rejected with 400.
- Given a question in RESOLVED status, a submission attempt is rejected with 400, this should already be unreachable since resolution only happens after lock time, but test it directly rather than relying on the lock-time check alone to catch it.

## User Story 9: Reject predictions with invalid financial inputs
As the system, I want to reject malformed or out-of-range financial inputs before a prediction is persisted, so that 
no prediction row exists with an invalid investment or debt value.

Acceptance Criteria:

- Given an investment amount below minInvestment or above maxInvestment, rejected with 400 before any prediction row is written.
- Given a negative debt amount, rejected with 400.
- Given a season whose config fields (minInvestment, etc.) are somehow null or unset, the submission 
is rejected with a clear, non-500 error rather than silently allowing an unbounded investment, this is a data-integrity 
guard on your own season config, not user input.

## Definition of Done for Prediction

All acceptance criteria above have a passing test.
- Full test suite green.
- Integration test confirms the exact-lock-time-instant boundary rejection, for both question types.
- Integration test confirms type mismatch is rejected (binary answer submitted to a continuous question and vice versa).
- Integration test confirms no modification path exists for a submitted prediction, for students or staff.
- Integration test confirms a student cannot view another student's prediction history.
- Given an investment amount below minInvestment or above maxInvestment, rejected with 400 before any prediction row is written.
- Given a negative debt amount, rejected with 400.
- Given a season whose config fields (minInvestment, etc.) are somehow null or unset, the submission is rejected with a clear 500-avoidant error rather than silently allowing an unbounded investment, this is a data-integrity guard on your own season config, not user input.