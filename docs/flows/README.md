# ŷFL - Financial Forecasting League

A competitive financial forecasting platform built for Professor Savlowitz and Competitive Analytics. Students predict real-world financial events, manage a virtual portfolio, and compete on a live leaderboard across an academic season.

Built as a production-grade web platform to replace a manual Excel/Alteryx/Google Drive workflow.

---

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1.0 |
| Database | PostgreSQL 16 (Docker) |
| Migrations | Flyway |
| Frontend | Next.js (App Router), TypeScript, Tailwind CSS |
| Auth | Passwordless OTP + JWT |

---

## Running Locally

### Prerequisites
- Java 21
- Docker Desktop
- Node.js 18+

### Backend

```bash
# Start the database
docker compose up -d

# Run the backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd yfl-frontend
npm install
npm run dev
```

App runs at `http://localhost:3000`
API runs at `http://localhost:8080`

---

## User Flows

### Authentication
Designed a passwordless OTP auth flow that handles email validation, code expiry, and role-based redirects so students, TAs, and professors each land in the right place after login

![Auth Flow](docs/flows/yfl_auth_flow.png)

> Uses a silent 200 response on unrecognized emails to prevent user enumeration.

### Student Onboarding
![Student Onboarding](docs/flows/student-onboarding-flow.png)

> Students join teams via an alphanumeric season-scoped code, similar to Jackbox.

### Student Game Loop
![Student Game Loop](docs/flows/student-game-loop-flow.png)

> Predictions are locked at question close time and cannot be edited after submission.

### Prediction Submission
![Prediction Submission](docs/flows/student-prediction-submission-flow.png)

> Supports binary (Yes/No) and continuous (numeric) question types with investment and confidence inputs.

---

## Architecture Notes

See `/docs/adr/` for Architecture Decision Records covering key decisions made during development.

---

## Project Status

| Phase | Description | Status |
|---|---|---|
| 0 | Scaffolding and environment | Done |
| 1 | Auth and roles | In Progress |
| 2 | Seasons and teams | Not Started |
| 3 | Forecast questions and predictions | Not Started |
| 4 | Scoring and leaderboard | Not Started |
| 5 | Hardening and performance | Not Started |
| 6 | API docs and frontend integration | Not Started |
| 7 | Polish and portfolio pass | Not Started |

---

## Author

Edwin Miyatake
Built in partnership with Competitive Analytics and Professor Savlowitz.