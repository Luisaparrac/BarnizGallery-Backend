# Barniz Gallery – Backend (MUSEO)

Backend of **MUSEO**, a photo gallery with an online shop for **Barniz de Pasto Mopa-Mopa**
(UNESCO Intangible Cultural Heritage, 2020). Master artisans run their own stores, buyers browse the
pieces, receive recommendations, take part in live online auctions and contact the masters.

This repository contains only the backend. The frontend lives in a separate repository and consumes the
REST API and WebSocket contract described below.

Final project of the Software Design Patterns course. It implements 8 design patterns.

## Stack

| Topic | Choice |
|---|---|
| Language / build | Java 21, Maven Wrapper |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, WebSocket) |
| Database | PostgreSQL 18, schema `museo` created by Flyway migrations (`ddl-auto=validate`) |
| API docs | springdoc-openapi – Swagger UI at `/swagger-ui.html` |
| Real time | STOMP over WebSocket (SockJS fallback) at `/ws` |
| Deployment | Docker image on Render |

## Architecture

Layered MVC:

```
HTTP / WebSocket
      │
 controller/      thin REST controllers: validate input, delegate
      │
 service/         business logic and transactions
      │      ╲
      │       patterns/   observer · state · composite · facade · adapter · strategy · builder · factorymethod
      │
 repository/      Spring Data JPA interfaces
      │
 PostgreSQL (schema museo)
```

Other packages: `entity/` (JPA entities), `enums/` (English enums), `converter/`
(Spanish DB values ↔ English enums), `dto/` (request and response records), `mapper/`,
`exception/` (global JSON error handler) and `config/`.

Important rules:

- The backend **never creates or changes tables** (`spring.jpa.hibernate.ddl-auto=validate`); the schema
  comes from the Flyway migrations in `src/main/resources/db/migration`, written by the database team.
- Business rules that live in the database (stock reservations, payment triggers, audit, currency
  checks) are called from the backend, not re-implemented in Java.
- There is **no seed data**: an empty table returns an empty list.
- Masters and stores **cannot be deleted** through the API (the database cascades deletes).
- All times are **UTC**.

## Running locally

Requirements: Java 21 and a local PostgreSQL 18.

1. Create a local database and restore the v1 backup into it with the PostgreSQL 18 `psql`
   (`psql -v ON_ERROR_STOP=1 -d museo_local -f museo_backup.sql`). The backup is not in this repository.
2. Copy `src/main/resources/application-local.properties.example` to
   `src/main/resources/application-local.properties` (ignored by git) and fill in the user and password.
   On the first start Flyway records the existing v1 tables as **baseline version 1** and applies
   `V2__museo_v2.sql`, which archives them as `public.museo_v1_*` and creates the `museo` schema with the
   migrated data. Later starts find the schema up to date.
3. Run the tests (they never connect to the database):
   ```
   .\mvnw.cmd verify
   ```
4. Start the app:
   ```
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
   ```
5. Open http://localhost:8080/swagger-ui.html

## Environment variables

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | JDBC URL. On Render use the **internal** URL: `jdbc:postgresql://<INTERNAL_HOST>:5432/museo_hm45` | – |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials | – |
| `JWT_SECRET` | HS256 signing key, at least 32 bytes. **Required**: the app does not start without it | – |
| `JWT_ACCESS_MINUTES` / `JWT_REFRESH_DAYS` | Lifetime of access tokens (minutes) and refresh tokens (days) | `15` / `7` |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | Cloudinary account for photos (empty = upload disabled) | – |
| `FRONTEND_URL` | Frontend base URL used in the email links (set it in production) | `http://localhost:5173` |
| `BREVO_API_KEY` / `MAIL_FROM` / `MAIL_FROM_NAME` | Brevo API key and the verified sender of the account emails (empty = emails disabled) | – / – / `MUSEO Barniz de Pasto` |
| `WOMPI_PUBLIC_KEY` / `WOMPI_INTEGRITY_SECRET` / `WOMPI_EVENTS_SECRET` | Wompi keys for COP payments (empty = Wompi disabled; `pub_test_` keys use the sandbox) | – |
| `WOMPI_BASE_URL` | Wompi Web Checkout URL | `https://checkout.wompi.co/p/` |
| `PAYPAL_CLIENT_ID` / `PAYPAL_CLIENT_SECRET` / `PAYPAL_WEBHOOK_ID` | PayPal REST app and webhook for USD payments (empty = PayPal disabled) | – |
| `PAYPAL_BASE_URL` | PayPal REST API base URL | `https://api-m.sandbox.paypal.com` |
| `TRUSTED_PROXY_HOPS` | Proxies in front of the app that append to `X-Forwarded-For` (set `1` on Render); used to find the real client IP for the login limit | `0` |
| `FLYWAY_ENABLED` | Run the Flyway migrations on start (baseline 1 on an existing v1 database, then V2) | `true` |
| `PORT` | HTTP port (set by Render) | `8080` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins (REST and WebSocket) | `http://localhost:5173,http://localhost:3000` |
| `AUCTION_SCHEDULER_ENABLED` | Run the 60-second auction scheduler | `true` |
| `AI_PROVIDER` | AI provider (not chosen yet; `none` disables AI) | `none` |
| `AI_SERVICE_URL` | Base URL of the MUSEO AI service, without a trailing `/` (empty disables it) | – |
| `AI_SERVICE_KEY` | Shared key sent in the `X-API-Key` header to the AI service (empty disables it) | – |
| `AI_API_KEY` | Not used yet: it will be added together with the adapter when the AI provider is chosen | – |
| `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k` (already set in the Dockerfile) | – |

