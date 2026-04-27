# Frontend / Mobile CURL Examples

Базовый URL:

```text
http://92.38.49.156:8090
```

## 1. Login

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "admin@vacation.local",
    "password": "Admin123!"
  }'
```

Ответ:

```json
{
  "accessToken": "ACCESS_TOKEN",
  "refreshToken": "REFRESH_TOKEN",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "fullName": "System Administrator",
    "email": "admin@vacation.local",
    "role": "ADMIN"
  }
}
```

## 2. Me

```bash
curl 'http://92.38.49.156:8090/api/v1/auth/me' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## 3. Refresh token

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/auth/refresh' \
  -H 'Content-Type: application/json' \
  -d '{
    "refreshToken": "REFRESH_TOKEN"
  }'
```

Ответ:

```json
{
  "accessToken": "NEW_ACCESS_TOKEN",
  "refreshToken": "NEW_REFRESH_TOKEN",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "fullName": "System Administrator"
  }
}
```

## 4. Logout

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/auth/logout' \
  -H 'Content-Type: application/json' \
  -d '{
    "refreshToken": "REFRESH_TOKEN"
  }' \
  -i
```

Ожидаемый статус:

```text
204 No Content
```

## 5. Список сотрудников для dropdown

```bash
curl 'http://92.38.49.156:8090/api/v1/users?role=EMPLOYEE&active=true&size=100&sort=id,asc' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## 6. Поиск сотрудников

```bash
curl 'http://92.38.49.156:8090/api/v1/users?q=demo&role=EMPLOYEE&active=true&size=100' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## 7. Получить аватар пользователя

```bash
curl 'http://92.38.49.156:8090/api/v1/users/2/avatar' --output employee-avatar.jpg
```

## 8. Загрузить аватар пользователя

Для `EMPLOYEE` разрешена загрузка только своего аватара. `ADMIN` может менять аватар любого пользователя.

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/users/2/avatar' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -F 'file=@/absolute/path/to/avatar.jpg'
```

В ответе `UserResponse` вернется:

```json
{
  "id": 2,
  "fullName": "Demo Employee",
  "avatarUrl": "/api/v1/users/2/avatar"
}
```

## 9. Удалить аватар пользователя

```bash
curl -X DELETE 'http://92.38.49.156:8090/api/v1/users/2/avatar' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -i
```

Ожидаемый статус:

```text
204 No Content
```

## 10. Список командировок

```bash
curl 'http://92.38.49.156:8090/api/v1/trips?page=0&size=20&sort=plannedStartDateTime,desc' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## 11. Фильтрация trips под dashboard

Поддерживаемые query params:
- `q`
- `employeeId`
- `employeeIds`
- `department`
- `status`
- `dateFrom`
- `dateTo`
- `page`
- `size`
- `sort`

Пример:

```bash
curl 'http://92.38.49.156:8090/api/v1/trips?q=astana&employeeId=2&department=Field%20Service&status=APPROVED&dateFrom=2026-04-01T00:00:00&dateTo=2026-04-30T23:59:59&page=0&size=20&sort=plannedStartDateTime,desc' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## 12. Одна командировка

```bash
curl 'http://92.38.49.156:8090/api/v1/trips/2' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## 13. Employee trips

```bash
curl 'http://92.38.49.156:8090/api/v1/employee/trips?page=0&size=20&sort=plannedStartDateTime,desc' \
  -H 'Authorization: Bearer EMPLOYEE_ACCESS_TOKEN'
```

## 14. Summary для dashboard

