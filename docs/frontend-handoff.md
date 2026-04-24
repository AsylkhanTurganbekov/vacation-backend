# Frontend Handoff

Актуально для текущего backend на сервере:

- Base URL: `http://92.38.49.156:8090`
- Swagger: [http://92.38.49.156:8090/swagger-ui.html](http://92.38.49.156:8090/swagger-ui.html)

## Demo Users

- `admin@vacation.local / Admin123!`
- `employee@vacation.local / Employee123!`

## Roles

- `ADMIN`
- `EMPLOYEE`

## Main Enums

### `BusinessTripStatus`

```text
DRAFT
APPROVED
IN_PROGRESS
ARRIVED
COMPLETED
CANCELLED
```

### `TripEventType`

```text
DEPARTURE
ARRIVAL
RETURN
```

### `VerificationStatus`

```text
PENDING
VERIFIED
FAILED
```

## Frontend Integration Notes

- Все защищенные endpoints требуют header:
  - `Authorization: Bearer <TOKEN>`
- После логина фронт должен сохранить `accessToken`
- Для первичной интеграции CORS сейчас открыт достаточно широко
- Swagger доступен и можно смотреть точные схемы ответов там

## Quick Flow

1. Логин под `ADMIN`
2. Получить `me`
3. Получить список пользователей или создать сотрудника
4. Создать командировку
5. Approve командировку
6. Логин под `EMPLOYEE`
7. Получить свои командировки
8. Отправить `departure`
9. Отправить `arrival`
10. Отправить `return`
11. Получить события и отчеты

## Auth

### Login

```bash
curl -X POST http://92.38.49.156:8090/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@vacation.local",
    "password": "Admin123!"
  }'
```

### Register

```bash
curl -X POST http://92.38.49.156:8090/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "New Employee",
    "email": "new.employee@vacation.local",
    "password": "Password123!",
    "role": "EMPLOYEE",
    "department": "Field Service",
    "position": "Inspector"
  }'
```

### Me

```bash
curl -X GET http://92.38.49.156:8090/api/v1/auth/me \
  -H "Authorization: Bearer <TOKEN>"
```

## Users

### Get Users

```bash
curl -X GET "http://92.38.49.156:8090/api/v1/users?page=0&size=20&sort=id,asc" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Get User By Id

```bash
curl -X GET http://92.38.49.156:8090/api/v1/users/2 \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Create User

```bash
curl -X POST http://92.38.49.156:8090/api/v1/users \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Employee One",
    "email": "employee.one@vacation.local",
    "password": "Password123!",
    "role": "EMPLOYEE",
    "department": "Sales",
    "position": "Regional Manager",
    "active": true
  }'
```

### Update User

```bash
curl -X PUT http://92.38.49.156:8090/api/v1/users/2 \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Demo Employee Updated",
    "email": "employee@vacation.local",
    "password": "Employee123!",
    "role": "EMPLOYEE",
    "department": "Field Service",
    "position": "Senior Mobile Engineer",
    "active": true
  }'
```

### Change Active Flag

```bash
curl -X PATCH http://92.38.49.156:8090/api/v1/users/2/active \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "active": false
  }'
```

## Trips

### Get Trips

```bash
curl -X GET "http://92.38.49.156:8090/api/v1/trips?status=APPROVED&employeeId=2&page=0&size=20&sort=plannedStartDateTime,desc" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Get Trip By Id

```bash
curl -X GET http://92.38.49.156:8090/api/v1/trips/1 \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Create Trip

```bash
curl -X POST http://92.38.49.156:8090/api/v1/trips \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 2,
    "purpose": "Client infrastructure inspection",
    "destinationAddress": "Astana, Mangilik El 55",
    "plannedStartDateTime": "2026-04-20T09:00:00",
    "plannedEndDateTime": "2026-04-22T18:00:00"
  }'
```

### Update Trip

```bash
curl -X PUT http://92.38.49.156:8090/api/v1/trips/1 \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 2,
    "purpose": "Updated trip purpose",
    "destinationAddress": "Almaty, Abay 10",
    "plannedStartDateTime": "2026-04-20T09:00:00",
    "plannedEndDateTime": "2026-04-23T18:00:00"
  }'
```

### Approve Trip

```bash
curl -X PATCH http://92.38.49.156:8090/api/v1/trips/1/approve \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Cancel Trip

```bash
curl -X PATCH http://92.38.49.156:8090/api/v1/trips/1/cancel \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## Employee Trips

### Get Current Employee Trips

```bash
curl -X GET "http://92.38.49.156:8090/api/v1/employee/trips?status=APPROVED&page=0&size=20&sort=plannedStartDateTime,desc" \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>"
```

### Get Current Employee Trip By Id

```bash
curl -X GET http://92.38.49.156:8090/api/v1/employee/trips/1 \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>"
```

## Trip Events

### Departure

```bash
curl -X POST http://92.38.49.156:8090/api/v1/trips/1/events/departure \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 43.238949,
    "longitude": 76.889709,
    "address": "Almaty office",
    "eventTime": "2026-04-16T10:00:00",
    "comment": "Leaving office",
    "imageBase64": "ZmFrZS1pbWFnZQ=="
  }'
```

### Arrival

```bash
curl -X POST http://92.38.49.156:8090/api/v1/trips/1/events/arrival \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 51.128207,
    "longitude": 71.430420,
    "address": "Client site",
    "eventTime": "2026-04-16T18:00:00",
    "comment": "Arrived at destination",
    "imageBase64": "ZmFrZS1pbWFnZQ=="
  }'
```

### Return

```bash
curl -X POST http://92.38.49.156:8090/api/v1/trips/1/events/return \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 43.238949,
    "longitude": 76.889709,
    "address": "Back to office",
    "eventTime": "2026-04-18T18:00:00",
    "comment": "Returned from trip",
    "imageBase64": "ZmFrZS1pbWFnZQ=="
  }'
```

### Get Trip Events

```bash
curl -X GET http://92.38.49.156:8090/api/v1/trips/1/events \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>"
```

### Example Event Response

```json
{
  "id": 10,
  "tripId": 1,
  "type": "DEPARTURE",
  "latitude": 43.2389490,
  "longitude": 76.8897090,
  "address": "Almaty office",
  "eventTime": "2026-04-16T10:00:00",
  "verificationStatus": "VERIFIED",
  "comment": "Leaving office",
  "createdAt": "2026-04-16T10:00:02"
}
```

## Biometric

### Manual Verify

```bash
curl -X POST http://92.38.49.156:8090/api/v1/biometric/verify \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 2,
    "tripEventId": 10,
    "imageBase64": "ZmFrZS1pbWFnZQ==",
    "imageUrl": "https://storage.local/images/10.jpg"
  }'
```

### Get Verification By Id

```bash
curl -X GET http://92.38.49.156:8090/api/v1/biometric/verifications/1 \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>"
```

## Reports

### Get Trips Report

```bash
curl -X GET "http://92.38.49.156:8090/api/v1/reports/trips?status=COMPLETED&employeeId=2&dateFrom=2026-04-01T00:00:00&dateTo=2026-04-30T23:59:59&page=0&size=20&sort=plannedStartDateTime,desc" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Get Summary

```bash
curl -X GET http://92.38.49.156:8090/api/v1/reports/trips/summary \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

### Get Employee Trips Report

```bash
curl -X GET "http://92.38.49.156:8090/api/v1/reports/employees/2/trips?status=COMPLETED&page=0&size=20&sort=plannedStartDateTime,desc" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## Recommended Frontend Env

Примеры:

```env
VITE_API_URL=http://92.38.49.156:8090
```

или

```env
NEXT_PUBLIC_API_URL=http://92.38.49.156:8090
```

## Useful Links

- Main API index: [docs/api/README.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/README.md)
- Auth docs: [docs/api/auth.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/auth.md)
- Trips docs: [docs/api/trips.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/trips.md)
- Trip events docs: [docs/api/trip-events.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/trip-events.md)
- Users docs: [docs/api/users.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/users.md)
- Biometric docs: [docs/api/biometric.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/biometric.md)
- Reports docs: [docs/api/reports.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/reports.md)
