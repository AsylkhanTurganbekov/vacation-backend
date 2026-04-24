# Auth API

## POST /api/v1/auth/login

Логин и получение JWT.

Request:

```json
{
  "email": "admin@vacation.local",
  "password": "Admin123!"
}
```

Response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "fullName": "System Administrator",
    "email": "admin@vacation.local",
    "role": "ADMIN",
    "department": "Operations",
    "position": "Platform Administrator",
    "active": true,
    "createdAt": "2026-01-01T09:00:00",
    "updatedAt": "2026-01-01T09:00:00"
  }
}
```

curl:

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@vacation.local",
    "password": "Admin123!"
  }'
```

## POST /api/v1/auth/register

Демо-регистрация пользователя.

Request:

```json
{
  "fullName": "New Employee",
  "email": "new.employee@vacation.local",
  "password": "Password123!",
  "role": "EMPLOYEE",
  "department": "Field Service",
  "position": "Inspector"
}
```

curl:

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
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

## GET /api/v1/auth/me

Текущий пользователь по токену.

curl:

```bash
curl -X GET http://localhost:8081/api/v1/auth/me \
  -H "Authorization: Bearer <TOKEN>"
```
