# Re Content Backend - Tum API Baglanti ve Sema Dokumani (TR)

Bu dokuman mevcut implementasyona gore hazirlanmistir.

## 1) Genel Baglanti Bilgileri

- Base URL (lokal): `http://localhost:8080`
- API prefix: `/api/v1`
- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- Actuator: `/actuator/health`, `/actuator/info`

### Kimlik Dogrulama Tipi

- Access token: `Authorization: Bearer <JWT>`
- Refresh token: `HttpOnly` cookie (`refresh_token`)
- Refresh endpoint body beklemez, cookie bekler.

### Ortak Header'lar

- `Accept: application/json`
- Body olan isteklerde: `Content-Type: application/json`
- Korumali endpoint'lerde: `Authorization: Bearer {{accessToken}}`

### Standart Hata Semasi

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

`errors` alani dogrulama disi hatalarda bos/olmayan olabilir.

## 2) Enum ve Sema Notlari

### MediaType

- Kabul edilen degerler: `movie`, `tv`
- `series` gonderilirse backend bunu `tv` olarak normalize eder.
- Buyuk/kucuk harf toleransi vardir.

### WatchlistStatus

- `planned`, `watching`, `completed`, `dropped`
- Buyuk/kucuk harf toleransi vardir.

### Membership / User / Role

- Role: `USER`, `ADMIN`, `OPERATOR`
- MembershipStatus: `ACTIVE`, `CANCELED`, `EXPIRED`
- DurationType: `FREE`, `MONTHLY`, `YEARLY`
- UserStatus: `ACTIVE`, `DISABLED`, `LOCKED`, `PENDING_VERIFICATION`

## 3) AUTH API'leri

### 3.1 `POST /api/v1/auth/register`

- Auth: Gerekmez
- Amac: Yeni kullanici olusturur, access token doner, refresh cookie set eder.
- Status: `201`

Request:

```json
{
  "username": "qa_user_1001",
  "email": "qa_user_1001@example.com",
  "password": "StrongPass123!",
  "firstName": "QA",
  "lastName": "User"
}
```

Validasyon:

- `username`: bos olamaz, 3-50, regex `^[a-zA-Z0-9._-]+$`
- `email`: gecerli email, max 255
- `password`: bos olamaz, 8-120
- `firstName`: max 80
- `lastName`: max 80

