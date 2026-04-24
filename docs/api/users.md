# Users API

Доступно только для `ADMIN`.

## GET /api/v1/users

Список пользователей.

curl:

```bash
curl -X GET "http://localhost:8081/api/v1/users?page=0&size=20&sort=id,asc" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## GET /api/v1/users/{id}

Получить пользователя.

curl:

```bash
curl -X GET http://localhost:8081/api/v1/users/2 \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

## POST /api/v1/users

Создать пользователя.

Request:

```json
{
  "fullName": "Employee One",
  "email": "employee.one@vacation.local",
  "password": "Password123!",
  "role": "EMPLOYEE",
  "department": "Sales",
  "position": "Regional Manager",
  "active": true
}
```

curl:

```bash
curl -X POST http://localhost:8081/api/v1/users \
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

## PUT /api/v1/users/{id}

Обновить пользователя.

curl:

```bash
curl -X PUT http://localhost:8081/api/v1/users/2 \
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

## PATCH /api/v1/users/{id}/active

Изменить флаг активности.

curl:

```bash
curl -X PATCH http://localhost:8081/api/v1/users/2/active \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "active": false
  }'
```
