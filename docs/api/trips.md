# Trips API

Основной API командировок. Управление доступно `ADMIN`.

## GET /api/v1/trips

Список командировок с фильтрами.

curl:

```bash
curl -X GET "http://localhost:8081/api/v1/trips?status=APPROVED&employeeId=2&page=0&size=20&sort=plannedStartDateTime,desc" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## GET /api/v1/trips/{id}

Получить командировку.

curl:

```bash
curl -X GET http://localhost:8081/api/v1/trips/1 \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## POST /api/v1/trips

Создать командировку.

Request:

```json
{
  "employeeId": 2,
  "purpose": "Client infrastructure inspection",
  "destinationAddress": "Astana, Mangilik El 55",
  "plannedStartDateTime": "2026-04-20T09:00:00",
  "plannedEndDateTime": "2026-04-22T18:00:00"
}
```

curl:

```bash
curl -X POST http://localhost:8081/api/v1/trips \
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

## PUT /api/v1/trips/{id}

Обновить командировку.

curl:

```bash
curl -X PUT http://localhost:8081/api/v1/trips/1 \
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

## PATCH /api/v1/trips/{id}/approve

Approve командировки из статуса `DRAFT`.

curl:

```bash
curl -X PATCH http://localhost:8081/api/v1/trips/1/approve \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## PATCH /api/v1/trips/{id}/cancel

Отмена командировки.

curl:

```bash
curl -X PATCH http://localhost:8081/api/v1/trips/1/cancel \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## GET /api/v1/employee/trips

Командировки текущего сотрудника.

curl:

```bash
curl -X GET "http://localhost:8081/api/v1/employee/trips?status=APPROVED&page=0&size=20&sort=plannedStartDateTime,desc" \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>"
```

## GET /api/v1/employee/trips/{id}

Одна командировка текущего сотрудника.

curl:

```bash
curl -X GET http://localhost:8081/api/v1/employee/trips/1 \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>"
```
