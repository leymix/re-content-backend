# Re Content Backend

Production-oriented Spring Boot backend for the existing `re-content` React TMDb platform.

This project is intentionally separate from the frontend:

```text
operatipnProject/
├─ re-content/              # existing React frontend, untouched
└─ re-content-backend/      # this Spring Boot backend, own Git repository
```

## Solution Overview

`re-content-backend` owns user and platform data while the frontend can continue using TMDb for catalog browsing.

Phase 1 backend ownership:

- Authentication and JWT authorization.
- User profile.
- Favorites.
- Watchlist.
- Ratings.
- Reviews.
- Membership/subscription-ready structure.
- Admin and audit scaffolding.
- Health, Actuator, OpenAPI, Docker-ready local development.

Catalog features that should stay TMDb-based for now:

- Movie and TV browsing.
- Detail pages.
- Search.
- Person pages.
- Trailers.
- TMDb images and metadata enrichment.
- Language-specific TMDb requests.

## Backend Architecture

The backend is a modular monolith using feature-based packages:

```text
src/main/java/com/recontent/backend/
├─ config/
├─ security/
├─ common/
├─ exception/
├─ auth/
├─ user/
├─ favorite/
├─ watchlist/
├─ rating/
├─ review/
├─ membership/
├─ admin/
├─ audit/
└─ health/
```

Each feature keeps a clean boundary:

- `controller`: HTTP API shape.
- `service`: business rules and transactions.
- `repository`: persistence only.
- `dto`: request and response contracts.
- `entity`: JPA persistence model.
- `mapper`: entity-to-DTO conversion.

Entities are not exposed directly from controllers.

## Technology Stack

- Java 21.
- Spring Boot 4.0.5.
- Spring WebMVC.
- Spring Security.
- OAuth2 Resource Server JWT validation.
- Spring Data JPA.
- PostgreSQL.
- Flyway migrations.
- Bean Validation.
- Spring Boot Actuator.
- Springdoc OpenAPI/Swagger UI.
- Docker and Docker Compose.
- Testcontainers for PostgreSQL-backed integration tests.

## Domain Model

Core tables:

- `users`
- `roles`
- `user_roles`
- `refresh_tokens`
- `favorites`
- `watchlist_items`
- `ratings`
- `reviews`
- `membership_plans`
- `user_memberships`
- `audit_logs`

Media references use `mediaType` plus `mediaId` and do not require copying TMDb catalog ownership into this backend.

Supported `mediaType` values:

- `movie`
- `tv`

The API also accepts `series` and normalizes it to `tv`.

## Database Design

Flyway migration:

```text
src/main/resources/db/migration/V1__initial_schema.sql
```

The initial schema includes:

- UUID primary keys.
- Foreign keys.
- Unique constraints for user-owned media data.
- Check constraints for enum-like fields.
- Indexes for user/media/admin access patterns.
- `jsonb` audit metadata.
- Seeded roles: `USER`, `ADMIN`, `OPERATOR`.
- Seeded membership plans: `Free`, `Plus`, `Pro`.

## Security Design

Auth endpoints:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

Security behavior:

- Passwords are hashed with BCrypt.
- Access tokens are JWT Bearer tokens.
- Refresh tokens are random opaque tokens stored as SHA-256 hashes in PostgreSQL.
- Refresh tokens are delivered as HttpOnly cookies.
- Refresh rotates the refresh token and revokes the old token.
- Logout revokes the refresh token and clears the cookie.
- Admin endpoints require `ADMIN`.
- CORS allows local React dev origins by default.

Frontend token strategy:

- Store the access token in memory or your frontend auth state.
- Do not store refresh tokens in localStorage.
- Call refresh/logout with credentials enabled so the HttpOnly cookie is sent.

## API Summary

User profile:

- `GET /api/v1/users/me`
- `PATCH /api/v1/users/me`

Favorites:

- `GET /api/v1/users/me/favorites`
- `POST /api/v1/users/me/favorites`
- `DELETE /api/v1/users/me/favorites/{mediaType}/{mediaId}`

Watchlist:

- `GET /api/v1/users/me/watchlist`
- `POST /api/v1/users/me/watchlist`
- `PATCH /api/v1/users/me/watchlist/{id}`
- `DELETE /api/v1/users/me/watchlist/{id}`

Ratings:

- `GET /api/v1/users/me/ratings`
- `POST /api/v1/users/me/ratings`
- `PATCH /api/v1/users/me/ratings/{id}`
- `DELETE /api/v1/users/me/ratings/{id}`

Reviews:

- `GET /api/v1/reviews/{mediaType}/{mediaId}`
- `POST /api/v1/reviews`
- `PATCH /api/v1/reviews/{id}`
- `DELETE /api/v1/reviews/{id}`

Membership:

- `GET /api/v1/membership/plans`
- `POST /api/v1/membership/subscribe`
- `GET /api/v1/users/me/membership`

Admin:

- `GET /api/v1/admin/users`
- `GET /api/v1/admin/audit-logs`
- `GET /api/v1/admin/health-summary`

