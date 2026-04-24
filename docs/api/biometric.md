# Biometric API

В проекте есть абстракция провайдера и mock implementation.

Поведение mock:

- если `imageBase64` пустой, verification будет `FAILED`
- если `imageBase64` заполнен, verification будет `VERIFIED`
- score по умолчанию `0.95`

## POST /api/v1/biometric/verify

Ручной endpoint для отдельной проверки биометрии.

Request:

```json
{
  "employeeId": 2,
  "tripEventId": 10,
  "imageBase64": "ZmFrZS1pbWFnZQ==",
  "imageUrl": "https://storage.local/images/10.jpg"
}
```

curl:

```bash
curl -X POST http://localhost:8081/api/v1/biometric/verify \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 2,
    "tripEventId": 10,
    "imageBase64": "ZmFrZS1pbWFnZQ==",
    "imageUrl": "https://storage.local/images/10.jpg"
  }'
```

## GET /api/v1/biometric/verifications/{id}

Получить результат биометрической верификации.

curl:

```bash
curl -X GET http://localhost:8081/api/v1/biometric/verifications/1 \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>"
```

## Пример ответа

```json
{
  "id": 1,
  "tripEventId": 10,
  "employeeId": 2,
  "imageUrl": "https://storage.local/images/10.jpg",
  "matchScore": 0.95,
  "verified": true,
  "provider": "mock-face-provider",
  "verifiedAt": "2026-04-16T10:00:02",
  "createdAt": "2026-04-16T10:00:02"
}
```
