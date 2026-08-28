# Backend Completion Plan

## Scope and assumptions

This document describes the current Git repository (`vacation/vacation-backend`), not the legacy local copy without `.git`.

Safe product assumptions used for this work:

- self-registration is an employee onboarding convenience, never an admin provisioning mechanism;
- privileged users are created and managed through the ADMIN-only users API;
- a missing Bitrix API key must fail closed (the integration is unavailable), rather than accept requests;
- mock biometric verification is development-only behaviour and is not evidence of identity verification;
- an employee receives `403` when attempting to access another employee's protected resources. Public avatars remain intentionally public because their URL is exposed in the existing frontend contract.

## Current implemented modules

| Area | Delivered behaviour |
| --- | --- |
| Identity | JWT access tokens, refresh-token rotation/revocation, logout, BCrypt passwords, ADMIN/EMPLOYEE authorization |
| Users | Admin CRUD, filters, active flag, IIN validation, avatar storage, user stats |
| Trips | Admin CRUD, filters, status workflow, employee-specific views, audit entries |
| Events | departure/arrival/return, coordinates, photo storage, trip status updates, biometric verification record |
| Admin/dashboard | reports, summary, monitoring map, notification centre, FCM device tokens and asynchronous status notifications |
| Documents | trip certificate JSON, HTML and PDF |
| Integration | Bitrix idempotent upsert by `externalTripId` |
| Platform | Spring Boot, PostgreSQL, Liquibase, Docker Compose, Nginx example, GitHub Actions build/deploy |

## Completed in this work

- verified the actual repo root and ran `git pull --ff-only` (already at `63003ac`); existing user changes were preserved;
- public registration now always creates an `EMPLOYEE`, even if the request contains `ADMIN`;
- production configuration now requires `APP_JWT_SECRET`; the development-only fallback resides in the `dev` profile;
- Bitrix compares API keys in constant time and returns a structured `503` when not configured and `401` for missing/invalid keys;
- employee access denials for trips, certificates, event photos, biometric records and avatar mutation use `403`;
- response handling now consistently returns the API error envelope for malformed JSON, `ResponseStatusException`, authentication/authorization errors and optimistic-lock conflicts;
- added a `version` column through Liquibase migration `013` and pessimistic locking for event-driven transitions; event timestamps cannot precede the immediately previous transition;
- hardened image uploads: 5 MB request/file limit, JPEG/PNG/WebP signature validation, UUID file names and server-controlled extensions;
- enabled health probes and restricted production health details;
- added regression tests for self-registration privilege escalation and Bitrix key failure modes.

## Remaining work

### P0 — required before a public production launch

- Replace the static mock biometric provider in the production profile with the adapter described below; fail closed or mark verification as `PENDING` when the external service is unavailable.
- Run database migration and an end-to-end PostgreSQL smoke test in the target deployment environment. H2 tests do not validate every PostgreSQL/Liquibase edge case.
- Configure non-default `APP_CORS_ALLOWED_ORIGIN_PATTERNS`, `APP_JWT_SECRET`, database passwords and Bitrix secret in the deployment secret store. Do not use sample `.env` values.
- Add integration tests for every protected endpoint and CI secret/configuration validation before enabling automatic deployment to an Internet-facing environment.

### P1 — production quality and admin operations

- Add API for querying audit trail with pagination and filters; include status-change history as a first-class admin view.
- Guard admin operations: prevent disabling/demoting the final active ADMIN and prevent an admin from accidentally locking out the only privileged account.
- Restrict editing an approved/in-progress trip to explicitly allowed fields, or require cancellation/re-approval; document the chosen approval policy.
- Add downloadable report export (CSV/XLSX) and dashboard endpoints for overdue trips, failed verification, missing events and plan-vs-actual variance.
- Add DB indexes backed by production query plans for dashboard/report filters; apply pagination limits and allowlisted sort fields.
- Track notification delivery lifecycle (`queued`, `sent`, `failed`) and retry policy; current FCM result is best-effort.
- Add API integration tests using PostgreSQL/Testcontainers and tests for auth, refresh/logout, permissions, events, notifications, Bitrix, document/photo access and Liquibase migration.

### P2 — scalability and maintainability

- Object storage adapter (S3-compatible) with virus scanning, retention policy, signed URLs and background deletion; current local volume storage is appropriate only for a single-node deployment.
- Rate limiting for login, registration and Bitrix endpoints; security audit events and alerting.
- Metrics/tracing/log correlation, database backup/restore runbook, SLOs and readiness checks for dependencies.
- Dedicated department catalogue, richer workflow roles/permissions (approver/manager), and explicit anomaly/monitoring feeds.

## API and migration compatibility

- No endpoint or response field has been removed.
- `POST /api/v1/auth/register` still accepts the existing body, but its `role` input is deliberately ignored and the created account is always `EMPLOYEE`. Frontends must not present role selection for public registration.
- Access control failures that previously surfaced as `400` now correctly produce `403` using the existing `ApiErrorResponse` structure.
- Migration `013-add-business-trip-version` adds a non-null `business_trips.version bigint default 0` column. It is additive and safe for rolling deploys; deploy migration before or with the application image.
- Base64 event images now must be JPEG, PNG or WebP and no larger than 5 MB decoded. This is a security restriction, not a schema change.

## ML/biometric integration contract

`BiometricProvider` is the extension seam. The current `MockBiometricProvider` returns verified for a non-empty payload and must be active only in development/test.

Production adapter contract:

```http
POST {ML_BASE_URL}/v1/face-verifications
Authorization: Bearer {ML_API_KEY}
Content-Type: application/json

{"employeeId": 42, "imageBase64": "...", "correlationId": "uuid"}
```

Expected response:

```json
{"verified": true, "score": 0.95, "provider": "face-service", "referenceVersion": "2026-01"}
```

Operational policy: connect timeout 2 seconds, response timeout 8 seconds, at most one retry only for idempotent network/5xx failures, no retry for 4xx, no biometric payloads in logs/audit records. Persist a `PENDING`/failure reason for unavailable ML and permit a controlled re-verification workflow. Secrets are supplied exclusively by environment/secret manager.

## Rollout and verification

1. Populate production secrets and allowed CORS origins; validate `APP_JWT_SECRET` is at least 32 random bytes.
2. Take a database backup; run Liquibase migration `013` against a staging PostgreSQL clone.
3. Deploy application without changing external API routes; verify `/actuator/health/liveness` and `/actuator/health/readiness`.
4. Smoke test: login, employee self-registration, admin user provisioning, trip lifecycle, cross-user `403`, Bitrix valid/invalid/missing key, avatar/event image validation, notification registration and certificate PDF.
5. Monitor API error rate, optimistic-lock conflicts, failed FCM deliveries, storage capacity and PostgreSQL connections; retain rollback image. The additive version column does not block rollback to the prior application image.

## Definition of done

Backend is production-ready when all P0 items are complete, CI runs compile + tests + package + image security/config checks, PostgreSQL/Liquibase integration tests pass, secrets are supplied by deployment infrastructure, protected-resource scenarios have automated coverage, and monitoring/backup/incident ownership is documented.
