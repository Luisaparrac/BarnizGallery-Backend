# Barniz Gallery – Backend (MUSEO)

Backend of **MUSEO**, an immersive 3D virtual gallery of **Barniz de Pasto Mopa-Mopa**
(UNESCO Intangible Cultural Heritage, 2020). Visitors walk through rooms dedicated to six master
artisans, interact with the pieces in 3D, receive room recommendations, take part in live online
auctions and contact the masters.

This repository contains only the backend. The frontend (TypeScript + Three.js/Babylon.js) lives in a
separate repository and consumes the REST API and WebSocket contract described below.

Final project of the Software Design Patterns course. It implements 8 design patterns.

## Stack

| Topic | Choice |
|---|---|
| Language / build | Java 21, Maven Wrapper |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, WebSocket) |
| Database | PostgreSQL 18 on Render (existing schema, `ddl-auto=validate`) |
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
 PostgreSQL (Render)
```

Other packages: `entity/` (JPA entities), `enums/` (English enums), `converter/`
(Spanish DB values ↔ English enums), `dto/` (request and response records), `mapper/`,
`exception/` (global JSON error handler) and `config/`.

Important rules:

- The backend **never creates or changes tables** (`spring.jpa.hibernate.ddl-auto=validate`).
- There is **no seed data**: an empty table returns an empty list.
- Masters and rooms **cannot be deleted** through the API (the database cascades deletes).
- All times are **UTC**.

## Running locally

Requirements: Java 21 and access to the database.

1. Copy `src/main/resources/application-local.properties.example` to
   `src/main/resources/application-local.properties` (ignored by git) and fill in the external host,
   user and password of the Render database. Keep `app.auction.scheduler-enabled=false` there while
   developing against the shared database.
2. Run the tests (they never connect to the database):
   ```
   .\mvnw.cmd verify
   ```
3. Start the app:
   ```
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
   ```
4. Open http://localhost:8080/swagger-ui.html

## Environment variables

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | JDBC URL. On Render use the **internal** URL: `jdbc:postgresql://<INTERNAL_HOST>:5432/museo_hm45` | – |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials | – |
| `PORT` | HTTP port (set by Render) | `8080` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins (REST and WebSocket) | `http://localhost:5173,http://localhost:3000` |
| `AUCTION_CURRENCY` | Single auction currency | `USD` |
| `AUCTION_SCHEDULER_ENABLED` | Run the 60-second auction scheduler | `true` |
| `AI_PROVIDER` | AI provider (not chosen yet; `none` disables AI) | `none` |
| `AI_API_KEY` | Not used yet: it will be added together with the adapter when the AI provider is chosen | – |
| `HYPER3D_API_KEY` | Hyper3D Rodin key; empty disables 3D generation | empty |
| `HYPER3D_BASE_URL` | Hyper3D API base URL | `https://api.hyper3d.com` |
| `STORAGE_PROVIDER` | Photo storage provider (not chosen yet) | `none` |
| `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k` (already set in the Dockerfile) | – |

## REST API

All JSON. DTOs expose both languages (`titleEs`, `titleEn`, …) so the frontend chooses which one to show.
Dates are ISO-8601 (UTC). Enum values are in English (`EXHIBITED`, `ACTIVE`, `FIRST_PERSON`, …).

### System
| Method | Path | Description |
|---|---|---|
| GET | `/api/health` | `{ status, aiEnabled, hyper3dEnabled, storageEnabled }` |

### Catalog
| Method | Path | Description |
|---|---|---|
| GET | `/api/gallery` | Full tree Gallery → Rooms → Artworks (Composite) |
| GET | `/api/masters` | Masters |
| GET | `/api/masters/{id}` | Master + its room |
| POST / PUT | `/api/masters`, `/api/masters/{id}` | Create / update master |
| GET | `/api/rooms` | Rooms with master and artwork count |
| GET | `/api/rooms/{id}` | Room + artworks |
| POST / PUT | `/api/rooms`, `/api/rooms/{id}` | Create / update room (one room per master) |
| GET | `/api/artworks?roomId=&status=` | Filterable list |
| GET | `/api/artworks/{id}` | Artwork + photos + 3D model + current auction |
| POST / PUT | `/api/artworks`, `/api/artworks/{id}` | Create / update artwork (status is managed by auctions) |
| GET | `/api/artworks/{id}/photos` | Photos |
| POST | `/api/artworks/{id}/photos` | Register photo by URL `{ fileUrl, angle }` |
| POST | `/api/artworks/{id}/photos/upload` | Multipart upload (503 while storage is disabled) |
| DELETE | `/api/photos/{id}` | Delete a photo |