## REST API

All JSON. DTOs expose both languages (`titleEs`, `titleEn`, …) so the frontend chooses which one to show.
Dates are ISO-8601 (UTC). Enum values are in English (`EXHIBITED`, `ACTIVE`, `PENDING_PAYMENT`, …).

### System
| Method | Path | Description |
|---|---|---|
| GET | `/api/health` | `{ status, aiEnabled, storageEnabled }` |

### Catalog
| Method | Path | Description |
|---|---|---|
| GET | `/api/gallery` | Full tree Gallery → Stores → Artworks (Composite): published stores of approved masters and their exhibited or auctioned artworks |
| GET | `/api/masters` | Masters |
| GET | `/api/masters/{id}` | Master + its store |
| POST / PUT | `/api/masters`, `/api/masters/{id}` | Create / update master |
| GET | `/api/stores` | Published stores of approved masters, newest first, with slug, logo, banner and count of public artworks |
| GET | `/api/stores/{slug}` | Public page of a published store (approved master): visible sections in order and exhibited or auctioned artworks; 404 otherwise |
| GET | `/api/artworks?storeId=&status=` | Filterable list |
| GET | `/api/artworks/{id}` | Artwork + photos + current auction |
| GET | `/api/artworks/{id}/photos` | Photos |
| POST | `/api/artworks/{id}/photos` | Register photo by URL `{ fileUrl, angle }` |
| POST | `/api/artworks/{id}/photos/upload` | Multipart upload (503 while storage is disabled) |
| DELETE | `/api/photos/{id}` | Delete a photo |

### Users
| Method | Path | Description |
|---|---|---|
| GET | `/api/users/{id}` | User |
| PATCH | `/api/users/{id}/preferences` | Change the preferred language |
| GET / PATCH | `/api/users/me` | Profile of the logged-in user; PATCH `{ name?, phone?, countryCode?, preferredLanguage?, avatarCode? }` |
| GET / PUT | `/api/users/me/taste-profile` | Same questionnaire for the logged-in user (catalog codes; unknown or inactive codes → 422) |
| GET / PUT | `/api/users/{id}/taste-profile` | Read / save the initial questionnaire as catalog codes `{ colorCodes, shapeCodes, styleCodes, motifCodes, budgetRangeCode }` |

### Interactions and recommendations
| Method | Path | Description |
|---|---|---|
| POST | `/api/interactions` | `{ userId, artworkId?, storeId?, action, durationSeconds }` – with an artwork: `VIEW` (duration required), `ZOOM`, `FAVORITE`, `ADD_TO_CART`, `SHARE`; with a store: `STORE_VISIT`, `CUSTOM_ORDER`. `BID` and `PURCHASE` are recorded automatically by the backend |
| GET | `/api/users/{id}/interactions` | History |
| POST | `/api/users/{id}/recommendations?strategy=hybrid` | Compute recommendations of `target=stores` (default) or `target=artworks` with a strategy (`profile`, `interactions`, `hybrid`, `ai`, `ai-service`) and replace the previous ones of that target. `ai-service` asks the AI service and falls back to `hybrid` if it is disabled or fails |
| GET | `/api/users/{id}/recommendations` | Last saved recommendations |

