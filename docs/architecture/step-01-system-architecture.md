# KIACMS Step 1 - System Architecture

## 1. System Goal

KIACMS is a role-based educational operations platform that connects:

- students
- instructors
- mentors
- root administrators

The platform combines class management, learning notes, project recruiting, notifications, and AI assistance in a single secure web service.

## 2. High-Level Architecture

```text
+---------------------------+
| React + Vite Frontend     |
| - role-based UI           |
| - calendar / notes / PM   |
| - notification center     |
+------------+--------------+
             |
             | HTTPS / REST
             v
+---------------------------+
| Spring Boot Backend       |
| - Auth / Approval         |
| - Course / Session        |
| - Notes / Comments        |
| - Projects / Applications |
| - Mentoring / Mapping     |
| - Notifications           |
| - AI Integration          |
| - Notion Integration      |
+------------+--------------+
             |
   +---------+----------+-------------------+
   |                    |                   |
   v                    v                   v
+--------+       +-------------+     +-------------+
|Postgres|       | OpenAI API  |     | Notion API  |
|Primary |       | AI features |     | optional    |
|storage |       | summaries   |     | export      |
+--------+       +-------------+     +-------------+
```

## 3. Architecture Principles

### 3.1 Backend style

- Use a modular monolith with clear domain boundaries.
- Keep shared cross-cutting concerns in `global/`.
- Use package-by-domain inside the backend for maintainability.
- Prefer explicit services and DTOs over over-engineered abstraction.

### 3.2 Frontend style

- Use React + Vite with feature-oriented folders.
- Centralize API clients and auth state.
- Split common layout/components from feature screens.
- Keep role-based routes explicit rather than deeply dynamic.

### 3.3 Security baseline

- Authentication: short-lived access token plus refresh token in HttpOnly cookie.
- Authorization: Spring Security with route and service-level ownership checks.
- Pending or rejected users cannot access business APIs.
- Password storage: BCrypt.
- Input validation: Bean Validation on DTOs.
- Persistence: JPA with parameter binding, avoid string-built SQL.
- Soft delete: withdrawn users excluded from default queries.
- Sensitive credentials: backend only, stored via environment variables or encrypted storage.
- XSS risk reduction: validate input, sanitize rendered rich text, do not trust client HTML.
- CORS: allow only known frontend origins.
- Logging: audit important admin approval and status transition events.

## 4. Recommended Repository Layout

```text
KIACMS/
|-- backend/
|   |-- build.gradle
|   |-- settings.gradle
|   `-- src/
|       |-- main/
|       |   |-- java/com/kiacms/
|       |   |   |-- global/
|       |   |   |   |-- config/
|       |   |   |   |-- security/
|       |   |   |   |-- exception/
|       |   |   |   |-- response/
|       |   |   |   |-- audit/
|       |   |   |   `-- util/
|       |   |   |-- auth/
|       |   |   |-- user/
|       |   |   |-- approval/
|       |   |   |-- course/
|       |   |   |-- sessionresource/
|       |   |   |-- note/
|       |   |   |-- project/
|       |   |   |-- mentor/
|       |   |   |-- notification/
|       |   |   |-- ai/
|       |   |   `-- integration/
|       |   `-- resources/
|       |       |-- application.yml
|       |       |-- application-local.yml
|       |       `-- db/migration/
|       `-- test/
|-- frontend/
|   |-- package.json
|   |-- vite.config.ts
|   `-- src/
|       |-- app/
|       |-- api/
|       |-- auth/
|       |-- components/
|       |-- layouts/
|       |-- pages/
|       |-- features/
|       |   |-- dashboard/
|       |   |-- courses/
|       |   |-- notes/
|       |   |-- projects/
|       |   |-- notifications/
|       |   |-- mentor/
|       |   |-- admin/
|       |   `-- settings/
|       |-- hooks/
|       |-- store/
|       |-- styles/
|       `-- utils/
|-- infra/
|   `-- docker-compose.yml
`-- docs/
    `-- architecture/
        `-- step-01-system-architecture.md