### 3D models
| Method | Path | Description |
|---|---|---|
| GET | `/api/artworks/{id}/model` | 3D model (404 if none) |
| POST | `/api/artworks/{id}/model/generate` | Send photos to Hyper3D (needs ≥ 1 photo with an absolute URL; 503 if disabled; 409 if already completed) |
| POST | `/api/artworks/{id}/model/refresh` | Check Hyper3D and save the GLB URL when done |

### Visitors
| Method | Path | Description |
|---|---|---|
| POST | `/api/visitors/identify` | `{ email, name, country, preferredLanguage, cameraMode }` → existing visitor (200) or new one (201), with `isNew` |
| GET | `/api/visitors/{id}` | Visitor |
| PATCH | `/api/visitors/{id}/preferences` | Change language and/or camera mode |
| GET / PUT | `/api/visitors/{id}/taste-profile` | Read / save the initial questionnaire |

### Interactions and recommendations
| Method | Path | Description |
|---|---|---|
| POST | `/api/interactions` | `{ visitorId, artworkId, action, durationSeconds }` – `VIEW` (duration required), `TOUCH`, `ROTATE`. `BID` interactions are recorded automatically |
| GET | `/api/visitors/{id}/interactions` | History |
| POST | `/api/visitors/{id}/recommendations?strategy=hybrid` | Compute (`profile`, `interactions`, `hybrid`, `ai`) and replace previous ones |
| GET | `/api/visitors/{id}/recommendations` | Last saved recommendations |

### Auctions and bids
| Method | Path | Description |
|---|---|---|
| GET | `/api/auctions?status=` | List with highest bid and bid count |
| GET | `/api/auctions/{id}` | Detail |
| POST | `/api/auctions` | `{ artworkId, basePrice?, startDate, endDate }` (Builder) |
| GET | `/api/artworks/{id}/suggested-price` | AI price suggestion (503 while AI is disabled) |
| POST | `/api/auctions/{id}/start` · `/finish` · `/cancel` | Lifecycle (State) |
| GET | `/api/auctions/{id}/bids` | Bids, highest first |
| POST | `/api/auctions/{id}/bids` | `{ visitorId, amount, currency }` |

Bid rules: only in `ACTIVE` auctions; `currency` must equal `AUCTION_CURRENCY`; `amount` must be at least
max(highest bid, base price) + `app.auction.min-increment` (422 otherwise); more than 5 bids per minute
from the same visitor → 429; an amount above 10 × the reference is accepted but reported to
`/topic/admin/anomalies`.

### Contact
| Method | Path | Description |
|---|---|---|
| POST | `/api/contact-messages` | `{ visitorId, masterId, artworkId?, content }` – the artwork must belong to the master's room |
| GET | `/api/masters/{id}/messages` | Messages received by a master |

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
| `/topic/visitors/{visitorId}/notifications` | `{ type: "OUTBID" \| "MATCHING_AUCTION", auctionId, artworkId, message }` |
| `/topic/admin/anomalies` | `{ auctionId, bidId, visitorId, amount, reason, timestamp }` |

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
| Hyper3D Rodin (photos → GLB) | Implemented, disabled without key | Set `HYPER3D_API_KEY`. Photos must have absolute public URLs. The GLB URL returned by Hyper3D may be temporary |
| Photo storage | Disabled – provider not chosen | Add an adapter implementing `StorageService` and set `STORAGE_PROVIDER` |

While an integration is disabled its endpoints answer **503** and nothing is invented: recommendations
use the deterministic strategies and bids are reviewed with deterministic rules.