### Auctions and bids
| Method | Path | Description |
|---|---|---|
| GET | `/api/auctions?status=` | List with highest bid and bid count |
| GET | `/api/auctions/{id}` | Detail |
| POST | `/api/auctions` | `{ artworkId, basePrice?, startDate, endDate }` (Builder) |
| GET | `/api/artworks/{id}/suggested-price` | Price suggested by the AI service from at least 3 comparable priced artworks in the auction currency (503 when AI is disabled or cannot suggest one) |
| POST | `/api/auctions/{id}/start` · `/finish` · `/cancel` | Lifecycle (State) |
| GET | `/api/auctions/{id}/bids` | Bids, highest first |
| POST | `/api/auctions/{id}/bids` | `{ userId, amount, currency }` |

Bid rules: only in `ACTIVE` auctions; `currency` must equal the auction currency (new auctions use the
`currency.default` setting; historical auctions have none yet and answer 409); `amount` must be at least
max(highest bid, base price) + `auction.min_increment` (422 otherwise); more than `auction.max_bids_per_minute`
bids per minute from the same user → 429; an amount above `auction.suspicious_multiplier` × the reference is
accepted but reported to `/topic/admin/anomalies`. These values live in the `app_settings` table; while one of
them is `null` the operation answers 409 `setting_not_configured` naming it.

### Contact
The v1 contact messages were migrated by the database to conversations and messages. The old
`/api/contact-messages` endpoints were removed; the conversation endpoints replace them.

### Catalogs
`GET /api/catalogs` (public) returns the active colors, shapes, motifs, styles, budget ranges, avatars and
store templates, each with its `code` and its names in Spanish and English. Requests always use these
**codes** (for example `colorCodes: ["rojo"]`, `shapeCode`, `styleCode`), never database ids.

### Catalog administration (administrators)
| Method | Path | Description |
|---|---|---|
| GET | `/api/admin/catalogs/{catalog}` | Every entry, active or not |
| POST | `/api/admin/catalogs/{catalog}` | `{ code, nameEs, nameEn, sortOrder?, active?, hex? (colors), imageUrl (avatars, https) }` → 201 (409 if the code exists) |
| PUT | `/api/admin/catalogs/{catalog}/{code}` | Edit names, order, extra fields or `active`; the code never changes |

Store templates: `GET/POST /api/admin/store-templates` and `PUT /api/admin/store-templates/{code}` with
`{ code, nameEs, nameEn, descriptionEs?, descriptionEn?, previewUrl?, defaultLayout?, defaultTheme?, active? }`
(the layout and theme are JSON objects).

Budget ranges: `GET/POST /api/admin/catalogs/budget-ranges` and `PUT/DELETE .../budget-ranges/{code}` with
`{ code, minAmount, maxAmount?, currency }`; a range used by a taste profile cannot be deleted (409).

`{catalog}`: `colors`, `shapes`, `motifs`, `styles`, `avatars`. Entries are never deleted: `active=false` hides them from `/api/catalogs` and from new
requests.

### Authentication
Public endpoints; every other call sends `Authorization: Bearer <accessToken>`. The access token lasts
`JWT_ACCESS_MINUTES` (15); the refresh token lasts `JWT_REFRESH_DAYS` (7) and works only once.

| Method | Path | Body → answer |
|---|---|---|
| POST | `/api/auth/register` | `{ name, email, password (8-72), countryCode?, preferredLanguage? }` → 201 `AuthResponse`; sends the verification email |
| POST | `/api/auth/register-artisan` | `{ account: { name, email, password, countryCode?, preferredLanguage? }, workshop?, city?, phone?, biographyEs?, biographyEn? }` → 201 `AuthResponse`; creates the master profile and its 5 verification steps |
| POST | `/api/auth/login` | `{ email, password }` → `AuthResponse` (401 wrong credentials, 403 suspended, 429 too many failures) |
| POST | `/api/auth/refresh` | `{ refreshToken }` → new `AuthResponse` (a reused refresh token closes every session) |
| POST | `/api/auth/logout` | `{ refreshToken }` → 204 |
| POST | `/api/auth/verify-email` | `{ token }` → `UserResponse`; the account becomes active |
| POST | `/api/auth/forgot-password` | `{ email }` → 202 (same answer whether the email exists or not) |
| POST | `/api/auth/reset-password` | `{ token, newPassword }` → 204; users migrated from v1 create their first password this way |

`AuthResponse` = `{ tokenType: "Bearer", accessToken, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt, user }`.

