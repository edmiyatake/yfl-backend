# Component: Authentication

Arguably the most important part of this project is the authentication and authorization. There was a problem with
cheating in the old system because Excel sheets could be edited and viewed by anyone regardless of role. The first 
step to combatting this is authentication. To minimize the time it takes to complete a prediction flow for a user, 
we will use passwordless login. We will issue a stateless JWT access token for each session, backed by a stateful, 
revocable refresh token, so we get fast per-request authorization checks without sacrificing the ability to log a 
user out or revoke a compromised session. The JWT carries the user's role as a claim, which Spring Security checks 
against each endpoint to enforce authorization.

## User Story 1: Request a login code
As a user, I want to request a one-time code sent to my email, so that I can log in without a password.

Acceptance Criteria:

- Given a valid email address, when the user requests a code, then a 6-digit cryptographically random code is 
generated and persisted with a 10-minute expiry, and the response is 200.
- Given an email address that has never been seen before, when a code is requested, then the request still succeeds.
- Given a user who requests a second code before the first one expires, then the old code is invalidated and only 
the newest code is valid. (Decision made: single active code per email, closes the ambiguity from before.)
- Given a malformed or missing email, when a code is requested, then the request is rejected with 400 before any 
code is generated.
- Rate limiting: Given more than 5 code requests for the same email within 15 minutes, when another request comes
in, then it is rejected with 429 rather than generating and sending another code.
- Rate limiting: Given more than a reasonable threshold of code requests from the same IP address in a short 
window, when another request comes in, then it is rejected with 429. This protects against someone enumerating 
emails or spamming inboxes at scale.

## User Story 2: Verify a login code
As a user, I want to submit the code I received, so that I can prove ownership of my email and get authenticated.

Acceptance Criteria:

- Given a valid, unexpired code that matches what was issued for that email, when the user submits it, then the 
request succeeds and returns an access token and sets a refresh token as an httpOnly cookie.
- Given an expired code, when the user submits it, then the request is rejected with 400 and no token is issued.
- Given a code that doesn't match what was issued for that email, when the user submits it, then the request is 
rejected with 400 and no token is issued.
- Given a code that has already been successfully used once, when the same code is submitted again, then the 
request is rejected. Mark the code row as consumed on first successful verify, check that flag alongside expiry.
- Given a successful verification, then the issued access token contains the user's ID and role as claims, and the 
refresh token is stored server-side, or otherwise made revocable, so logout can actually invalidate it.
- Rate limiting: Given more than 5 failed verify attempts for the same email within a short window, when another 
attempt comes in, then it is rejected with 429 regardless of whether the code would have been correct. This is what 
actually stops brute-forcing a 6-digit code.
- Audit logging: Given any verify attempt, successful or failed, then a structured log entry is written recording 
the email, outcome, and timestamp, without logging the code itself.

## User Story 3: Access token authorizes a protected request
As an authenticated user, I want my access token to grant access to endpoints matching my role, so that the API 
enforces who can do what.

Acceptance Criteria:

- Given a valid, unexpired access token, when the user calls a protected endpoint appropriate to their role, then 
the request succeeds.
- Given a valid access token but a role that doesn't match the endpoint's required role, then the request is rejected
with 403.
- Given a missing or malformed Authorization header, then the request is rejected with 401.
- Given an expired access token, then the request is rejected with 401, not 403.
- Given a ResponseStatusException thrown from a protected endpoint, then the error body still renders correctly 
rather than masking as a raw 403 through /error.
- Revocation: Given an access token whose associated session has been revoked (via logout or an admin-forced 
revocation), when it's used to call a protected endpoint, then the request is rejected with 401 even though the 
token itself hasn't technically expired yet. This is the piece that makes access tokens short-lived and checkable 
rather than trust-until-expiry.

## User Story 4: Refresh an expired access token
As a returning user, I want my session to renew automatically using my refresh token, so that I don't have to 
re-enter a code every few minutes.

Acceptance Criteria:

- Given a valid, unexpired refresh token cookie, when the client calls the refresh endpoint, then a new access token 
is issued.
- Given an expired or revoked refresh token, when the client calls the refresh endpoint, then the request is rejected 
with 401.
- Given a successful refresh, then the refresh token is rotated, the old refresh token is invalidated and a new one 
issued, so a stolen refresh token has a limited window of usefulness.
- Reuse detection: Given a refresh token that has already been rotated (used once and replaced), when it's presented 
again, then this is treated as a signal of possible theft, and all sessions for that user are revoked, forcing 
re-authentication. This is a standard OAuth-style protection and is worth having even though you're not running a 
full OAuth2 server.

## User Story 5: Log out
As a user, I want to log out, so that my session can't be reused from this device or if the token is intercepted.

Acceptance Criteria:

- Given a logged-in user, when they call logout, then their refresh token is invalidated server-side and the 
httpOnly cookie is cleared.
- Given a refresh token that was just invalidated by logout, when it's used to call refresh, then the request is 
rejected with 401.


## User Story 6: Deliver OTP codes reliably
As a user, I want my login code to actually arrive in my inbox promptly, so that I can log in without support 
tickets piling up.

Acceptance Criteria:

- Given a code request, when the email is sent, then it goes through a transactional email provider (not ad hoc SMTP),
so delivery and bounce handling are tracked.
- Given an email send failure at the provider level, when this happens, then it is logged and surfaced distinctly 
from a validation failure, so you can tell "the code was never delivered" apart from "the user typed it wrong."
- Given a provider outage, then code requests still return a sensible response rather than hanging or 500ing 
opaquely.


## Definition of Done for Auth

- All acceptance criteria above have a passing test.
- Full test suite green (./mvnw test).
- Integration test confirms a Student token cannot hit a Staff-only endpoint.
- /error is confirmed permitAll in SecurityConfig.
- OTP codes are single-use and single-active-per-email.
- Refresh tokens rotate on use, with reuse detection triggering full session revocation.
- Rate limiting is in place on both request-code and verify-code.
- Audit log entries exist for code requests and verify attempts, without ever logging the code value itself.
- Email delivery goes through a real transactional provider, not deferred indefinitely.
- ADR entries written for: single-active-code decision, refresh rotation with reuse detection, and choice of 
email provider.