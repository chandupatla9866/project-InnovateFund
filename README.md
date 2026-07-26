# InnovateFund — From Idea to Investment

An AI-assisted startup funding platform: founders validate and publish startup profiles, investors
discover and follow them, everyone shares a common startup feed, and admins verify accounts. Beyond
the core loop, the platform also covers real-time chat, meeting scheduling, due-diligence document
rooms, notifications, events/competitions, and a set of explainable AI tools (evaluation, pitch
review, mentor Q&A, market research, founder-investor matching, meeting summaries, and fraud
flagging) — see [AI features](#ai-features) below for exactly how each one works and its limits.

## Tech stack

- **Frontend**: React 19 + Vite (JavaScript/JSX), Tailwind CSS v4, React Router, Axios, TanStack Query, Framer Motion, STOMP/WebSocket (`@stomp/stompjs`)
- **Backend**: Spring Boot 4 (Java 21), Spring Security + JWT + OAuth2 Client, Spring Data JPA (Hibernate), Spring WebSocket (STOMP)
- **Database**: PostgreSQL

## Prerequisites

- JDK 21+ (built/tested with JDK 26; JDK 21 works too — Maven itself needs `JAVA_HOME` pointed at a
  JDK new enough to support `--release 21` compilation, see note below)
- Node.js 18+
- PostgreSQL running locally

## 1. Database setup

Create the database (default connection assumes user `postgres`, password `1234`, `localhost:5432`):

```sh
psql -U postgres -h localhost -c "CREATE DATABASE innovatefund;"
```

Override credentials via environment variables if needed: `DB_USERNAME`, `DB_PASSWORD` (see
`backend/src/main/resources/application.yml`).

## 2. Run the backend

```sh
cd backend
./mvnw spring-boot:run
```

The API runs on **http://localhost:8081** (port 8080 is often taken by other local Java apps — adjust
`server.port` in `application.yml` if 8081 is also unavailable on your machine).

Swagger UI: http://localhost:8081/swagger-ui.html

> **Windows note**: if `JAVA_HOME` points at a JDK older than 21 (Maven will fail with
> `error: release version 21 not supported`), run with an explicit override, e.g.:
> `JAVA_HOME="C:\Program Files\Java\jdk-26.0.1" ./mvnw spring-boot:run`

Hibernate auto-creates/updates the schema (`ddl-auto: update`) — no manual migration step needed
beyond creating the database itself.

## 3. Run the frontend

```sh
cd frontend
npm install
npm run dev
```

Runs on **http://localhost:5173**. `frontend/.env` points it at the backend
(`VITE_API_BASE_URL=http://localhost:8081/api`) — update if you changed the backend port.

## Roles & getting started

Register as either a **Founder** or an **Investor** from the app (or via "Continue with Google" if
configured — see below). There is no self-serve Admin registration — promote an existing user
manually:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

Then log in again to pick up the new role/JWT.

## Google OAuth2 login (optional)

Google sign-in is fully implemented but safely no-ops until you configure real credentials — the
app boots and runs normally without them, and the "Continue with Google" button simply doesn't
render (the frontend checks `GET /api/auth/oauth2/enabled` first).

To enable it:
1. Create an OAuth 2.0 Client ID in [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
   (Web application), with authorized redirect URI `http://localhost:8081/login/oauth2/code/google`.
2. Set env vars before starting the backend: `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`.
3. Optionally set `OAUTH2_REDIRECT_URI` (defaults to `http://localhost:5173/oauth2/redirect`).

A brand-new Google sign-in creates a User with the role chosen on the login/register page (Founder
or Investor); an existing account (matched by email) keeps its existing role.

## AI features

Every AI feature here is a real, working implementation — heuristic/rule-based rather than a
trained model, and each one says so in its own output rather than pretending otherwise. All are
built behind clean interfaces/services so a real Gemini (or other LLM) call can be swapped in later
without touching controllers, entities, or the frontend.

| Feature | How it works today | Where |
|---|---|---|
| **Startup Evaluation** | 9-category weighted rubric scored from the startup's text via length/keyword heuristics | `ai/MockAiEvaluationService.java` |
| **Pitch Deck Review** | Reviews the startup's narrative fields as if they were slides (Problem, Solution, Market, Business Model, Competitors, Financials) | `ai/pitch/PitchReviewService.java` |
| **AI Mentor** | Rule-based Q&A — matches the question to a category (funding/valuation/hiring/scaling) and answers using the startup's own stage/funding data | `ai/mentor/MentorService.java` |
| **Market Research** | Curated lookup table for ~8 Indian startup categories (competitors, growth, challenges); generic fallback otherwise | `ai/research/MarketResearchService.java` |
| **Founder-Investor Matching** | Hybrid: industry substring match (40%) + stage-keyword match (20%) + keyword-overlap between startup fields and investor interests (40%) — explainable reasons, not embeddings | `ai/matching/MatchingService.java` |
| **Meeting Summaries** | Regex (currency amounts) + keyword sentence-matching (concerns, action items) over a pasted transcript | `meeting/service/MeetingSummaryService.java` |
| **Fraud/Spam Flags** (admin) | Rule-based: duplicate startup names, copy-pasted problem statements across founders, funding asks 10x+ typical for stage, near-empty descriptions with a funding ask | `ai/fraud/FraudDetectionService.java` |

To swap the Evaluation feature for a real Gemini call: add the Spring AI Gemini starter dependency,
implement `GeminiAiEvaluationService.evaluate()`, and set `app.ai.provider=gemini` (env var
`AI_PROVIDER=gemini`). The other five features would follow the same pattern — replace the
heuristic service body, keep the interface/DTO/controller shape.

## Other features

- **Real-time chat** — STOMP over WebSocket (`/ws`), JWT-authenticated via the STOMP CONNECT frame
  (native WebSocket handshakes can't carry custom headers). Sending is REST (`POST /api/chat/messages`)
  for reliability; the WebSocket pushes the message live to the recipient if they're online.
- **Meeting scheduling** — request/accept/reject/cancel between founder and investor, tied
  optionally to a startup.
- **Due diligence rooms** — investor requests access to a startup's documents; founder
  approves/rejects; approved investors see uploaded documents (plain URL links — no file storage/
  Cloudinary integration yet).
- **Notifications** — in-app bell with unread count, triggered on: new follower, new comment,
  admin verification, meeting request/response, due-diligence request/approval, new chat message.
- **Feed trending** — `Score = likes×1 + comments×2 + recency bonus (decays over 48h)` over the
  last 7 days of posts. No AI involved — a plain, explainable ranking formula.
- **Events & Competitions** — admin-managed, publicly browsable, with title/date/location/link.

## Project structure

```
backend/    Spring Boot API (Maven) — domain-per-package: user, founder, investor, startup, feed,
            ai (+ ai.pitch, ai.mentor, ai.research, ai.matching, ai.fraud), admin, notification,
            chat, meeting, diligence, event, oauth
frontend/   React + Vite SPA — api/, types/, context/, hooks/, components/, pages/, routes/
```

## Not yet implemented

Real Cloudinary file storage (documents/pitch decks are plain URL fields), natural-language search
(NL→SQL), real embeddings/cosine-similarity semantic matching (current matching is rule + keyword-
overlap, not vector-based), Flyway migrations (schema is Hibernate `ddl-auto: update`), and the real
Gemini-backed implementations of the six AI features above (all are heuristic/rule-based pending an
LLM API key).