Response (AuthResponse):

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresInSeconds": 900,
  "user": {
    "id": "uuid",
    "username": "qa_user_1001",
    "email": "qa_user_1001@example.com",
    "firstName": "QA",
    "lastName": "User",
    "avatarUrl": null,
    "status": "ACTIVE",
    "roles": ["USER"],
    "createdAt": "2026-04-17T14:00:00Z",
    "updatedAt": "2026-04-17T14:00:00Z"
  }
}
```

Olasi hata kodlari:

- `EMAIL_ALREADY_EXISTS` (409)
- `USERNAME_ALREADY_EXISTS` (409)
- `VALIDATION_ERROR` (400)
- `INTERNAL_ERROR` (500)

---

### 3.2 `POST /api/v1/auth/login`

- Auth: Gerekmez
- Status: `200`
- Not: `login` alani hem username hem email alir.

Request:

```json
{
  "login": "qa_user_1001@example.com",
  "password": "StrongPass123!"
}
```

Validasyon:

- `login`: bos olamaz, max 255
- `password`: bos olamaz, max 120

Response: `AuthResponse` (register ile ayni format)

Olasi hata kodlari:

- `INVALID_CREDENTIALS` (401)
- `USER_NOT_ACTIVE` (403)

---

### 3.3 `POST /api/v1/auth/refresh`

- Auth header: Gerekmez
- Cookie: **Zorunlu** -> `refresh_token=<token>`
- Status: `200`
- Not: Refresh token rotate edilir; eski token yeniden kullanilirsa gecersizdir.

Response: `AuthResponse` (yeni access token)

Olasi hata kodlari:

- `MISSING_REFRESH_TOKEN` (401)
- `INVALID_REFRESH_TOKEN` (401)

---

### 3.4 `POST /api/v1/auth/logout`

- Auth: Access token varsa iyi olur, ama refresh cookie null olsa da 204 donebilir.
- Cookie: Opsiyonel `refresh_token`
- Status: `204`
- Etki: Refresh token revoke edilir, cookie temizlenir.

---

### 3.5 `GET /api/v1/auth/me`

- Auth: Bearer zorunlu
- Status: `200`
- Response: `AuthResponse` ama `accessToken=null`, `expiresInSeconds=0`, `user` dolu.

## 4) USERS API'leri

### 4.1 `GET /api/v1/users/me`

- Auth: Bearer zorunlu
- Status: `200`
- Response: `UserResponse`

### 4.2 `PATCH /api/v1/users/me`

- Auth: Bearer zorunlu
- Status: `200`

Request:

```json
{
  "firstName": "Yeni",
  "lastName": "Isim",
  "avatarUrl": "https://example.com/avatar.png"
}
```

Validasyon:

- `firstName`: max 80
- `lastName`: max 80
- `avatarUrl`: max 1000

Response: `UserResponse`

## 5) FAVORITES API'leri

Base: `/api/v1/users/me/favorites`

### 5.1 `GET /api/v1/users/me/favorites`

- Auth: Bearer zorunlu
- Status: `200`
- Response: `FavoriteResponse[]`

### 5.2 `POST /api/v1/users/me/favorites`

- Auth: Bearer zorunlu
- Status: `201`
- Not: Upsert davranisi var (`user + mediaType + mediaId` unique)

Request (TMDb alias destekli):

```json
{
  "mediaType": "movie",
  "id": 550,
  "title": "Fight Club",
  "poster_path": "/poster.jpg",
  "backdrop_path": "/backdrop.jpg",
  "overview": "Aciklama",
  "release_date": "1999-10-15",
  "vote_average": 8.4
}
```

Desteklenen aliaslar:

- `id` -> `mediaId`
- `poster_path` -> `posterPath`
- `backdrop_path` -> `backdropPath`
- `release_date` / `first_air_date` -> `releaseDate`
- `vote_average` -> `voteAverage`

Validasyon:

- `mediaType`: zorunlu
- `mediaId`: zorunlu, pozitif
- `title`: zorunlu, max 300
- `overview`: max 5000
- `voteAverage`: 0.0 - 10.0

### 5.3 `DELETE /api/v1/users/me/favorites/{mediaType}/{mediaId}`

- Auth: Bearer zorunlu
- Path:
  - `mediaType`: movie|tv|series(->tv)
  - `mediaId`: pozitif long
- Status: `204`
- Hata: `FAVORITE_NOT_FOUND` (404)

## 6) WATCHLIST API'leri

Base: `/api/v1/users/me/watchlist`

### 6.1 `GET /api/v1/users/me/watchlist`

- Auth: Bearer zorunlu
- Status: `200`
- Response: `WatchlistResponse[]`

### 6.2 `POST /api/v1/users/me/watchlist`

- Auth: Bearer zorunlu
- Status: `201`
- Not: Upsert var (`user + mediaType + mediaId`)

Request:

```json
{
  "mediaType": "tv",
  "id": 1399,
  "title": "Game of Thrones",
  "poster_path": "/poster.jpg",
  "status": "watching"
}
```

Not: `status` verilmezse backend `planned` atar.

### 6.3 `PATCH /api/v1/users/me/watchlist/{id}`

- Auth: Bearer zorunlu
- Status: `200`

Request:

```json
{ "status": "completed" }
```

Validasyon:

- `status` zorunlu

Hata kodlari:

- `WATCHLIST_ITEM_NOT_FOUND` (404)
- `WATCHLIST_ITEM_FORBIDDEN` (403)

### 6.4 `DELETE /api/v1/users/me/watchlist/{id}`

- Auth: Bearer zorunlu
- Status: `204`
- Hata kodlari: yukaridaki ile ayni

## 7) RATINGS API'leri

Base: `/api/v1/users/me/ratings`

### 7.1 `GET /api/v1/users/me/ratings`

- Auth: Bearer zorunlu
- Status: `200`
- Response: `RatingResponse[]`

### 7.2 `POST /api/v1/users/me/ratings`

- Auth: Bearer zorunlu
- Status: `201`
- Not: Upsert var (`user + mediaType + mediaId`)

Request:

```json
{
  "mediaType": "movie",
  "mediaId": 603,
  "score": 8.5
}
```

Validasyon:

- `score`: 0.5 - 10.0
- `mediaId`: pozitif

### 7.3 `PATCH /api/v1/users/me/ratings/{id}`

- Auth: Bearer zorunlu
- Status: `200`

Request:

```json
{ "score": 9.0 }
```

Hata:

- `RATING_NOT_FOUND` (404)
- `RATING_FORBIDDEN` (403)

### 7.4 `DELETE /api/v1/users/me/ratings/{id}`

- Auth: Bearer zorunlu
- Status: `204`

## 8) REVIEWS API'leri

Base: `/api/v1/reviews`

### 8.1 `GET /api/v1/reviews/{mediaType}/{mediaId}`

- Auth: Gerekmez (public)
- Status: `200`
- Response: `ReviewResponse[]`

### 8.2 `POST /api/v1/reviews`

- Auth: Bearer zorunlu
- Status: `201`

Request:

```json
{
  "mediaType": "movie",
  "mediaId": 603,
  "content": "Bu film etkileyici.",
  "spoilerFlag": false
}
```

Validasyon:

- `content`: 2-5000

### 8.3 `PATCH /api/v1/reviews/{id}`

- Auth: Bearer zorunlu
- Status: `200`
- Yetki: Review sahibi veya `ADMIN`

Request:

```json
{
  "content": "Guncellenmis yazi",
  "spoilerFlag": true
}
```

### 8.4 `DELETE /api/v1/reviews/{id}`

- Auth: Bearer zorunlu
- Status: `204`
- Yetki: Review sahibi veya `ADMIN`

Hata:

- `REVIEW_NOT_FOUND` (404)
- `REVIEW_FORBIDDEN` (403)

## 9) MEMBERSHIP API'leri

### 9.1 `GET /api/v1/membership/plans`

- Auth: Gerekmez (public)
- Status: `200`
- Response: `MembershipPlanResponse[]` (sadece aktif planlar)

### 9.2 `POST /api/v1/membership/subscribe`

- Auth: Bearer zorunlu
- Status: `201`

Request:

```json
{ "planId": "uuid" }
```

Validasyon:

- `planId`: zorunlu UUID

Not:

- Kullanici aktif uyeligi varsa `CANCELED` yapilip yenisi `ACTIVE` acilir.

### 9.3 `GET /api/v1/users/me/membership`

- Auth: Bearer zorunlu
- Status: `200`
- Response: Aktif uyelik varsa `UserMembershipResponse`, yoksa `null`.

## 10) ADMIN API'leri

Base: `/api/v1/admin`

Tum endpoint'ler `ADMIN` rolu ister.

### 10.1 `GET /api/v1/admin/users`

- Auth: Bearer (ADMIN)
- Status: `200`
- Response: `AdminUserResponse[]`

### 10.2 `GET /api/v1/admin/audit-logs?page=0&size=50`

- Auth: Bearer (ADMIN)
- Status: `200`
- Response: Spring `Page<AuditLogResponse>`

Query:

- `page` default: `0`
- `size` default: `50`
- `size` max: `100` (service tarafinda kisitlanir)

### 10.3 `GET /api/v1/admin/health-summary`

- Auth: Bearer (ADMIN)
- Status: `200`
- Response: tablo bazli sayisal ozet (`users`, `favorites`, `watchlistItems`, `ratings`, `reviews`, `auditLogs`)

## 11) HEALTH / ACTUATOR

### 11.1 `GET /api/v1/health`

- Auth: Gerekmez
- Status: `200`

### 11.2 `GET /actuator/health`

- Auth: Gerekmez
- Status: `200`

### 11.3 `GET /actuator/info`

- Auth: Gerekmez
- Status: `200`

## 12) Postman ile Baglanti Akisi (Onerilen)

1. `Health` ile servis ayakta mi kontrol et.
2. `Auth/Register` veya `Auth/Login` calistir.
3. `accessToken` ve `refreshToken` env variable'larina yaz.
4. Korumali endpoint'lerde `Bearer {{accessToken}}` kullan.
5. Refresh icin body gonderme; `Cookie: refresh_token={{refreshToken}}` gonder.
6. Refresh cagrisindan sonra yeni token'lari env'e geri yaz.

## 13) Onemli Davranislar

- Refresh token tek kullanimli rotate akisi vardir.
- Ayni kullanici+media kaydinda favorite/watchlist/rating upsert davranisi vardir.
- `MediaType` olarak `series` gonderimi backend tarafinda `tv`'ye donusturulur.
- Validation hatalari 400 + `VALIDATION_ERROR` ile doner.

