# Postman Testing Guide

This folder contains import-ready Postman assets generated from the **actual** `re-content-backend` implementation.

## Files

- `re-content-backend.postman_collection.json`
- `re-content-local.postman_environment.json`

## Quick Start

1. Start backend on `http://localhost:8080`.
2. Import collection and environment into Postman.
3. Select environment `re-content-local`.
4. Run requests in the order below.

## Suggested Execution Order

1. `Health / GET /api/v1/health`
2. `Auth / Register` (or `Auth / Login (User)` if user exists)
3. `Auth / Get Current Auth User`
4. `Users / Get My Profile`
5. `Users / Update My Profile`
6. `Favorites / Add Favorite`
7. `Favorites / List Favorites`
8. `Favorites / Delete Favorite`
9. `Watchlist / Add Watchlist Item`
10. `Watchlist / Update Watchlist Status`
11. `Ratings / Add Rating`
12. `Reviews / Create Review`
13. `Membership / List Membership Plans`
14. `Membership / Subscribe`
15. `Membership / Get My Membership`
16. `Auth / Refresh Token`
17. `Auth / Logout`
18. `Actuator / GET /actuator/health`
19. `Actuator / GET /actuator/info`

## Admin Flow Notes

Admin endpoints require `ADMIN` role and use `adminAccessToken`.

- Run `Auth / Login (Admin)` after setting `adminEmail` and `adminPassword` in environment.
- If you do not have an admin user yet, create one in DB. Example approach:

```sql
-- Find USER role id (already seeded roles exist)
SELECT id, name FROM roles;

-- Attach ADMIN role to existing user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@example.com'
  AND r.name = 'ADMIN'
ON CONFLICT DO NOTHING;
```

## Auth/Cookie Notes

Refresh endpoint expects the `refresh_token` cookie.

- The collection captures `refreshToken` from `Set-Cookie` header.
- Refresh/logout requests send explicit header: `Cookie: refresh_token={{refreshToken}}`.
- This works even if Postman cookie jar behavior differs across setups.

## Validation-Aware Payloads Included

Payloads are aligned with backend validation constraints:

- Register username: 3-50, pattern `^[a-zA-Z0-9._-]+$`
- Register password: 8-120
- Favorite/watchlist title max 300
- Rating score: 0.5..10.0
- Review content: 2..5000
- Watchlist status: `planned|watching|completed|dropped`
- Media type: `movie|tv` (`series` accepted and normalized)

## Negative Cases Included

- `Auth / Register (Negative Validation)`
- `Ratings / Add Rating (Negative Validation)`
- `Admin / Admin Endpoint (Negative - USER token)`

These verify error contract and authorization behavior.
