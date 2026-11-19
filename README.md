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
- Masters and rooms **cannot be deleted** through the API (the database cascades deletes).
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
| `FLYWAY_ENABLED` | Run the Flyway migrations on start (baseline 1 on an existing v1 database, then V2) | `true` |
| `PORT` | HTTP port (set by Render) | `8080` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins (REST and WebSocket) | `http://localhost:5173,http://localhost:3000` |
| `AUCTION_CURRENCY` | Currency of new auctions | `USD` |
| `AUCTION_SCHEDULER_ENABLED` | Run the 60-second auction scheduler | `true` |
| `AI_PROVIDER` | AI provider (not chosen yet; `none` disables AI) | `none` |
| `AI_API_KEY` | Not used yet: it will be added together with the adapter when the AI provider is chosen | – |
| `STORAGE_PROVIDER` | Photo storage provider (not chosen yet) | `none` |
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
| GET | `/api/gallery` | Full tree Gallery → Stores → Artworks (Composite) |
| GET | `/api/masters` | Masters |
| GET | `/api/masters/{id}` | Master + its store |
| POST / PUT | `/api/masters`, `/api/masters/{id}` | Create / update master |
| GET | `/api/rooms` | Stores with master and artwork count (legacy path) |
| GET | `/api/rooms/{id}` | Store + artworks (legacy path) |
| POST / PUT | `/api/rooms`, `/api/rooms/{id}` | Create / update store `{ masterId, templateId, nameEs, nameEn, … }` (one store per master) |
| GET | `/api/artworks?storeId=&status=` | Filterable list |
| GET | `/api/artworks/{id}` | Artwork + photos + current auction |
| POST / PUT | `/api/artworks`, `/api/artworks/{id}` | Create / update artwork (status is managed by auctions) |
| GET | `/api/artworks/{id}/photos` | Photos |
| POST | `/api/artworks/{id}/photos` | Register photo by URL `{ fileUrl, angle }` |
| POST | `/api/artworks/{id}/photos/upload` | Multipart upload (503 while storage is disabled) |
| DELETE | `/api/photos/{id}` | Delete a photo |

### Users
| Method | Path | Description |
|---|---|---|
| POST | `/api/users/identify` | `{ email, name, countryCode?, preferredLanguage }` → existing user (200) or new one (201), with `isNew` |
| GET | `/api/users/{id}` | User |
| PATCH | `/api/users/{id}/preferences` | Change the preferred language |
| GET / PUT | `/api/users/{id}/taste-profile` | Read / save the initial questionnaire as catalog codes `{ colorCodes, shapeCodes, styleCodes, motifCodes, budgetRangeCode }` |

### Interactions and recommendations
| Method | Path | Description |
|---|---|---|
| POST | `/api/interactions` | `{ userId, artworkId, action, durationSeconds }` – `VIEW` (duration required), `ZOOM`. `BID` interactions are recorded automatically |
| GET | `/api/users/{id}/interactions` | History |
| POST | `/api/users/{id}/recommendations?strategy=hybrid` | Compute (`profile`, `interactions`, `hybrid`, `ai`) and replace previous ones |
| GET | `/api/users/{id}/recommendations` | Last saved recommendations |

### Auctions and bids
| Method | Path | Description |
|---|---|---|
| GET | `/api/auctions?status=` | List with highest bid and bid count |
| GET | `/api/auctions/{id}` | Detail |
| POST | `/api/auctions` | `{ artworkId, basePrice?, startDate, endDate }` (Builder) |
| GET | `/api/artworks/{id}/suggested-price` | AI price suggestion (503 while AI is disabled) |
| POST | `/api/auctions/{id}/start` · `/finish` · `/cancel` | Lifecycle (State) |
| GET | `/api/auctions/{id}/bids` | Bids, highest first |
| POST | `/api/auctions/{id}/bids` | `{ userId, amount, currency }` |

Bid rules: only in `ACTIVE` auctions; `currency` must equal the auction currency (new auctions use
`AUCTION_CURRENCY`; historical auctions have none yet and answer 409); `amount` must be at least
max(highest bid, base price) + `app.auction.min-increment` (422 otherwise); more than 5 bids per minute
from the same visitor → 429; an amount above 10 × the reference is accepted but reported to
`/topic/admin/anomalies`.

### Contact
The v1 contact messages were migrated by the database to conversations and messages. The old
`/api/contact-messages` endpoints were removed; the conversation endpoints replace them.

### Errors
Every error has the same shape:
```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "message": "Artwork 99 not found", "path": "/api/artworks/99" }
```
400 invalid input (with `fieldErrors`), 404 not found, 409 business rule or database constraint,
422 invalid bid / business validation, 429 too many bids, 503 integration disabled.

## WebSocket contract (STOMP)

- Endpoint: `/ws` with SockJS (`new SockJS(BASE_URL + "/ws")`); native WebSocket clients use `/ws/websocket`.
- Application prefix: `/app`. Broker prefix: `/topic`.

| Topic | Payload |
|---|---|
| `/topic/auctions/{auctionId}` | `AuctionEvent { type: BID_PLACED \| AUCTION_STARTED \| AUCTION_FINISHED \| AUCTION_CANCELLED, auctionId, artworkId, bid?, previousTopBidderId?, timestamp }` |
| `/topic/users/{userId}/notifications` | `{ type: "OUTBID" \| "MATCHING_AUCTION", auctionId, artworkId, message }` |
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

The free plan has 512 MB of RAM (hence the JVM flags) and an ephemeral disk (uploaded files are never
stored on the server). Because the service sleeps, auction statuses are also synced every time an
auction is read or receives a bid, not only by the scheduler.

## Integrations status

| Integration | Status | How to enable |
|---|---|---|
| AI (recommendation strategy `ai`, price suggestions, AI bid review) | Disabled – provider not chosen | When the provider is chosen: add an adapter implementing `AiTextClient`, add the `AI_API_KEY` property, and set `AI_PROVIDER` |
| Photo storage | Disabled – provider not chosen | Add an adapter implementing `StorageService` and set `STORAGE_PROVIDER` |

While an integration is disabled its endpoints answer **503** and nothing is invented: recommendations
use the deterministic strategies and bids are reviewed with deterministic rules.
