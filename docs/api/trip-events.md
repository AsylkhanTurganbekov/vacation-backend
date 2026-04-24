# Trip Events API

События командировки. После каждого события выполняется mock biometric verification.

## Ограничения бизнес-флоу

- `DEPARTURE` возможен только для командировки в статусе `APPROVED`
- `ARRIVAL` возможен только после `DEPARTURE`
- `RETURN` возможен только после `ARRIVAL`
- `EMPLOYEE` может отправлять события только по своей командировке

## POST /api/v1/trips/{tripId}/events/departure

Request:

```json
{
  "latitude": 43.238949,
  "longitude": 76.889709,
  "address": "Almaty office",
  "eventTime": "2026-04-16T10:00:00",
  "comment": "Leaving office",
  "imageBase64": "ZmFrZS1pbWFnZQ=="
}
```

curl:

```bash
curl -X POST http://localhost:8081/api/v1/trips/1/events/departure \
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

## POST /api/v1/trips/{tripId}/events/arrival

curl:

```bash
curl -X POST http://localhost:8081/api/v1/trips/1/events/arrival \
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

## POST /api/v1/trips/{tripId}/events/return

curl:

```bash
curl -X POST http://localhost:8081/api/v1/trips/1/events/return \
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

## GET /api/v1/trips/{tripId}/events

История событий командировки.

curl:

```bash
curl -X GET http://localhost:8081/api/v1/trips/1/events \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>"
```

## Пример ответа события

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
