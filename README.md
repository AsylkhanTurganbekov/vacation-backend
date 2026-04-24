# Vacation Backend

Production-like REST API backend for controlling employee business trips with status transitions, trip events, biometric verification, reporting, JWT security, PostgreSQL, and Liquibase.

Frontend-oriented API docs: [docs/api/README.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/README.md)

## Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security + JWT
- PostgreSQL
- Liquibase
- Lombok
- MapStruct
- OpenAPI / Swagger
- Maven
- Docker Compose
- Nginx
- GitLab CI/CD

## Profiles

- `dev` by default
- `prod` for containerized/runtime deployment

## Project Structure

```text
src/main/java/com/company/vacation
├── config
├── controller
├── dto
│   ├── auth
│   ├── biometric
│   ├── common
│   ├── report
│   ├── trip
│   └── user
├── entity
│   └── enums
├── exception
├── mapper
├── repository
├── security
├── service
│   └── impl
└── specification
```

## Run

```bash
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d postgres
mvn spring-boot:run
```

Swagger UI: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)

Если `mvn` не установлен, поставь Maven 3.9+ и проверь:

```bash
java -version
mvn -version
```

Локальный запуск без глобального Maven:

```bash
chmod +x mvn-local run-local.sh
./mvn-local -version
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d postgres
./run-local.sh
```

Локальный compose-запуск вместе с backend:

```bash
docker compose up --build -d
```

Локальный compose-запуск вместе с `nginx`:

```bash
cp .env.example .env
docker compose --profile proxy up --build -d
```

## Configuration

Основные значения вынесены в env vars:

- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`
- `APP_JWT_EXPIRATION`
- `APP_CORS_ALLOWED_ORIGIN_PATTERNS`
- `APP_OPENAPI_LOCAL_URL`
- `APP_OPENAPI_PROD_URL`
- `BACKEND_EXTERNAL_PORT`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_EXTERNAL_PORT`
- `NGINX_HTTP_PORT`

Файлы конфигурации:

- [application.yml](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/src/main/resources/application.yml)
- [application-dev.yml](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/src/main/resources/application-dev.yml)
- [application-prod.yml](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/src/main/resources/application-prod.yml)
- [.env.example](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/.env.example)

## Infra

- backend внутри контейнера: `8081`
- backend наружу по умолчанию: `8090`
- postgres наружу публикуется только через [docker-compose.local.yml](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docker-compose.local.yml)
- dockerized `nginx` не стартует по умолчанию и включается только профилем `proxy`
- health endpoint: `GET /actuator/health`
- info endpoint: `GET /actuator/info`
- metrics endpoint: `GET /actuator/metrics`

## Nginx

Reverse proxy config:

- [nginx/default.conf](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/nginx/default.conf)

Маршрутизация:

- `nginx:80 -> vacation-backend:8081`
- `/actuator/health` проксируется отдельно для health checks

Если на сервере уже есть системный `nginx`, контейнерный `nginx` не поднимай. Используй отдельный backend-порт и системный reverse proxy:

```bash
cp .env.example .env
docker compose up --build -d
```

Пример server block:

- [deploy/nginx/vacation-backend.conf.example](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/deploy/nginx/vacation-backend.conf.example)

В этом сценарии системный `nginx` проксирует на `127.0.0.1:8090`.

Если нужен именно контейнерный `nginx`, запускай его явно:

```bash
docker compose --profile proxy up --build -d
```

## CI/CD

Базовый GitLab pipeline:

- [.gitlab-ci.yml](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/.gitlab-ci.yml)
- GitHub Actions deploy: [.github/workflows/deploy.yml](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/.github/workflows/deploy.yml)
- GitHub setup notes: [docs/github-actions-deploy.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/github-actions-deploy.md)

Стадии:

- `compile`
- `package`
- `docker-build`
- `deploy-prod`

Для deploy pipeline ожидает переменные:

- `CI_REGISTRY`
- `CI_REGISTRY_USER`
- `CI_REGISTRY_PASSWORD`
- `SSH_PRIVATE_KEY`
- `DEPLOY_HOST`
- `DEPLOY_USER`
- `DEPLOY_PATH`

## Demo Credentials

- `admin@vacation.local` / `Admin123!`
- `employee@vacation.local` / `Employee123!`

## Endpoints

### Auth

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `GET /api/v1/auth/me`

### Users

- `GET /api/v1/users`
- `GET /api/v1/users/{id}`
- `POST /api/v1/users`
- `PUT /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}/active`

### Trips

- `POST /api/v1/trips`
- `GET /api/v1/trips`
- `GET /api/v1/trips/{id}`
- `PUT /api/v1/trips/{id}`
- `PATCH /api/v1/trips/{id}/approve`
- `PATCH /api/v1/trips/{id}/cancel`

### Employee Trips

- `GET /api/v1/employee/trips`
- `GET /api/v1/employee/trips/{id}`

### Trip Events

- `POST /api/v1/trips/{tripId}/events/departure`
- `POST /api/v1/trips/{tripId}/events/arrival`
- `POST /api/v1/trips/{tripId}/events/return`
- `GET /api/v1/trips/{tripId}/events`

### Biometric

- `POST /api/v1/biometric/verify`
- `GET /api/v1/biometric/verifications/{id}`

### Reports

- `GET /api/v1/reports/trips`
- `GET /api/v1/reports/trips/summary`
- `GET /api/v1/reports/employees/{employeeId}/trips`

## Test Sequence

1. Login as admin.
2. Create employee or use seeded `employee@vacation.local`.
3. Create trip via `POST /api/v1/trips`.
4. Approve trip via `PATCH /api/v1/trips/{id}/approve`.
5. Login as employee.
6. Send `departure`.
7. Send `arrival`.
8. Send `return`.
9. Get reports.

## Curl Examples

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@vacation.local","password":"Admin123!"}'
```

```bash
curl -X POST http://localhost:8081/api/v1/trips \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "employeeId": 2,
    "purpose": "Equipment inspection",
    "destinationAddress": "Astana, Turan 12",
    "plannedStartDateTime": "2026-04-20T09:00:00",
    "plannedEndDateTime": "2026-04-22T18:00:00"
  }'
```

```bash
curl -X PATCH http://localhost:8081/api/v1/trips/2/approve \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

```bash
curl -X POST http://localhost:8081/api/v1/trips/2/events/departure \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 43.238949,
    "longitude": 76.889709,
    "address": "Almaty office",
    "eventTime": "2026-04-16T10:00:00",
    "imageBase64": "ZmFrZS1pbWFnZQ=="
  }'
```

```bash
curl -X POST http://localhost:8081/api/v1/trips/2/events/arrival \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 51.128207,
    "longitude": 71.430420,
    "address": "Client site",
    "eventTime": "2026-04-16T18:00:00",
    "imageBase64": "ZmFrZS1pbWFnZQ=="
  }'
```

```bash
curl -X POST http://localhost:8081/api/v1/trips/2/events/return \
  -H "Authorization: Bearer <EMPLOYEE_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 43.238949,
    "longitude": 76.889709,
    "address": "Back to office",
    "eventTime": "2026-04-18T18:00:00",
    "imageBase64": "ZmFrZS1pbWFnZQ=="
  }'
```

```bash
curl -X GET "http://localhost:8081/api/v1/reports/trips/summary" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```
