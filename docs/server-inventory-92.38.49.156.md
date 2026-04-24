# Server Inventory: `92.38.49.156`

Snapshot date: `2026-04-24`

This file is a safety memo for deploying `vacation-backend` on the shared server without breaking existing services.

## Host

- Hostname: `vds29233.vpsza500.kz`
- OS: Ubuntu 22.04.5
- Existing system `nginx` is already running on `80` and `443`
- Docker is already installed and actively used by multiple projects

## Running Docker Containers

| Container | Image | Status | Published ports | Notes |
| --- | --- | --- | --- | --- |
| `sd-bot-bot-1` | `sd-bot-bot` | Up | `8000 -> 8000` | Telegram bot app |
| `sd-bot-db-1` | `postgres:15-alpine` | Up (healthy) | none | Internal DB for `sd-bot` |
| `sd-frontend` | `sd-frontend:latest` | Up | `8081 -> 80` | Existing frontend |
| `sd-backend` | `sd-backend:latest` | Up | `18080 -> 18080` | Existing backend |
| `sd-minio` | `minio/minio:latest` | Up | `9000`, `9001` | MinIO |
| `sd-db-backend` | `postgis/postgis:15-3.3` | Up (healthy) | `5432 -> 5432` | Existing public DB |
| `wedding-container` | `wedding-site` | Up | `3006 -> 80` | Existing site |
| `onlyoffice-docs` | `onlyoffice/documentserver` | Up | `8082 -> 80` | OnlyOffice |
| `kindy-b2b-app` | `kindy-landing` | Up | `3005 -> 3000` | Existing frontend |

## Occupied Ports

Do not use these ports for `vacation-backend`:

- `80`
- `443`
- `5432`
- `8000`
- `8081`
- `8082`
- `9000`
- `9001`
- `18080`
- `3005`
- `3006`

Recommended safe port for `vacation-backend` backend: `8090`

## Existing Nginx Configs

### `/etc/nginx/conf.d`

- `kindy-b2b.conf`
- `onlyoffice.conf`
- `senimdesk.conf`

### `/etc/nginx/sites-enabled`

- `wedding-domains`

## Known Project Directories

- `/var/www/sd-bot`
- `/var/www/sd-backend`
- `/var/www/sd-frontend`
- `/var/www/html`

Recommended directory for the new project:

- `/var/www/vacation-backend`

## Compose Working Directories Found

- `sd-bot-bot-1` -> `/var/www/sd-bot`
- `sd-bot-db-1` -> `/var/www/sd-bot`
- `sd-frontend` -> `/var/www/sd-frontend`
- `sd-backend` -> `/var/www/sd-backend`
- `sd-minio` -> `/var/www/sd-backend`
- `sd-db-backend` -> `/var/www/sd-backend`

## Safe Deployment Rules For `vacation-backend`

- Do not stop or restart existing containers.
- Do not change existing `nginx` configs unrelated to this project.
- Do not bind anything to `80` or `443` directly from Docker.
- Do not publish a new Postgres on `5432`.
- Start only the new project in its own directory.
- Prefer backend on `127.0.0.1:8090` or `0.0.0.0:8090`.
- If a domain is added later, use system `nginx` to proxy to `127.0.0.1:8090`.

## Deployment Shape Chosen For This Repo

This repo was adjusted for this server:

- main [docker-compose.yml](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docker-compose.yml) is now safe for a shared server
- backend publishes to `8090` by default through `BACKEND_EXTERNAL_PORT`
- `postgres` is not published externally by default
- containerized `nginx` is optional and starts only with `--profile proxy`
- local-only Postgres publishing was moved to [docker-compose.local.yml](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docker-compose.local.yml)

## Intended First Deploy

1. Copy the project into `/var/www/vacation-backend`
2. Create `.env`
3. Run:

```bash
docker compose up --build -d
```

4. Verify:

```bash
curl http://127.0.0.1:8090/actuator/health
```

5. Only after that, optionally add a new system `nginx` server block using:

- [deploy/nginx/vacation-backend.conf.example](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/deploy/nginx/vacation-backend.conf.example)
