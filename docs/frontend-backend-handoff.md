# Frontend ↔ Backend Handoff

## Environment and conventions

- Local base URL: `http://localhost:8081` (or the configured `BACKEND_EXTERNAL_PORT` in Docker).
- Swagger: `GET /swagger-ui.html`; OpenAPI: `GET /api-docs`.
- API prefix: `/api/v1`.
- Protected calls send `Authorization: Bearer <accessToken>`.
- Dates use ISO-8601 local date-time, for example `2026-08-28T14:30:00`.
- Lists return `PagedResponse`: `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`.
- Errors return `{ timestamp, status, error, message, path }`.

## Authentication

| Method | Endpoint | Use |
| --- | --- | --- |
| POST | `/auth/login` | Login; returns `accessToken`, `refreshToken`, `tokenType`, `user` |
| POST | `/auth/register` | Public employee registration |
| POST | `/auth/refresh` | Exchange a valid refresh token for a new token pair |
| POST | `/auth/logout` | Revoke a refresh token |
| GET | `/auth/me` | Current user and a refreshed access token |

`POST /auth/register` always creates an `EMPLOYEE`. Do not show a role selector in public registration: an incoming `role=ADMIN` is ignored for security. Creating administrators and changing roles use the ADMIN-only `/users` API.

Store the access token in memory where possible. On `401`, call `/auth/refresh` once; if it fails, clear session and send the user to login. The backend does not set cookies.

## Roles and access

| Capability | ADMIN | EMPLOYEE |
| --- | --- | --- |
| Users, reports, all trips | Yes | No |
| Own trip list/detail/events | Yes | Yes |
| Another employee’s event/photo/certificate/biometric data | Yes | No (`403`) |
| Devices and own notifications | Yes | Yes |
| User avatar upload/delete | Any user | Only own avatar |

Handle `401` as unauthenticated/expired token, `403` as forbidden, `400` as a form/business-rule error, `404` as missing resource, and `409` as a concurrent update: reload the affected trip and let the user retry.

## Users (ADMIN)

- `GET /users?q=&role=&department=&active=&page=&size=&sort=`
- `GET /users/{id}`
- `GET /users/{id}/stats`
- `POST /users`, `PUT /users/{id}`, `PATCH /users/{id}/active`
- `GET /users/{id}/avatar` is public for rendering avatar URLs.
- `POST /users/{id}/avatar` multipart field `file`; `DELETE /users/{id}/avatar`.

User fields include `id`, `fullName`, `email`, `iin`, `role`, `department`, `position`, `active`, `avatarUrl`, `createdAt`, `updatedAt`. IIN is optional but must be unique and exactly 12 digits if supplied. Avatar files must be JPEG, PNG or WebP and at most 5 MB.

## Trips

### Admin management

- `GET /trips?q=&status=&employeeId=&department=&dateFrom=&dateTo=&page=&size=&sort=`
- `POST /trips`
- `GET /trips/{id}`, `PUT /trips/{id}`
- `PATCH /trips/{id}/approve`, `PATCH /trips/{id}/cancel`

Trip fields: `id`, `employeeId`, `employeeName`, `purpose`, `destinationAddress`, `externalTripId`, planned and actual dates, `status`, `createdAt`, `updatedAt`.

Statuses: `DRAFT`, `APPROVED`, `IN_PROGRESS`, `ARRIVED`, `COMPLETED`, `CANCELLED`.

### Employee view

- `GET /employee/trips` supports the same filters except `employeeId`.
- `GET /employee/trips/{id}`.

Date filtering is range intersection, not just start-date filtering. A trip spanning the selected period is returned.

## Trip events, location and photos

- `POST /trips/{tripId}/events/departure`
- `POST /trips/{tripId}/events/arrival`
- `POST /trips/{tripId}/events/return`
- `GET /trips/{tripId}/events`
- `GET /trip-events/{eventId}/image`

Event request:

```json
{
  "latitude": 43.238949,
  "longitude": 76.889709,
  "address": "Almaty office",
  "eventTime": "2026-08-28T10:00:00",
  "comment": "Leaving",
  "imageBase64": "data:image/jpeg;base64,..."
}
```

`imageBase64` or `imageUrl` is required. If sending base64, use JPEG/PNG/WebP, decoded size ≤ 5 MB. Event order is strict: departure only from `APPROVED`; arrival after departure; return after arrival. Event time cannot be before the preceding event. A duplicate/concurrent request is rejected safely; on `409`, reload the trip.

## Documents, reporting and monitoring

- Certificate: `GET /trips/{tripId}/certificate`, `/certificate/html`, `/certificate/pdf`.
- Reports (ADMIN): `GET /reports/trips`, `/reports/trips/summary`, `/reports/employees/{employeeId}/trips`.
- Monitoring: `GET /monitoring/map` provides `withCoordinates` and `withoutCoordinates`; render the latest event location, current address and employee avatar from this dedicated response.

## Notifications and devices

- `POST /devices/push-token`, `DELETE /devices/push-token`.
- `GET /notifications?page=&size=`.
- `PATCH /notifications/{id}/read`, `PATCH /notifications/read-all`.
- `POST /notifications/test-push` for authenticated mobile/frontend diagnostics.

Register the FCM token after login and deactivate it on logout or reset. Delivery is asynchronous and may be unavailable when Firebase credentials are not configured; treat in-app notification records as the authoritative UI source.

## Biometric and Bitrix integration

- `POST /biometric/verify`, `GET /biometric/verifications/{id}`: access is limited to the employee’s own records (or admin).
- `POST /integrations/bitrix/trips`, `GET /integrations/bitrix/trips/{externalTripId}` are service-to-service routes, not frontend routes. They require `X-API-Key`; missing configuration returns `503`, missing/invalid key returns `401`.

The mock biometric provider is used only in dev/test. Do not display a successful mock response as a production-grade identity guarantee.

## Frontend implementation checklist

1. Centralize bearer token attachment and one-time refresh flow.
2. Use role-based route guards, but preserve backend error handling as the source of truth.
3. Implement user-facing messages for `400/401/403/404/409`.
4. Use `imageUrl` from response for display; never reconstruct a storage path client-side.
5. Do not expose Bitrix/ML/Firebase/JWT secrets in browser code.
6. Regenerate typed client models from `/api-docs` when the backend version changes.
