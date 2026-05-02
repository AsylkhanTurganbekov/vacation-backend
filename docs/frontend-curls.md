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

## 4.1. Зарегистрировать FCM push token

Используется mobile/web после логина, когда клиент получил device token из Firebase.

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/devices/push-token' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "token": "fcm_device_token_here",
    "platform": "android",
    "deviceId": "android-emulator-5554",
    "deviceName": "Pixel 8 Pro",
    "appVersion": "1.0.0"
  }'
```

Ответ:

```json
{
  "success": true
}
```

Поддерживаемые `platform`:
- `android`
- `ios`

## 4.2. Деактивировать FCM push token

Обычно вызывается на logout или при сбросе приложения.

По токену:

```bash
curl -X DELETE 'http://92.38.49.156:8090/api/v1/devices/push-token' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "token": "fcm_device_token_here"
  }'
```

Или по `deviceId`:

```bash
curl -X DELETE 'http://92.38.49.156:8090/api/v1/devices/push-token' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "deviceId": "android-emulator-5554"
  }'
```

Ответ:

```json
{
  "success": true
}
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

## 13. Командировочное удостоверение

JSON-данные документа:

```bash
curl 'http://92.38.49.156:8090/api/v1/trips/2/certificate' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Printable HTML:

```bash
curl 'http://92.38.49.156:8090/api/v1/trips/2/certificate/html' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

PDF-документ:

```bash
curl 'http://92.38.49.156:8090/api/v1/trips/2/certificate/pdf' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -o trip-certificate-2.pdf
```

Проверка headers:

```bash
curl -I 'http://92.38.49.156:8090/api/v1/trips/2/certificate/pdf' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Ожидаемые headers:

```text
Content-Type: application/pdf
Content-Disposition: inline; filename="trip-certificate-2.pdf"
```

Основные поля:
- `documentNumber`
- `documentDate`
- `employeeFullName`
- `department`
- `position`
- `personnelNumber`
- `destinationAddress`
- `purpose`
- `plannedStartDateTime`
- `plannedEndDateTime`
- `calendarDays`
- `marks`

Отметки `marks` строятся из trip events:
- `DEPARTURE`
- `ARRIVAL`
- `RETURN`

Что использовать на клиентах:
- web preview: `GET /api/v1/trips/{tripId}/certificate/html`
- web/mobile download/print: `GET /api/v1/trips/{tripId}/certificate/pdf`
- если нужен свой UI документа: `GET /api/v1/trips/{tripId}/certificate`

## 14. Employee trips

```bash
curl 'http://92.38.49.156:8090/api/v1/employee/trips?page=0&size=20&sort=plannedStartDateTime,desc' \
  -H 'Authorization: Bearer EMPLOYEE_ACCESS_TOKEN'
```

## 15. Summary для dashboard

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

## 16. Notification center

Получить уведомления текущего пользователя:

```bash
curl 'http://92.38.49.156:8090/api/v1/notifications?page=0&size=20' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Пример ответа:

```json
{
  "content": [
    {
      "id": 1,
      "type": "TRIP_STATUS_CHANGED",
      "title": "Статус командировки обновлён",
      "body": "Командировка #2 переведена в статус в пути",
      "tripId": 2,
      "clickAction": "trip_details",
      "oldStatus": "APPROVED",
      "newStatus": "IN_PROGRESS",
      "read": false,
      "readAt": null,
      "createdAt": "2026-05-02T01:15:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Отметить одно уведомление прочитанным:

```bash
curl -X PATCH 'http://92.38.49.156:8090/api/v1/notifications/1/read' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

Отметить все уведомления прочитанными:

```bash
curl -X PATCH 'http://92.38.49.156:8090/api/v1/notifications/read-all' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## 16.1. Тестовая отправка push на текущего пользователя

Используется для ручной проверки push без смены статуса командировки и без запуска trip business flow.

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/notifications/test-push' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Тестовое уведомление",
    "body": "Проверка push в Triply",
    "tripId": 2
  }'
```

Пример ответа:

```json
{
  "success": true,
  "devicesFound": 2,
  "configured": true,
  "successCount": 2,
  "failureCount": 0
}
```

Логика endpoint:
- берет текущего пользователя из access token
- находит все его активные `pushToken`
- отправляет push на все найденные устройства
- ничего не меняет в trip business logic
- не создает trip status change event
- не требует approve/cancel/departure/arrival/return

## 17. Какой push payload сейчас отправляет backend

При смене статуса командировки backend отправляет FCM `notification` + `data`.

Пример `data` payload:

```json
{
  "type": "trip_status_changed",
  "tripId": "2",
  "oldStatus": "APPROVED",
  "newStatus": "IN_PROGRESS",
  "clickAction": "trip_details"
}
```

Текущая логика получателей:
- сотрудник поездки
- инициатор создания поездки
- без дублей
- пользователь, который сам изменил статус, по умолчанию не уведомляется

## 16. Карта мониторинга

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

## 17. Отчет по trips

```bash
curl 'http://92.38.49.156:8090/api/v1/reports/trips?page=0&size=20&sort=plannedStartDateTime,desc' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## 18. История событий по поездке

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

## 19. Получить фото события

`ADMIN` видит фото всех событий. `EMPLOYEE` видит только фото событий своих поездок.

```bash
curl 'http://92.38.49.156:8090/api/v1/trip-events/10/image' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  --output trip-event-image.jpg
```

## 20. Отметить выезд

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

## 21. Отметить прибытие

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

## 22. Отметить возврат

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

## 23. Swagger

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
