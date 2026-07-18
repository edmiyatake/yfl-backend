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

- Given a student who answered correctly with high confidence, they score higher than a student who answered correctly with low confidence, the multiplier must reward conviction when right.
- Given a resolved binary question and a student's yes/no answer with a confidence percentage, when scored, then accuracyScore is computed as (1 - (confidence - outcome)^2) * 100, where confidence is the stated probability as a decimal and outcome is 1 or 0. This is a proper scoring rule, so overconfidence when wrong is already penalized more heavily than underconfidence when wrong, by construction, not by a separately tuned multiplier.
- Given a student who answered incorrectly with high confidence, decide explicitly whether this is penalized more than an incorrect low-confidence answer, this is a real design fork (symmetric penalty vs asymmetric, rewarding humility when wrong), and it directly affects whether the incentive structure encourages honest calibration or reckless overconfidence. Worth deciding deliberately in the scoring doc, not by default.
- Given a cancelled question, no accuracy score is ever computed for its predictions.


## User Story 3: Score a continuous prediction's accuracy
As the system, I want to calculate an accuracy score for a resolved continuous prediction, so that closeness to the actual value is rewarded.

Acceptance Criteria:

- Given a resolved continuous question and a student's numeric prediction, when scored, then a MAPE-based error percentage is calculated: |actual - predicted| / |actual| * 100, matching the formula already shown in your reference materials.
- Given actual == 0 and predicted == 0, accuracyScore = 100. Given actual == 0 and predicted != 0, accuracyScore = 0. Otherwise, MAPE = |actual - predicted| / |actual| * 100 and accuracyScore = max(0, 100 - MAPE).
- Given accuracyScore is constructed on the same 0-100 scale as the binary formula above, no separate conversion step is needed for the mixed leaderboard, both types are natively comparable.

## User Story 4: Calculate financial profit or loss
As the system, I want to calculate profit or loss on a resolved prediction, so that a student's portfolio balance reflects the financial dimension of the game, separate from raw accuracy.

Acceptance Criteria:

- Given a resolved prediction's accuracyScore (from Story 2 or 3) and its exposure (investment + debt), then profitLoss = exposure * (2 * (accuracyScore / 100) - 1). At accuracyScore = 100, this returns +exposure; at 50, breakeven; at 0, -exposure. finalResult = profitLoss - interestCost, using the interest cost already locked at submission time.
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

- Given a resolved question with multiple team members each having submitted their own prediction, then the team's score for that question is the average of its members' accuracyScore values, not the sum, so larger teams aren't structurally advantaged over smaller, more accurate ones.
- Given a student with no team (working solo, which is explicitly in scope), their score still counts toward the individual leaderboard but has no team aggregation to contribute to.
- Given a student who left a team after submitting a prediction (Team Story 3), then their already-scored prediction's contribution to that team's historical score is preserved, consistent with the non-retroactive rule already established in Team.


## Definition of Done for Scoring

All acceptance criteria above have a passing test, with heavy unit test coverage specifically, per your roadmap's own flag that this is the highest-bug-risk area.
- Full test suite green.
- Integration test confirms scoring is idempotent under a simulated retry.
- Integration test confirms interest cost is deducted regardless of prediction correctness.
- Integration test confirms the zero-actual-value MAPE edge case doesn't throw.
- Integration test confirms cancelled questions never produce a score or portfolio change.
- A dedicated scoring design doc (yFL_Scoring_Design.md) documents the Brier score formula, the bounded-MAPE formula, the linear profit/loss formula, and the average-based team rollup, with ADR entries for each.

