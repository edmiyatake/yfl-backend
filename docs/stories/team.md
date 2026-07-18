# Component: Team
Originally, this was not in scope but since I have time, I have decided to implement. This section covers Team creation, 
membership, roster visibility, staff oversight. All team actions are scoped within a season, since a team only exists 
inside one season's context.

## User Story 1: Create a team
As a Student, I want to create a team within my enrolled season, so that I can compete alongside teammates.

Acceptance Criteria:

- Given a Student enrolled in an ACTIVE or DRAFT season, when they create a team with a name, then the team is persisted, 
scoped to that season, and the creator is automatically added as its first member.
- Given a Student not enrolled in the season they're trying to create a team in, then the request is rejected with 403.
- Given a Student already on a team in that season, when they attempt to create another team in the same season, then the
request is rejected with 400, one team per student per season.
- Given a team name that already exists within that season, then decide whether to reject as a duplicate or allow it,
I'd reject with 400 and require uniqueness within a season, since duplicate team names will make the leaderboard confusing 
and support requests annoying.
- Given a season in CLOSED status, when a student attempts to create a team, then the request is rejected with 400.


## User Story 2: Join a team
As a Student, I want to join an existing team in my season, so that I can compete with others instead of alone.

Acceptance Criteria:

- Given a Student enrolled in the same season as the target team, when they join, then they are added as a member.
- Given a Student already on a different team in that season, when they attempt to join another, then the request is 
rejected with 400, they must leave their current team first (see Story 3), not silently switch.
- Given a Student not enrolled in the team's season, then the request is rejected with 403.


## User Story 3: Leave a team
As a Student, I want to leave my current team, so that I can compete solo or join a different team.

Acceptance Criteria:

- Given a Student who is a member of a team, when they leave, then their membership is removed, and they become teamless 
(but still enrolled in the season, working alone is explicitly in scope per your brief).
- Given a Student who is the sole remaining member of a team, when they leave, then decide whether the now-empty team is 
auto-disbanded or left as an empty shell. I'd auto-disband it, an empty team with no members serves no purpose and would 
otherwise clutter the season's team list.
- Given historical predictions or scores already tied to that student while they were on the team, then leaving does not 
retroactively change or delete that history, per-prediction attribution is fixed at submission time (this matters once 
Scoring exists, but the rule belongs here since it's a Team-membership decision).


## User Story 4: View team roster
As a user, I want to see who is on a given team, so that I know my teammates or can review a team's composition.
Acceptance Criteria:

- Given any Student enrolled in the season, when they view a team's roster within that season, then the member list is returned.
- Given a Student attempting to view a team roster in a season they're not enrolled in, then the request is rejected with 403.
- Given a TA viewing rosters in their assigned season, or a Professor viewing any season, then the roster is returned 
regardless of team membership.


## User Story 5: List teams within a season
As a user, I want to see all teams in a season, so that I can decide which one to join.

Acceptance Criteria:

- Given a Student enrolled in a season, when they list teams, then all teams in that season are returned with at least 
name and member count.
- Given a user not enrolled in and not staff-assigned to that season, then the request is rejected with 403.


## User Story 6: Staff removes a student from a team
As a TA or Professor, I want to remove a student from a team, so that I can correct membership issues (rule violations, 
mistaken joins, roster disputes).

Acceptance Criteria:

- Given a Professor, when they remove a student from any team, then the membership is deleted.
- Given a TA, when they remove a student from a team within their assigned season, then the membership is deleted; 
attempting this outside their assigned season is rejected with 403.
- Given a Student attempting to remove another student from a team, then the request is rejected with 403, only staff can 
force-remove. Shouldn't even be possible for a student to see the option to remove another student.
- Given this action, then it's audit-logged with actor, affected student, and team, since it's a dispute-relevant action.


## User Story 7: Staff disbands a team
As a TA or Professor, I want to disband a team entirely, so that I can clean up mistakenly created or duplicate teams.

Acceptance Criteria:

- Given a Professor, when they disband a team, then the team and all its memberships are removed; enrolled students 
remain in the season but become teamless.
- Given a TA, when they disband a team within their assigned season, then the same applies; outside their assigned season 
it's rejected with 403.
- Given a team with existing prediction history tied to its members, then disbanding the team does not delete or alter 
that prediction history, same non-retroactive rule as Story 3.


## Definition of Done for Team

- All acceptance criteria above have a passing test.
- Full test suite green.
- Integration test confirms a student cannot belong to two teams in the same season.
- Integration test confirms a TA cannot manage teams outside their assigned season.
- Integration test confirms leaving a team as the last member auto-disbands it, without affecting that member's historical 
predictions.
- ADR entries written for: team name uniqueness within a season, no team size cap for v1, and auto-disband-on-empty behavior.
