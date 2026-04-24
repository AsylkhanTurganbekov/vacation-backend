# Reports API

Доступно только для `ADMIN`.

## GET /api/v1/reports/trips

Отчет по всем командировкам с фильтрами.

curl:

```bash
curl -X GET "http://localhost:8081/api/v1/reports/trips?status=COMPLETED&employeeId=2&dateFrom=2026-04-01T00:00:00&dateTo=2026-04-30T23:59:59&page=0&size=20&sort=plannedStartDateTime,desc" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## GET /api/v1/reports/trips/summary

Сводный отчет.

curl:

```bash
curl -X GET http://localhost:8081/api/v1/reports/trips/summary \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

Пример ответа:

```json
{
  "totalTrips": 12,
  "tripsByStatus": {
    "DRAFT": 1,
    "APPROVED": 2,
    "IN_PROGRESS": 1,
    "ARRIVED": 1,
    "COMPLETED": 6,
    "CANCELLED": 1
  },
  "verifiedEvents": 18,
  "failedEvents": 1,
  "pendingEvents": 0
}
```

## GET /api/v1/reports/employees/{employeeId}/trips

Отчет по командировкам одного сотрудника.

curl:

```bash
curl -X GET "http://localhost:8081/api/v1/reports/employees/2/trips?status=COMPLETED&page=0&size=20&sort=plannedStartDateTime,desc" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```
