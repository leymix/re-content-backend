# Re-Content Full-Stack Infra (Phase 6)

This folder is the full-stack entrypoint for:

- `../../re-content` (React frontend)
- `..` (Spring Boot backend)

Nginx is the single public entrypoint and proxies:

- `/` to frontend
- `/api` to backend
- `/actuator` to backend (debug/readiness convenience)

## Quick Start

```powershell
cd C:\Users\ayhan\Desktop\operatipnProject\re-content-backend\infra
copy .env.example .env
docker compose --env-file .env up --build
```

Open:

- App: `http://localhost:3000` (or `http://localhost:${FRONTEND_PUBLIC_PORT}`)
- API health (gateway): `http://localhost:3000/api/v1/health`
- API health (direct backend): `http://localhost:8080/api/v1/health`

## Common Commands

Start in background:

```powershell
docker compose --env-file .env up --build -d
```

Stop stack:

```powershell
docker compose down
```

Stop and remove DB volume:

```powershell
docker compose down -v
```

Follow logs:

```powershell
docker compose logs -f
docker compose logs -f nginx
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
```

Validate merged compose config:

```powershell
docker compose --env-file .env config
```

Run from:

```text
C:\Users\ayhan\Desktop\operatipnProject\re-content-backend\infra
```

## Notes

- Published host ports:
  - Frontend gateway (nginx): `3000 -> 80`
  - Backend: `8080 -> 8080`
  - Postgres: `5432 -> 5432`
- Browser API calls are same-origin (`REACT_APP_BACKEND_BASE_URL=/api/v1`) to keep cookie/refresh flow proxy-friendly.
- For HTTPS deployment later, set:
  - `REFRESH_COOKIE_SECURE=true`
  - stricter `CORS_ALLOWED_ORIGINS`
  - strong non-default `JWT_SECRET`