```

## 5. Backend Domain Decomposition

### 5.1 `auth`

Responsibility:

- signup
- login
- logout
- token refresh
- password change

Main components:

- controller
- service
- token provider
- refresh token persistence
- auth DTOs

### 5.2 `user`

Responsibility:

- user profile
- account status
- soft delete
- user settings

Main entities:

- `User`
- `UserSettings`

### 5.3 `approval`

Responsibility:

- root review of signup requests
- role upgrade request flow
- approval/rejection history

Main entities:

- `RoleUpgradeRequest`

### 5.4 `course`

Responsibility:

- create and manage courses
- create and manage course sessions
- assign instructor to session
- enroll students in course
- query student calendar

Main entities:

- `Course`
- `CourseSession`
- `Enrollment`
- `SessionWatchStatus`

### 5.5 `sessionresource`

Responsibility:

- Zoom link
- recording link
- summary link
- additional instructor guidance

Main entity:

- `SessionResource`

### 5.6 `note`

Responsibility:

- student notes
- instructor tags
- instructor comments
- note ownership checks

Main entities:

- `Note`
- `NoteTag`
- `NoteComment`

### 5.7 `project`

Responsibility:

- project recruiting posts
- project positions
- student applications
- mentor applications
- PM review of applications

Main entities:

- `ProjectPost`
- `ProjectPosition`
- `ProjectApplication`
- `MentorApplication`

### 5.8 `mentor`

Responsibility:

- mentor-to-student mapping
- mentor view of student progress
- mentoring dashboard

Main entity:

- `MentorStudentMapping`

### 5.9 `notification`

Responsibility:

- in-app notifications
- unread count
- mark one or all as read
- navigation target metadata

Main entity:

- `Notification`

### 5.10 `ai`

Responsibility:

- note summary generation
- career-based lecture recommendation
- similar project recommendation
- request logging and prompt safety

Main entity:

- `AiRequestLog`

### 5.11 `integration`

Responsibility:

- optional Notion connection interface
- secure token handling abstraction
- export summarized note content

Recommended future entity:

- `UserExternalIntegration` or `UserNotionConnection`

This should store an encrypted token or secret reference, not plaintext credentials.

## 6. Frontend Domain Decomposition

### Core application layers

- `app/`: router, providers, theme bootstrap
- `api/`: axios or fetch client, auth interceptors
- `auth/`: auth state, route guard, session bootstrap
- `components/`: reusable UI building blocks
- `layouts/`: app shell, role-specific navigation
- `pages/`: top-level route pages
- `features/`: business features grouped by domain

### Role-based route intent

- Student
  - dashboard
  - my courses
  - class calendar
  - notes
  - projects
  - notifications
  - my page
- Instructor
  - teaching sessions
  - session resource update
  - tagged notes
  - note comments
  - notifications
- Mentor
  - managed students
  - student progress
  - mentor applications
  - notifications
- Root
  - approval queue
  - role upgrade queue
  - course management
  - session management
  - enrollment management
  - platform monitoring

## 7. Core Entity Relationship Summary

### User and approval

- One `User` has one active business role: `STUDENT`, `INSTRUCTOR`, `MENTOR`, or `ROOT`.
- One `User` has one account status: `PENDING`, `APPROVED`, `REJECTED`, `WITHDRAWN`.
- One `User` has one `UserSettings`.
- One `User` may create many `RoleUpgradeRequest`.
- One `ROOT` user reviews many approval actions.

### Courses and sessions

- One `Course` has many `CourseSession`.
- One `Course` has many `Enrollment`.
- One `Enrollment` connects one student to one course.
- One `CourseSession` belongs to one course.
- One `CourseSession` is assigned to one instructor.
- One `CourseSession` has one `SessionResource`.
- One student has many `SessionWatchStatus` rows, one per session.

### Notes

- One student writes many `Note`.
- One `Note` belongs to one course and optionally one course session.
- One `Note` has many `NoteTag`.
- One `NoteTag` targets one instructor.
- One `Note` has many `NoteComment`.
- One instructor writes many `NoteComment`.

### Projects

- One student creates many `ProjectPost`.
- One `ProjectPost` has many `ProjectPosition`.
- One `ProjectPost` has many `ProjectApplication`.
- One `ProjectPost` has many `MentorApplication`.
- One application belongs to one applicant and one position or project.

### Mentoring

- One mentor has many `MentorStudentMapping`.
- One student may be mapped to one or more mentors depending on policy.
- For MVP, recommend one active primary mentor per student unless requirements expand.

### Notifications

- One `User` has many `Notification`.
- Each notification includes `targetType`, `targetId`, `message`, `isRead`, and optional `targetUrl`.

### AI

- One `AiRequestLog` belongs to one requesting user.
- An AI log may reference a note, project post, or recommendation request context.

## 8. Recommended Enum Set

```text
RoleType
- STUDENT
- INSTRUCTOR
- MENTOR
- ROOT

