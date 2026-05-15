# Bitrix -> Triply: интеграция командировок

Этот документ предназначен для команды Bitrix.

Назначение:
- после согласования командировки в Bitrix отправить ее в Triply;
- если командировка уже была отправлена раньше, обновить ее по `externalTripId`;
- получить командировку из Triply по внешнему идентификатору.

## Важно для командировочного удостоверения

Triply формирует командировочное удостоверение по данным пользователя и командировки.

Для корректного формирования удостоверения должны быть заполнены:
- `iin` сотрудника в профиле Triply;
- `plannedStartDateTime` командировки;
- `plannedEndDateTime` командировки.

Что это значит для Bitrix:
- при вызове integration API Bitrix обязан передавать:
  - `plannedStartDateTime`
  - `plannedEndDateTime`
- `iin` не передается в текущем Bitrix trip endpoint и должен уже существовать в профиле сотрудника в Triply

Если у сотрудника в Triply не заполнен `iin`, удостоверение будет сформировано без ИИН.

## Базовый URL

```text
http://92.38.49.156:8090
```

## Аутентификация

Во все запросы нужно передавать header:

```text
X-API-Key: <BITRIX_API_KEY>
```

Ключ выдается отдельно и не должен храниться в клиентском коде.

## 1. Создать или обновить командировку

### Endpoint

```http
POST /api/v1/integrations/bitrix/trips
```

### Логика

- если `externalTripId` новый, в Triply создается новая командировка;
- если `externalTripId` уже существует, командировка обновляется;
- так как Bitrix вызывает этот endpoint **после согласования**, новая командировка в Triply создается сразу в статусе:
  - `APPROVED`

### Request body

```json
{
  "externalTripId": "BX-TRIP-10025",
  "employeeId": 2,
  "purpose": "Client infrastructure inspection",
  "destinationAddress": "Astana, Mangilik El 55",
  "plannedStartDateTime": "2026-05-20T09:00:00",
  "plannedEndDateTime": "2026-05-22T18:00:00"
}
```

### Поля

- `externalTripId` — обязательный внешний идентификатор командировки в Bitrix, строка, максимум 100 символов
- `employeeId` — обязательный id сотрудника в Triply
- `purpose` — обязательная цель командировки, максимум 500 символов
- `destinationAddress` — обязательный адрес назначения, максимум 500 символов
- `plannedStartDateTime` — обязательная дата/время начала, формат ISO-8601; используется и для календаря, и для командировочного удостоверения
- `plannedEndDateTime` — обязательная дата/время завершения, формат ISO-8601; используется и для календаря, и для командировочного удостоверения

### Важные правила

- `plannedEndDateTime` должен быть позже `plannedStartDateTime`
- обновление запрещено для командировок в статусах:
  - `COMPLETED`
  - `CANCELLED`

### Пример curl

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/integrations/bitrix/trips' \
  -H 'X-API-Key: BITRIX_API_KEY' \
  -H 'Content-Type: application/json' \
  -d '{
    "externalTripId": "BX-TRIP-10025",
    "employeeId": 2,
    "purpose": "Client infrastructure inspection",
    "destinationAddress": "Astana, Mangilik El 55",
    "plannedStartDateTime": "2026-05-20T09:00:00",
    "plannedEndDateTime": "2026-05-22T18:00:00"
  }'
```

### Пример успешного ответа

```json
{
  "id": 25,
  "externalTripId": "BX-TRIP-10025",
  "employeeId": 2,
  "employeeName": "Demo Employee",
  "employeeAvatarUrl": "/api/v1/users/2/avatar",
  "purpose": "Client infrastructure inspection",
  "destinationAddress": "Astana, Mangilik El 55",
  "plannedStartDateTime": "2026-05-20T09:00:00",
  "plannedEndDateTime": "2026-05-22T18:00:00",
  "actualStartDateTime": null,
  "actualArrivalDateTime": null,
  "actualReturnDateTime": null,
  "status": "APPROVED",
  "createdAt": "2026-05-15T12:00:00",
  "updatedAt": "2026-05-15T12:00:00"
}
```

## 2. Получить командировку по внешнему id

### Endpoint

```http
GET /api/v1/integrations/bitrix/trips/{externalTripId}
```

### Пример curl

```bash
curl 'http://92.38.49.156:8090/api/v1/integrations/bitrix/trips/BX-TRIP-10025' \
  -H 'X-API-Key: BITRIX_API_KEY'
```

### Пример ответа

```json
{
  "id": 25,
  "externalTripId": "BX-TRIP-10025",
  "employeeId": 2,
  "employeeName": "Demo Employee",
  "employeeAvatarUrl": "/api/v1/users/2/avatar",
  "purpose": "Client infrastructure inspection",
  "destinationAddress": "Astana, Mangilik El 55",
  "plannedStartDateTime": "2026-05-20T09:00:00",
  "plannedEndDateTime": "2026-05-22T18:00:00",
  "actualStartDateTime": null,
  "actualArrivalDateTime": null,
  "actualReturnDateTime": null,
  "status": "APPROVED",
  "createdAt": "2026-05-15T12:00:00",
  "updatedAt": "2026-05-15T12:00:00"
}
```

## 3. Возможные ошибки

### Нет API key

Статус:

```text
401 Unauthorized
```

Пример:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing X-API-Key header"
}
```

### Неверный API key

Статус:

```text
401 Unauthorized
```

### Сотрудник не найден

Статус:

```text
404 Not Found
```

Пример:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Employee not found with id 999"
}
```

### Некорректные даты

Статус:

```text
400 Bad Request
```

Пример:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "plannedEndDateTime must be after plannedStartDateTime"
}
```

### Нельзя обновлять завершенную/отмененную командировку

Статус:

```text
400 Bad Request
```

Пример:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Completed or cancelled trip cannot be updated via Bitrix integration"
}
```

## 4. Идемпотентность

Для синхронизации между Bitrix и Triply используйте `externalTripId`.

Ожидаемая модель работы:
- Bitrix хранит свой постоянный id командировки;
- при повторной отправке той же командировки используется тот же `externalTripId`;
- Triply по нему находит существующую запись и обновляет ее.

## 5. Рекомендуемый сценарий вызова

Рекомендуемый flow:
1. командировка согласована в Bitrix;
2. Bitrix вызывает `POST /api/v1/integrations/bitrix/trips`;
3. Bitrix сохраняет ответ Triply:
   - `id`
   - `externalTripId`
   - `status`
4. при изменении данных до старта поездки Bitrix повторно вызывает тот же `POST` с тем же `externalTripId`.

## 6. Что нужно проверить перед запуском

- в Triply у сотрудника заполнен `iin`
- Bitrix передает корректные:
  - `plannedStartDateTime`
  - `plannedEndDateTime`
- `employeeId` в запросе соответствует реальному пользователю в Triply