```bash
curl 'http://92.38.49.156:8090/api/v1/reports/trips/summary' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Ответ:

```json
{
  "totalTrips": 2,
  "tripsByStatus": {
    "DRAFT": 0,
    "APPROVED": 1,
    "IN_PROGRESS": 0,
    "ARRIVED": 0,
    "COMPLETED": 1,
    "CANCELLED": 0
  },
  "verifiedEvents": 3,
  "failedEvents": 0,
  "pendingEvents": 0
}
```

## 15. Карта мониторинга

Отдельный endpoint под карту:
- `GET /api/v1/monitoring/map`
- не перегружает обычный `GET /api/v1/trips`
- возвращает `withCoordinates` и `withoutCoordinates`
- `EMPLOYEE` автоматически видит только свои поездки

Поддерживаемые query params:
- `q`
- `employeeId`
- `department`
- `status`
- `dateFrom`
- `dateTo`

Пример:

```bash
curl 'http://92.38.49.156:8090/api/v1/monitoring/map?q=astana&employeeId=2&department=Field%20Service&status=COMPLETED&dateFrom=2026-04-01T00:00:00&dateTo=2026-04-30T23:59:59' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Фильтр по нескольким сотрудникам:

```bash
curl 'http://92.38.49.156:8090/api/v1/monitoring/map?employeeIds=2&employeeIds=5&employeeIds=8' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Пример ответа:

```json
{
  "withCoordinates": [
    {
      "tripId": 2,
      "employeeId": 2,
      "employeeName": "Demo Employee",
      "employeeAvatarUrl": "/api/v1/users/2/avatar",
      "department": "Field Service",
      "purpose": "Client infrastructure inspection",
      "destinationAddress": "Astana, Mangilik El 55",
      "currentAddress": "Astana, Mangilik El 55",
      "status": "COMPLETED",
      "lastEventType": "RETURN",
      "lastEventTime": "2026-04-26T17:40:00",
      "coordinates": [71.430420, 51.128207]
    }
  ],
  "withoutCoordinates": []
}
```

## 16. Отчет по trips

```bash
curl 'http://92.38.49.156:8090/api/v1/reports/trips?page=0&size=20&sort=plannedStartDateTime,desc' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## 17. История событий по поездке

```bash
curl 'http://92.38.49.156:8090/api/v1/trips/2/events' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Теперь каждый event может возвращать:

```json
{
  "id": 10,
  "tripId": 2,
  "type": "DEPARTURE",
  "imageUrl": "/api/v1/trip-events/10/image"
}
```

## 18. Получить фото события

`ADMIN` видит фото всех событий. `EMPLOYEE` видит только фото событий своих поездок.

```bash
curl 'http://92.38.49.156:8090/api/v1/trip-events/10/image' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  --output trip-event-image.jpg
```

## 19. Отметить выезд

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/trips/2/events/departure' \
  -H 'Authorization: Bearer EMPLOYEE_ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "latitude": 51.128207,
    "longitude": 71.430420,
    "address": "Astana, Mangilik El 55",
    "eventTime": "2026-04-25T09:10:00",
    "comment": "Departure from office",
    "imageBase64": "ZmFrZS1pbWFnZQ=="
  }'
```

`eventTime` должен быть в прошлом или настоящем. `imageBase64` должен быть не длиннее `1000000` символов.

## 20. Отметить прибытие

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/trips/2/events/arrival' \
  -H 'Authorization: Bearer EMPLOYEE_ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "latitude": 51.130500,
    "longitude": 71.428900,
    "address": "Astana, Client site",
    "eventTime": "2026-04-25T10:05:00",
    "comment": "Arrival at destination",
    "imageBase64": "ZmFrZS1pbWFnZQ=="
  }'
```

## 21. Отметить возврат

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/trips/2/events/return' \
  -H 'Authorization: Bearer EMPLOYEE_ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "latitude": 51.128207,
    "longitude": 71.430420,
    "address": "Astana, Mangilik El 55",
    "eventTime": "2026-04-26T17:40:00",
    "comment": "Return to office",
    "imageBase64": "ZmFrZS1pbWFnZQ=="
  }'
```

## 22. Swagger

```text
http://92.38.49.156:8090/swagger-ui.html
```

## Демо-учетки

Admin:

```text
admin@vacation.local / Admin123!
```

Employee:

```text
employee@vacation.local / Employee123!
```
