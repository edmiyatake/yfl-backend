# Component: Leaderboard
The stakeholder mentioned that this is his favorite part of the original system and I got to replicate it. Read/aggregation 
layer over Scoring's output. No new writes of its own beyond what's needed for caching; this component queries and ranks 
data that Scoring already produced.

## User Story 1: Individual accuracy leaderboard
As a user, I want to see students ranked by average accuracy score, so that I know who's forecasting best.

Acceptance Criteria:

- Given a season, when the accuracy leaderboard is requested, then enrolled students are ranked descending by average accuracyScore across all their resolved (non-cancelled) predictions.
- Given a student with zero resolved predictions, then they appear unranked or excluded, not ranked with an undefined average, decide which explicitly, I'd exclude them from the ranked list entirely and surface them separately as "not yet ranked," rather than showing a misleading placeholder score.
- Given a Student, TA, or Professor requesting this, then visibility follows the same season-scoping rules as everywhere else, Professors see any season, TAs see their assigned season, Students see the season they're enrolled in.


## User Story 2: Individual ROI leaderboard
As a user, I want to see students ranked by portfolio return, so that I know who's making the best financial decisions, 
not just the most accurate calls.

Acceptance Criteria:

- Given a season, when the ROI leaderboard is requested, then enrolled students are ranked descending by (currentBalance - startingBalance) / startingBalance, using the Portfolio balance Scoring already maintains.
- Given a student with no resolved predictions yet, then their ROI is 0% (unchanged from starting balance), which is a valid, rankable value, unlike accuracy this doesn't need an "unranked" state, a flat 0% is a real answer.
- Given the same visibility scoping as Story 1.


## User Story 3: Most improved accuracy leaderboard
As a user, I want to see who has improved the most recently, so that growth is recognized, not just raw standing.

Acceptance Criteria:

- Given a student with at least window + 1 resolved predictions, then their improvement score is (average of last window predictions' accuracyScore) - (average of all prior predictions' accuracyScore).
- Given a student with fewer than window + 1 resolved predictions, then they do not appear on this leaderboard, and the response distinguishes "not yet eligible" from "eligible but low improvement," don't collapse both into a missing/zero entry.
- Given the season's improvementWindow config field, then it defaults to 10 and is set at season creation, same configuration pattern as the financial fields.


## User Story 4: Most improved ROI leaderboard
As a user, I want to see whose financial performance is trending upward, so that recent good decisions are recognized.

Acceptance Criteria:

- Given a student with at least window + 1 resolved predictions, then their ROI improvement is the trailing-window profit/loss rate compared to the prior-period profit/loss rate, same window and eligibility rule as Story 3, applied to profitLoss instead of accuracyScore.
- Given the same eligibility and visibility rules as Story 3.


## User Story 5: Team leaderboards
As a user, I want to see teams ranked by accuracy and by ROI, so that team competition is visible alongside individual 
standing.

Acceptance Criteria:

- Given a season, when the team accuracy leaderboard is requested, then teams are ranked by the average member accuracyScore per resolved question, aggregated across the season, per the rollup rule already defined in Scoring.
- Given a season, when the team ROI leaderboard is requested, then teams are ranked by average member portfolio ROI.
- Given a team with zero resolved predictions across all members, then it's excluded from ranking, same unranked-not-zero treatment as Story 1.
- Given solo students (no team), then they never appear on team leaderboards, only individual ones, consistent with Team's existing rules.


## User Story 6: Leaderboard updates when scores change
As a user, I want the leaderboard to reflect the latest resolved question shortly after it resolves, so that standings stay 
current per the brief's "everyone can see their leaderboard all the time" requirement.

Acceptance Criteria:

- Given a scoring run completes for a resolved question (Scoring Story 1), then subsequent leaderboard requests reflect the updated scores, near-real-time is acceptable, the brief does not require sub-second consistency, but "next request sees it" is the bar.
- Given the read-heavy nature of this endpoint at your 5,000-user target, then leaderboard results are cached with a short TTL (or invalidated explicitly on scoring completion) rather than recomputed from raw prediction rows on every single request, this is the caching decision your Backend Checklist flags as needed once you're near the user target, worth building the seam now even if the cache is a no-op in early testing.
- Given a cache invalidation approach, then it is triggered by the same event that completes a scoring run, not by a separate polling job, so there's one clear point of truth for "when did this leaderboard last change."


## User Story 7: Leaderboard entries expose enough context to be useful
As a user, I want to see more than just a rank and a number, so that the leaderboard is actually informative.

Acceptance Criteria:

- Given a leaderboard entry, then it includes student or team display name, rank, the relevant score (accuracy, ROI, or improvement delta), and a count of resolved predictions contributing to that score, so a viewer can tell a high score from 3 predictions apart from one from 40.
- Given a request for a leaderboard, then pagination is supported, matching the same reasoning as User Story 5 in Role Management, this doesn't need to return all 5,000 users in one payload.


## Definition of Done for Leaderboard

All acceptance criteria above have a passing test.
- Full test suite green.
- Integration test confirms unranked (zero resolved predictions) students/teams are excluded from accuracy/team leaderboards but appear at 0% on ROI leaderboards, per the distinction drawn in Stories 1 and 2.
- Integration test confirms Most Improved eligibility rule (window + 1 minimum) is enforced correctly at the boundary (exactly window predictions vs window + 1).
- Integration test confirms cache invalidation actually reflects a newly resolved question's scores, not stale data.
- Load test (Phase 5, but worth flagging here) specifically targets this endpoint, since it's explicitly your highest-read-volume one per the brief.
- ADR entries written for: Most Improved baseline-vs-window definition, unranked-vs-zero treatment per leaderboard type, and the caching/invalidation approach chosen.