UserStatus
- PENDING
- APPROVED
- REJECTED
- WITHDRAWN

RoleUpgradeRequestStatus
- PENDING
- APPROVED
- REJECTED
- CANCELLED

SessionWatchState
- NOT_STARTED
- IN_PROGRESS
- COMPLETED

ProjectApplicationStatus
- SUBMITTED
- ACCEPTED
- REJECTED
- WITHDRAWN

NotificationType
- APPROVAL_RESULT
- ROLE_UPGRADE_RESULT
- SESSION_ZOOM_UPDATED
- SESSION_RECORDING_UPDATED
- NOTE_TAGGED
- NOTE_COMMENTED
- PROJECT_APPLICATION_RECEIVED
- PROJECT_APPLICATION_RESULT
```

## 9. API Design Conventions

Recommended base path:

- `/api/auth`
- `/api/users`
- `/api/admin/approvals`
- `/api/courses`
- `/api/sessions`
- `/api/notes`
- `/api/projects`
- `/api/mentor`
- `/api/notifications`
- `/api/ai`

Recommended response envelope:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-04-12T23:00:00Z"
}
```

Recommended error envelope:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ACCESS_DENIED",
    "message": "You do not have permission to access this resource.",
    "fieldErrors": []
  },
  "timestamp": "2026-04-12T23:00:00Z"
}
```

## 10. Security Decisions That Affect Later Implementation

### Chosen auth direction

Use:

- access token in frontend memory
- refresh token in HttpOnly cookie
- refresh token rotation and server-side persistence

Why:

- avoids storing long-lived tokens in local storage
- works well with React SPA plus Spring Boot REST APIs
- supports explicit logout and token invalidation

### Access control rules

- `PENDING`, `REJECTED`, and `WITHDRAWN` users cannot access protected business features.
- Role checks must exist both at controller and service ownership layers.
- Student-owned resources must validate author identity server-side.
- Instructor actions must verify ownership of the target session.
- Root-only operations must never rely on frontend hiding alone.

### Soft delete rules

- `WITHDRAWN` accounts remain in DB for integrity and audit reasons.
- Default user lookups exclude withdrawn users.
- Authentication blocks withdrawn accounts.

## 11. Step 1 Scope Boundary

Implemented in this step:

- repository architecture decision
- secure system structure proposal
- domain boundary definition
- core relationship map
- development PostgreSQL compose setup

Deferred to later steps:

- actual Spring Boot code
- actual React code
- DB migrations
- JWT and refresh-token implementation
- AI prompt/service code
- Notion API implementation

## 12. What Comes Next in Step 2

Step 2 will convert this architecture into:

- concrete JPA entity design
- field-level schema definitions
- unique constraints
- indexes
- foreign key rules
- soft delete handling strategy in entities
- ERD-level mapping detail
