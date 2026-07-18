# Component: Portfolio
Scope: Per-student, per-season fund balance tracking. Owns the running total; does not compute profit/loss itself, that's Scoring's job, Portfolio just holds and exposes the number Scoring updates.

## User Story 1: Initialize a portfolio on season enrollment
As the system, I want a student to start with the season's configured starting balance the moment they enroll, so that their fund balance has a baseline to track against.
Acceptance Criteria:

Given a student enrolling in a season (Season Story 5), when enrollment succeeds, then a Portfolio record is created for that student-season pair with balance set to the season's startingBalance.
Given a student who is already enrolled, no duplicate Portfolio record is created.
Given a season's startingBalance config, then it's read at enrollment time and copied into the Portfolio, later changes to the season's config do not retroactively alter portfolios already created, consistent with your immutability principles elsewhere.

## User Story 2: View own portfolio
As a Student, I want to see my current fund balance and how it's changed, so that I can track my performance beyond just accuracy.
Acceptance Criteria:

Given an enrolled student, when they view their portfolio, then current balance, total profit/loss to date, and a per-prediction breakdown (investment, debt, interest cost, resulting gain/loss once resolved) are returned.
Given predictions not yet resolved, then they appear in the breakdown with a pending status and no profit/loss value yet, not a zero, pending and zero are different things and should be visually distinguishable.
Given a student attempting to view another student's portfolio, rejected with 403.

## User Story 3: Staff views portfolios within a season
As a TA or Professor, I want to see fund balances across students in a season, so that I can review standings, this is a precursor to the ROI leaderboard, not the leaderboard itself.
Acceptance Criteria:

Given a Professor, all portfolios in any season are visible.
Given a TA, only portfolios within their assigned season are visible; outside it, rejected with 403.
Given a Student attempting this endpoint, rejected with 403.

## Definition of Done for Portfolio

All acceptance criteria above have a passing test.
Full test suite green.
Integration test confirms a portfolio is created exactly once per student-season enrollment, with the correct starting balance snapshotted from season config at that moment.
Integration test confirms a student cannot view another student's portfolio, and a TA cannot view portfolios outside their assigned season.
ADR entry: pool-vs-fixed-range decision, and the decision to snapshot startingBalance at enrollment rather than reading it live from Season.