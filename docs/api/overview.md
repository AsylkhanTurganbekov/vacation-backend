# Overview

## Base URL

```text
http://localhost:8081
```

## Swagger

```text
http://localhost:8081/swagger-ui.html
```

## Auth Header

Все защищенные endpoint'ы требуют заголовок:

```text
Authorization: Bearer <JWT_TOKEN>
```

## Роли

- `ADMIN` управляет пользователями, всеми командировками, отчетами
- `EMPLOYEE` видит только свои командировки и отправляет события только по своим командировкам

## Query Params

Поддерживаются в нужных endpoint'ах:

- `status`
- `employeeId`
- `dateFrom`
- `dateTo`
- `page`
- `size`
- `sort`

Пример:

```text
GET /api/v1/trips?status=APPROVED&employeeId=2&page=0&size=10&sort=plannedStartDateTime,desc
```

## Формат пагинации

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

## Формат ошибки

```json
{
  "timestamp": "2026-04-16T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "ARRIVAL requires existing DEPARTURE",
  "path": "/api/v1/trips/1/events/arrival"
}
```

## Демо-учетки

- `admin@vacation.local` / `Admin123!`
- `employee@vacation.local` / `Employee123!`
