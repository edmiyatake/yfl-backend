## Component: Scoring
Scoring is triggered by question resolution. Computes accuracy score and financial profit/loss per prediction, updates 
Portfolio balances, and rolls individual scores up to team scores. This is the highest-bug-risk component in the 
system, since it touches real outcomes and money-equivalent balances.

## User Story 1: Trigger scoring on question resolution
As the system, I want scoring to run automatically when a question is resolved, so that scores update without a manual step.

Acceptance Criteria:

- Given a question transitions to RESOLVED (ForecastQuestion Story 6), then a scoring job runs for every non-cancelled prediction tied to that question.
- Given the resolution write and the scoring computation, then they are separate, sequential operations, if scoring fails partway through, the question remains correctly marked RESOLVED and the outcome is not lost, per the separation already established in ForecastQuestion.
- Given a scoring run that fails partway through (one prediction's calculation throws), then it does not silently skip that prediction, it either retries or is surfaced as a visible failure state staff can see and re-trigger, a silently unscored prediction is exactly the kind of bug your roadmap is warning about.
- Given a question that resolves with zero predictions submitted, then the scoring job completes as a no-op, not an error.


## User Story 2: Score a binary prediction's accuracy
As the system, I want to calculate an accuracy score for a resolved binary prediction, so that correctness and confidence both factor into the leaderboard.

Acceptance Criteria:

- Given a resolved binary question and a student's yes/no answer with a confidence percentage, when scored, then a numeric accuracy score is produced and stored on the prediction, correctness plus a confidence multiplier per your existing scoring decision, exact formula TBD in the dedicated scoring design doc.
- Given a student who answered correctly with high confidence, they score higher than a student who answered correctly with low confidence, the multiplier must reward conviction when right.
- Given a student who answered incorrectly with high confidence, decide explicitly whether this is penalized more than an incorrect low-confidence answer, this is a real design fork (symmetric penalty vs asymmetric, rewarding humility when wrong), and it directly affects whether the incentive structure encourages honest calibration or reckless overconfidence. Worth deciding deliberately in the scoring doc, not by default.
- Given a cancelled question, no accuracy score is ever computed for its predictions.


## User Story 3: Score a continuous prediction's accuracy
As the system, I want to calculate an accuracy score for a resolved continuous prediction, so that closeness to the actual value is rewarded.

Acceptance Criteria:

- Given a resolved continuous question and a student's numeric prediction, when scored, then a MAPE-based error percentage is calculated: |actual - predicted| / |actual| * 100, matching the formula already shown in your reference materials.
- Given an actual value of zero, then MAPE is undefined by division by zero, decide explicitly how this edge case is handled (a fixed fallback penalty, or an absolute-error fallback instead of percentage), don't let it throw at resolution time. Worth a specific test for this since it's a guaranteed eventual production bug otherwise, e.g. a KPI that legitimately resolves to zero.
- Given the raw MAPE result, then it needs to convert to a score directly comparable to a binary prediction's score for the mixed leaderboard, this is the scoring-combination decision flagged back in ForecastQuestion, and it has to be resolved here, not deferred further, since Leaderboard depends on it.


## User Story 4: Calculate financial profit or loss
As the system, I want to calculate profit or loss on a resolved prediction, so that a student's portfolio balance reflects the financial dimension of the game, separate from raw accuracy.

Acceptance Criteria:

- Given a resolved prediction with an investment amount and optional debt, then profit or loss is calculated as a function of accuracy/correctness against that exposure (investment + debt), exact payout formula TBD in the scoring design doc, this is the "proprietary exponents and logarithms" piece from the original materials, which you'll define your own version of, not reproduce theirs.
- Given a prediction that used debt, then the pre-calculated interest cost (locked at submission time per Prediction Story 1&2) is deducted from the outcome regardless of whether the prediction was correct, borrowing has a cost whether or not the bet paid off, this mirrors real leverage and is worth stating as an explicit rule rather than leaving it implicit.
- Given a cancelled question, no profit/loss is computed and no portfolio balance is touched for its predictions.
- Given the computed profit/loss, then it is applied to the student's Portfolio balance (Portfolio Story 1), and this update is atomic with the score computation, a scoring run that updates the score but not the balance, or vice versa, is a correctness bug, not an acceptable partial state.


## User Story 5: Prevent double-scoring
As the system, I want a resolved question to only ever be scored once, so that a re-triggered or retried job can't apply profit/loss twice to the same prediction.

Acceptance Criteria:

- Given a prediction that has already been scored, when the scoring job runs again for the same question (e.g. a retry after a partial failure), then already-scored predictions are skipped, not recomputed and reapplied.
- Given this idempotency guarantee, then it is enforced at the data level, a scoredAt timestamp or equivalent flag on the prediction, not just application-level logic that could be bypassed by a retry racing a partial failure.


## User Story 6: Roll individual scores into team scores
As the system, I want a team's score to reflect the performance of its members, so that team leaderboards are meaningful.

Acceptance Criteria:

- Given a resolved question with multiple team members having each submitted their own prediction (per your "students may work together but must submit their own predictions" rule), then the team's score for that question is derived from its members' individual scores, average vs. sum is the open question from your Backend Checklist Section 0, and needs an explicit answer here since it changes whether larger teams are structurally advantaged.
- Given a student with no team (working solo, which is explicitly in scope), their score still counts toward the individual leaderboard but has no team aggregation to contribute to.
- Given a student who left a team after submitting a prediction (Team Story 3), then their already-scored prediction's contribution to that team's historical score is preserved, consistent with the non-retroactive rule already established in Team.


## Definition of Done for Scoring

All acceptance criteria above have a passing test, with heavy unit test coverage specifically, per your roadmap's own flag that this is the highest-bug-risk area.
- Full test suite green.
- Integration test confirms scoring is idempotent under a simulated retry.
- Integration test confirms interest cost is deducted regardless of prediction correctness.
- Integration test confirms the zero-actual-value MAPE edge case doesn't throw.
- Integration test confirms cancelled questions never produce a score or portfolio change.
- A dedicated scoring design doc exists (per your roadmap's own recommendation) pinning down: the binary confidence-multiplier formula, the continuous MAPE-to-score conversion, the profit/loss payout formula, the binary/continuous combination rule for a unified leaderboard, and average-vs-sum for team rollup. ADR entries for each once decided.