Health:

- `GET /api/v1/health`
- `GET /actuator/health`
- `GET /actuator/info`

OpenAPI:

- `GET /swagger-ui.html`
- `GET /v3/api-docs`

## Error Contract

Validation and domain errors use a consistent shape:

```json
{
  "timestamp": "2026-04-17T12:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/v1/auth/register",
  "errors": {
    "email": "must be a well-formed email address"
  }
}
```

## Frontend Integration Strategy

The existing React app should migrate gradually:

1. Keep TMDb catalog browsing, search, detail pages, trailers, people, and language support as-is.
2. Replace `src/services/authService.js` with backend calls to `/api/v1/auth/*`.
3. Add a backend Axios client with `baseURL=http://localhost:8080/api/v1`.
4. Send `Authorization: Bearer <accessToken>` for authenticated requests.
5. Call refresh/logout with `withCredentials: true`.
6. Move favorites from `localStorage` to `/users/me/favorites`.
7. Add watchlist, ratings, and reviews using the backend endpoints.
8. Later, optionally add a backend TMDb proxy/cache/search index without changing the phase-1 ownership model.

Example favorite request compatible with current TMDb-shaped frontend data:

```json
{
  "mediaType": "movie",
  "id": 550,
  "title": "Fight Club",
  "poster_path": "/poster.jpg",
  "backdrop_path": "/backdrop.jpg",
  "overview": "A favorite movie",
  "release_date": "1999-10-15",
  "vote_average": 8.4
}
```

The backend responds with normalized names:

```json
{
  "id": "64f7d679-9e87-4c59-a2fd-54b213f1d03a",
  "mediaType": "movie",
  "mediaId": 550,
  "title": "Fight Club",
  "posterPath": "/poster.jpg",
  "backdropPath": "/backdrop.jpg"
}
```

## Local Development Without Docker

Requirements:

- Java 21.
- PostgreSQL running locally.

Create a database:

```powershell
createdb recontent
```

Run:

```powershell
cd C:\Users\ayhan\Desktop\operatipnProject\re-content-backend
copy .env.example .env
.\mvnw.cmd spring-boot:run
```

The app starts on:

```text
http://localhost:8080
```

## Local Development With Docker

Start the backend and PostgreSQL:

```powershell
cd C:\Users\ayhan\Desktop\operatipnProject\re-content-backend
docker compose up --build
```

Stop:

```powershell
docker compose down
```

Remove the local database volume:

```powershell
docker compose down -v
```

## Run Alongside React

Terminal 1:

```powershell
cd C:\Users\ayhan\Desktop\operatipnProject\re-content-backend
docker compose up --build
```

Terminal 2:

```powershell
cd C:\Users\ayhan\Desktop\operatipnProject\re-content
npm start
```

Default CORS allows:

```text
http://localhost:3000
http://127.0.0.1:3000
```

## Full-Stack Infra Entry Point

For full-stack local/self-host style runtime with a single Nginx entrypoint,
use the infra setup inside this backend repository:

```powershell
cd C:\Users\ayhan\Desktop\operatipnProject\re-content-backend\infra
copy .env.example .env
docker compose --env-file .env up --build
```

This stack runs:

- `postgres`
- `backend`
- `frontend`
- `nginx` (public entrypoint at `http://localhost`)

## Tests

Run:

```powershell
.\mvnw.cmd test
```

Integration tests use Testcontainers and PostgreSQL. If Docker is unavailable, they are skipped; when Docker Desktop or CI Docker is running, they execute against a real PostgreSQL container.

Current tested scenarios include:

- Register/login/current-user/refresh/logout flow.
- Refresh token rotation.
- Standard validation error shape.
- Favorite idempotent upsert with TMDb-style aliases.
- Rating validation.
- Admin endpoint protection.
- Review owner authorization.

## Operations Readiness

Included now:

- Actuator health/info.
- Structured-log-friendly console pattern.
- Environment-based config.
- Central audit log service.
- Docker build and Compose runtime.
- PostgreSQL migration discipline.

Future-ready additions:

- Prometheus/Grafana via Micrometer registry.
- Elasticsearch indexing for reviews/watchlist/search enrichment.
- Kubernetes manifests or Helm chart.
- CI/CD pipeline for test, build, image publish, deploy.
- External secret manager for JWT/database credentials.
- Admin role management endpoints.

## Git Repository

This backend is designed to be its own repository:

```powershell
cd C:\Users\ayhan\Desktop\operatipnProject\re-content-backend
git status
```

The frontend repository remains separate at:

```text
C:\Users\ayhan\Desktop\operatipnProject\re-content
```

## Next Recommended Phase

Connect the React frontend incrementally:

1. Implement a backend Axios client and auth service.
2. Add login/register UI.
3. Move favorites from Redux/localStorage persistence to backend sync.
4. Add watchlist UI.
5. Add ratings and reviews to movie/series detail pages.
6. Add admin user/audit pages after a first admin account seeding strategy is chosen.
