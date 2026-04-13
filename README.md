# KIACMS

KIACMS (Korea IT Academy Class Management System) is an AI-powered class management platform designed for real educational operations rather than a generic LMS.

This repository is organized as a monorepo with:

- `backend/`: Spring Boot API server
- `frontend/`: React + Vite web client
- `infra/`: local infrastructure such as PostgreSQL for development
- `docs/`: architecture and implementation notes

## Step 1 Output

Step 1 establishes the implementation-ready architecture for the project:

- overall system architecture
- backend and frontend directory structure
- domain decomposition
- core entity relationship design
- security baseline and implementation principles

See [docs/architecture/step-01-system-architecture.md](docs/architecture/step-01-system-architecture.md).

## Step 2 Output

Step 2 defines the PostgreSQL ERD and generates the initial Spring Boot JPA entity layer.

- detailed table and enum design
- relationship and constraint proposal
- initial backend entity code under `backend/src/main/java/com/kiacms`

See [docs/architecture/step-02-erd-and-entities.md](docs/architecture/step-02-erd-and-entities.md).

## Step 3 Output

Step 3 makes the backend project runnable with shared runtime foundations.

- Spring Boot backend bootstrap structure
- common API response format
- global exception handling
- repository skeletons
- health check endpoint
- local and test configuration examples

See [docs/architecture/step-03-backend-bootstrap.md](docs/architecture/step-03-backend-bootstrap.md).

## Planned Delivery Order

1. Step 1: architecture and project structure
2. Step 2: DB entity and ERD design
3. Step 3: Spring Boot project initialization
4. Step 4: authentication, authorization, and approval flow
5. Step 5: course, session, and calendar features
6. Step 6: notes, tags, and comments
7. Step 7: project recruiting and applications
8. Step 8: notifications
9. Step 9: AI features
10. Step 10: React frontend integration
11. Step 11: security review and refactoring
12. Step 12: README and run guide

## Local Infrastructure

The repository already includes a local PostgreSQL compose file for development.

1. Copy `.env.example` to `.env`.
2. Start PostgreSQL:

```powershell
docker compose -f infra/docker-compose.yml --env-file .env up -d
```

3. Stop PostgreSQL:

```powershell
docker compose -f infra/docker-compose.yml --env-file .env down
```

## Frontend Run Guide

The React client lives in `frontend/` and is built with Vite + React Router.

### 1. Install

```powershell
cd frontend
npm install
```

### 2. Environment Variables

Create `frontend/.env` from `frontend/.env.example`.

```powershell
Copy-Item .env.example .env
```

Default example:

```env
VITE_API_BASE_URL=http://localhost:8080
```

### 3. Run in Development

```powershell
cd frontend
npm run dev
```

The default Vite dev server runs on `http://localhost:5173`.

### 4. Production Build

```powershell
cd frontend
npm run build
```

Optional preview:

```powershell
cd frontend
npm run preview -- --host
```

## Frontend Integration Notes

### Authentication

- Login API: `POST /api/auth/login`
- Signup API: `POST /api/auth/signup`
- The access token returned from login is stored in `localStorage` under `kiacms.accessToken`.
- The current user snapshot is stored in `localStorage` under `kiacms.user`.
- Pending signup information is stored in `sessionStorage` under `kiacms.pendingSignup`.

### Authorization Header Injection

The shared client is implemented in `frontend/src/api/http.js`.

- `request()` reads the access token from local storage.
- When a token exists, it injects `Authorization: Bearer <token>`.
- Public endpoints such as login and signup call the client with `auth: false`.

### 401 Handling

- On `401`, the client clears the saved token and cached user.
- It dispatches the `kiacms:unauthorized` browser event.
- `AuthContext` listens for that event and logs the user out cleanly.
- Protected pages then route back through the existing auth guard flow.

## Current Frontend Scope

### Common

- Login / signup / pending approval
- Role-based routing and home redirects
- App shell with sidebar, topbar, notification dropdown, and theme toggle
- Shared API client, auth context, theme context, and route guards

### Student

- Dashboard
- Course calendar
- Session detail
- Note list / create / detail / edit
- Project recruiting post create
- My recruiting posts
- My project applications

### Instructor

- Dashboard
- Assigned session management
- Tagged note list and detail
- Mentor application status

### Mentor

- Dashboard
- Mentor application status

### Root

- Dashboard
- Root course/admin placeholder screen

## Known Gaps / TODO

These items are intentionally shown in the frontend as placeholders, disabled actions, or TODO cards because matching backend APIs are not available yet.

- My page update APIs are missing:
  - profile update
  - password change
  - account withdrawal
- Root detailed operation APIs are limited:
  - richer approval analytics
  - course/session aggregate metrics
  - 운영 로그/감사 로그 대시보드
- Mentor-specific student management APIs are missing:
  - managed student list
  - mentee course progress summary
  - mentee note/project participation summary
- Instructor tag UX has a backend constraint:
  - note creation supports `taggedInstructorIds`
  - note edit does not support tag removal or replacement
  - there is no instructor search API yet, so create/edit uses a minimal input flow
- Project management is currently read/create/decision oriented:
  - no project post edit API
  - no project post delete/close API from the frontend
- Notification target URLs depend on backend-provided `targetUrl`; if a backend event does not provide one, the frontend falls back to the notifications page.

## Suggested Next Priorities

1. Add user self-service APIs for profile update, password change, and withdrawal.
2. Add mentor management APIs for assigned students and learning summaries.
3. Add richer root dashboards for approvals, courses, sessions, and audit signals.
4. Add instructor search/select API so note tagging can move from manual input to searchable UX.
5. Add project post edit/close APIs and align PM management UI with those actions.
6. Add pagination and search for notifications, notes, and project lists.

## Notes

- Do not commit real API keys or passwords.
- Notion integration is intentionally deferred to a secure interface-first design.