### Access by role
| Who | What |
|---|---|
| Anyone | `/api/auth/**`; `GET` of health, gallery, stores (`/api/stores`), masters, artworks, auctions and catalogs |
| Logged-in user | Their own account (`/api/users/{id}/**` with their id), notifications, interactions and bids in their own name |
| Artisan or administrator | Create and edit stores, artworks and photos (an artisan only their own store); suggested prices |
| Administrator | `/api/admin/**`, masters, creating and closing auctions, any user's data |

A wrong role answers 403 `access_denied`; a missing or expired token answers 401 `unauthorized`.

### Artisan verification (artisans)
Steps: `EMAIL` (approved by itself when the email is confirmed), `IDENTITY`, `WORKSHOP`, `SAMPLES` (evidence from
the artisan) and `ADMIN_APPROVAL` (the administrator's final decision).

| Method | Path | Description |
|---|---|---|
| GET | `/api/artisans/me/verification` | `{ masterId, name, verificationStatus, steps: [{ verificationId, step, status, evidenceUrl, notes, reviewedAt }] }` |
| PUT | `/api/artisans/me/verification/{step}` | `{ evidenceUrl }` (https) for `IDENTITY`, `WORKSHOP` or `SAMPLES` → step `SUBMITTED` |
| POST | `/api/artisans/me/verification/{step}/evidence` | Multipart `file` (JPEG/PNG/WebP) uploaded to the photo storage (503 while disabled) |

### Store editor (artisans)
| Method | Path | Description |
|---|---|---|
| POST | `/api/artisans/me/store` | `{ templateCode, nameEs, nameEn, descriptionEs?, descriptionEn?, slug?, acceptsCustomOrders? }` → 201 draft store with the template theme and sections |
| GET | `/api/artisans/me/store` | The artisan's store with every section |
| PATCH | `/api/artisans/me/store` | `{ nameEs?, nameEn?, descriptionEs?, descriptionEn?, slug?, logoUrl?, bannerUrl?, theme?, acceptsCustomOrders? }` (409 if the slug is taken) |
| POST | `/api/artisans/me/store/sections` | `{ sectionType, titleEs?, titleEn?, contentEs?, contentEn?, imageUrl?, config? }` → 201, added at the end |
| PUT | `/api/artisans/me/store/sections/{sectionId}` | Replace the content of a section |
| PATCH | `/api/artisans/me/store/sections/{sectionId}/visibility` | `{ visible }` hides or shows it on the public page |
| POST | `/api/artisans/me/store/publish` | Publish the store; the database rejects it while the master is not approved (409) |
| PUT | `/api/artisans/me/store/sections/order` | `{ sectionIds: [...] }` every section once, top to bottom (422 otherwise) |

### Artworks (artisans)
| Method | Path | Description |
|---|---|---|

### Artisan verification (administrators)
| Method | Path | Description |
|---|---|---|
| GET | `/api/admin/verifications` | Masters waiting for a decision with their open steps (view `v_pending_verifications`) |
| PUT | `/api/admin/verifications/{verificationId}` | `{ decision: APPROVED \| REJECTED, notes? }` for `IDENTITY`, `WORKSHOP` or `SAMPLES`; the artisan is notified |
| POST | `/api/admin/artisans/{masterId}/approve` | Final approval when the other four steps are approved (409 naming the missing ones): master `APPROVED`, account `ACTIVE`, notification |
| GET | `/api/artisans/me/artworks` | Every artwork of the store, drafts included |
| POST | `/api/artisans/me/artworks` | `{ titleEs, titleEn, historyEs?, historyEn?, technique?, widthCm?, heightCm?, depthCm?, weightG?, saleMode?, price?, currency?, stock?, colorCodes?, motifCodes?, shapeCode?, styleCode? }` → 201 `DRAFT` |
| PUT | `/api/artisans/me/artworks/{artworkId}` | Replace the data of an artwork (the status does not change) |

### Notifications (logged-in user)
| Method | Path | Description |
|---|---|---|
| GET | `/api/notifications?unreadOnly=false` | Latest 100 notifications, newest first, in the user's language |
| POST | `/api/notifications/{id}/read` | Mark one as read (another user's notification answers 404) |
| POST | `/api/notifications/read-all` | Mark all as read: `{ "updated": n }` |

### Errors
Every error has the same shape:
```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "code": "artwork_not_found",
  "message": "Artwork 99 not found", "path": "/api/artworks/99" }
```
`code` is stable (see `ErrorCode`): the frontend decides by it. `message` is text for people, in the language
of the logged-in user, or else of the `Accept-Language` header (`es` or `en`; Spanish by default). The texts live in
`messages_es.properties` and `messages_en.properties`.
400 invalid input (with `fieldErrors`), 404 not found, 409 business rule or database constraint,
422 invalid bid / business validation, 429 too many bids, 503 integration disabled.

## WebSocket contract (STOMP)

- Endpoint: `/ws` with SockJS (`new SockJS(BASE_URL + "/ws")`); native WebSocket clients use `/ws/websocket`.
- **Authentication:** the `CONNECT` frame must carry the access token in a STOMP header:
  `client.connect({ Authorization: "Bearer " + accessToken }, onConnected)` (with `@stomp/stompjs`:
  `connectHeaders: { Authorization: "Bearer " + accessToken }`). Without a valid token the server answers with
  an `ERROR` frame and closes the session; reconnect with a fresh token after `/api/auth/refresh`.
- **Subscriptions:** `/topic/auctions/{auctionId}` for every connected user; `/topic/users/{userId}/notifications`
  only for that same user; `/topic/admin/**` only for administrators. Topics are read only: `SEND` frames are
  refused.
- Application prefix: `/app`. Broker prefix: `/topic`.

| Topic | Payload |
|---|---|
| `/topic/auctions/{auctionId}` | `AuctionEvent { type: BID_PLACED \| AUCTION_STARTED \| AUCTION_FINISHED \| AUCTION_CANCELLED, auctionId, artworkId, bid?, previousTopBidderId?, timestamp }` |
| `/topic/users/{userId}/notifications` | Stored notifications `{ notificationId, type, title, body, link, payload, readAt, createdAt }` (title and body in the user's language); the auction observers still send `{ type: "OUTBID" \| "MATCHING_AUCTION", auctionId, artworkId, message }` |
| `/topic/admin/anomalies` | `{ auctionId, bidId, userId, amount, reason, timestamp }` |

Bids are placed through REST (`POST /api/auctions/{id}/bids`); WebSocket is used to receive updates.

**Reconnect automatically.** On Render's free plan the service sleeps after 15 minutes without traffic
and takes about a minute to wake up; open WebSocket connections are dropped. The frontend must
reconnect (for example with `@stomp/stompjs` `reconnectDelay`) and re-subscribe.

## Deployment on Render

1. Create a **Web Service** from this repository, runtime **Docker**, region **Virginia (US East)**
   (the same region as the database, required for the internal URL).
2. Set the environment variables above (use the **internal** database URL, without `sslmode`).
3. Health check path: `/api/health`.
4. Set `TRUSTED_PROXY_HOPS=1`: Render's proxy appends the client address to `X-Forwarded-For`, and only that
   entry is trusted for the login attempt limit (earlier entries can be forged by the client). Check it on the
   first deploy.

### First administrator

The backend never creates administrators and **no migration may contain a password or a password hash**.
To create the first one:

1. The person signs up through the API like any customer (`POST /api/auth/register`). Their password only
   exists as the BCrypt hash the backend stores.
2. The database owner promotes the account with a manual `UPDATE`, run directly on the database and **never
   committed** to this repository:
   ```sql
   UPDATE museo.users SET role = 'administrador' WHERE lower(email) = lower('<their email>');
   ```
3. The person logs in again: the new role travels in the next access token.

The free plan has 512 MB of RAM (hence the JVM flags) and an ephemeral disk (uploaded files are never
stored on the server). Read requests never change data: auction statuses are synced by the scheduler and
by write actions (bid, start, finish, cancel), so after the service wakes up the scheduler catches up on
its next run. Historical auctions without currency are frozen: neither the scheduler nor any automatic
action changes their status.

## Integrations status

| Integration | Status | How to enable |
|---|---|---|
| AI service (price suggestions, recommendation strategy `ai-service`) | Enabled when configured | Set `AI_SERVICE_URL` and `AI_SERVICE_KEY` |
| AI text model (recommendation strategy `ai`, AI bid review) | Disabled – provider not chosen | When the provider is chosen: add an adapter implementing `AiTextClient`, add the `AI_API_KEY` property, and set `AI_PROVIDER` |
| Payments (by currency: COP → Wompi, USD → PayPal; simulated gateway for demos) | Each gateway enabled when its keys are set | Set the `WOMPI_*` and `PAYPAL_*` variables; any other currency answers 409 |
| Photo storage (Cloudinary, JPEG/PNG/WebP up to 5 MB) | Enabled when configured | Set `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` and `CLOUDINARY_API_SECRET` |

While an integration is disabled its endpoints answer **503** and nothing is invented: recommendations
use the deterministic strategies and bids are reviewed with deterministic rules.
