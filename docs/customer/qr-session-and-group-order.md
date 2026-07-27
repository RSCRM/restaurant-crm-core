# QR Session & Group Ordering (uc-c-02)

Contract shared by uc-c-03…06 and the Manager QR module. Covers the **two QR token
types**, the **Redis session schema**, the **CUSTOMER_SESSION token**, and the
**`bindOrder` contract** used by uc-c-05.

> Design pivot: **a session is NOT an order.** The session lives in Redis and is created
> as soon as OTP passes; the DB `Order` is created later (uc-c-05) via the existing
> `OrderService.create()` when the first item is sent to the kitchen. This lets members
> join immediately and keeps the `erp/order` module untouched.

---

## 1. Two QR token types

Both are `SignedJWT` + `HS512` + HMAC (same precedent as `AttendanceServiceImpl`), signed
with `security.qr.signer-key` (falls back to `security.jwt.signer-key`).

### 1a. TABLE QR — static, printed on the table

| Property | Value |
| :-- | :-- |
| `type` | `TABLE_QR` |
| Secret | `HMAC(qrSignerKey, "TABLE_QR:" + organizationId + ":" + branchId)` — stable, never rotated |
| `exp` | none (printed on paper) |
| Claims | `type`, `organizationId`, `branchId`, `tableId`, `qrVersion`, `iat` |
| Revocation | bump `qrVersion` (Manager re-generates → old prints invalid, FR-04) |

- Uses `tableId` (UUID), **never** `tableNumber` (only unique within an area).
- Generation belongs to the Manager module (uc-m-*); this module only exposes
  `TableQrTokenService.generate(...)`.
- `qrVersion` needs column `qr_version INT NOT NULL DEFAULT 1` on `restaurant_tables`
  (owned by the table module). Until it exists, verify **accepts any version** and logs
  a debug line — see the `TODO(uc-c-02)` in `TableQrTokenServiceImpl.verify`.

`TableQrPayload = (organizationId, branchId, tableId, qrVersion)`.

### 1b. GROUP QR — dynamic, session-bound, short-lived

| Property | Value |
| :-- | :-- |
| `type` | `GROUP_QR` |
| Secret | `HMAC(qrSignerKey, "GROUP_QR:" + sessionId)` |
| `exp` | **required**, default 30 min (`GROUP_QR_TTL_SECONDS`) |
| Claims | `type`, `organizationId`, `branchId`, `tableId`, `sessionId`, `iat`, `exp` |
| Revocation | session closed / order `PAID` → Redis keys gone → verify + liveness fail |

> ⚠️ **Bearer capability.** A GROUP QR can be screenshotted and forwarded off-premise.
> Mitigations: short `exp` (owner can `refresh`), liveness re-check against Redis on every
> join, member cap (`MAX_MEMBERS = 12` → `TQR_1009`), and an **anti-splice** check that the
> token's `branchId`/`tableId`/`organizationId` match the session hash (`TQR_1004`).

`GroupQrPayload = (organizationId, branchId, tableId, sessionId)`.

---

## 2. Redis schema (all keys TTL = `SESSION_TTL_SECONDS` = 4h)

| Key | Type | Value |
| :-- | :-- | :-- |
| `qr:table:{branchId}:{tableId}` | String | `sessionId` (owner-election pointer, set via **SETNX**) |
| `qr:session:{sessionId}` | Hash | `organizationId, branchId, tableId, ownerDeviceId, ownerCustomerId, ownerCustomerPhone, orderId, status, createdAt` |
| `qr:session:{sessionId}:members` | Hash | `deviceId` → JSON(`QrSessionMember`) |
| `qr:order:{orderId}` | String | `sessionId` (reverse pointer, written by uc-c-05) |

- Prefixes live in `RedisConstants`; keys are built by `RedisKeyGenerator`.
- Only `QrSessionRedisRepository` talks to Redis — services never touch `StringRedisTemplate`.
- `heartbeat` extends the TTL of **all four** keys.
- `status` ∈ `OPEN | LOCKED_FOR_PAYMENT | CLOSED` (`QrSessionStatus`).
- `orderId` starts null; filled by `bindOrder` (see §4).

`QrSessionMember = (deviceId, role, customerId, customerPhone, joinedAt, lastSeenAt)`,
`role` ∈ `OWNER | MEMBER` (`SessionMemberRole`). Owner is the SETNX winner; everyone joining
via GROUP QR is a member. Only the OWNER may finalize the order (BR-CST-GRP-02, uc-c-05).

---

## 3. CUSTOMER_SESSION token (the third token type)

- `type = CUSTOMER_SESSION`, signed with `security.jwt.signer-key` so the existing
  `jwtDecoder` validates it.
- Claims: `type`, `organizationId`, `branchId`, `tableId`, `sessionId`, `deviceId`, `sessionRole`.
- `SecurityConfig` maps it to a single authority `ROLE_CUSTOMER_SESSION`; customer endpoints
  guard with `@PreAuthorize("hasRole('CUSTOMER_SESSION')")`.
- Two-way isolation: a `CUSTOMER_SESSION` token cannot call staff endpoints (no staff
  authorities), and `IDENTITY`/`CONTEXT` tokens cannot call `/customer/qr/**` (no
  `ROLE_CUSTOMER_SESSION`).

---

## 4. `bindOrder` contract (for uc-c-05)

```java
qrSessionService.bindOrder(String sessionId, String orderId);
```

Call **right after** `OrderService.create()` returns. It:

1. writes `orderId` into `qr:session:{sessionId}` hash, and
2. writes `qr:order:{orderId}` → `sessionId` (reverse lookup),

with TTL refreshed on both. **Idempotent**: re-binding the same `orderId` is a no-op;
binding a *different* `orderId` to a session that already has one throws `TQR_1004`.

---

## 5. Endpoints

| Method & path | Auth | Purpose |
| :-- | :-- | :-- |
| `POST /api/v1/public/customer/qr/resolve` | public | verify TABLE QR, show table info (no Redis write) |
| `POST /api/v1/public/customer/qr/session` | public | OWNER opens a session (after OTP) |
| `POST /api/v1/public/customer/qr/session/join` | public | MEMBER joins via GROUP QR |
| `GET  /api/v1/customer/qr/session` | CUSTOMER_SESSION | read current session |
| `POST /api/v1/customer/qr/session/group-qr/refresh` | CUSTOMER_SESSION (OWNER) | issue a fresh GROUP QR |
| `POST /api/v1/customer/qr/session/heartbeat` | CUSTOMER_SESSION | keep alive + extend TTL |

`deviceId` is always generated **server-side** (`UUID`), never accepted from the client.
`organizationId`/`branchId`/`tableId`/`sessionId` always come from a verified token, never
from request params (NFR-07).

---

## 6. Error codes (`TQR_*`)

| Code | HTTP | Meaning |
| :-- | :-- | :-- |
| `TQR_1000` | 400 | token malformed |
| `TQR_1001` | 401 | signature mismatch |
| `TQR_1002` | 400 | required claim missing |
| `TQR_1003` | 409 | qrVersion outdated (reserved; not yet enforced) |
| `TQR_1004` | 403 | context mismatch / token splice |
| `TQR_1005` | 404 | table not in branch |
| `TQR_1006` | 404 | session not found / closed |
| `TQR_1007` | 401 | session expired |
| `TQR_1008` | 409 | session locked for payment |
| `TQR_1009` | 409 | member limit reached |
| `TQR_1010` | 500 | token generation failed |
| `TQR_1011` | 409 | table already has a session (not the owner) |
| `TQR_1012` | 401 | group QR expired |
| `TQR_1013` | 403 | not the session owner |
| `TQR_1014` | 401 | OTP ticket invalid |

> The project's `GlobalExceptionHandler` returns HTTP **200** with the failure encoded in
> the `ApiResponse.errorMessage`; the HTTP column above is the semantic status carried by
> each `ErrorCode`.

---

## 7. Open questions for the team (not decided here)

1. `Order` has no `customerId` FK (only `customerPhone`). Is the phone-string ref enough, or
   is a `customer_id` column needed (affects uc-c-08 voucher lookup)?
2. Do MEMBERs identify themselves (own phone/OTP) or do points always go to the OWNER?
3. Who sets `LOCKED_FOR_PAYMENT`, and is an `OrderStatus.PENDING_PAYMENT` needed (BR-CST-PAY-01)?
4. Loyalty wallet scope: per-chain or per-branch? `OrderServiceImpl.create()` currently
   initializes the wallet per **branch**.
5. Who adds `qr_version` to `restaurant_tables`, and when?
6. Is a 30-min GROUP QR TTL right for long sittings, and who may refresh it?
7. Who closes the session when the invoice is `PAID` (BR-CST-GRP-04) — uc-c-05, invoice, or an
   `OrderStatus` listener?

---

# OTP identification (uc-c-03)

Phone + OTP is the gate before opening a session: it produces the `otpTicket` that
`POST /public/customer/qr/session` (uc-c-02) consumes.

```
Scan TABLE QR
  → POST /public/customer/otp/request  { qrToken, customerPhone }            → sends a 6-digit OTP
  → POST /public/customer/otp/verify   { qrToken, customerPhone, otpCode }   → returns otpTicket
  → POST /public/customer/qr/session   { qrToken, customerPhone, otpTicket } → opens the session (uc-c-02)
```

## Module layout (no cross-module cycle)

- `crm/customer_account` owns the OTP domain and is **QR-agnostic** — it imports nothing from erp.
  `CustomerOtpService` takes `branchId`/`tableId` as already-trusted parameters:
  ```java
  OtpRequestResult request(String customerPhone, String branchId, String tableId);
  String           verify (String customerPhone, String branchId, String tableId, String otpCode);
  ```
- `erp/order` is the composition layer: `CustomerOtpController` verifies the TABLE QR (erp), then
  delegates to `CustomerOtpService` (crm). The uc-c-02 plug point `OtpTicketVerifierImpl`
  (`@Profile("!dev")`) also lives here and calls back into `OtpTicketService` (crm).

## Redis schema (uc-c-03)

| Key | Type | Value | TTL |
| :-- | :-- | :-- | :-- |
| `otp:code:{phone}` | Hash | `codeHmac, attempts, issuedAt, branchId, tableId` | 180s |
| `otp:lock:{phone}` | String | `1` | 900s |
| `otp:resend:{phone}` | String | `1` | 60s |
| `otp:table:{branchId}:{tableId}` | String | counter (`INCR`) | 3600s |

- The OTP code is **never** stored raw: `codeHmac = HMAC-SHA256(otpSignerKey, phone + ":" + code)`
  — the phone is mixed in so each code has its own hash space (a Redis dump can't be table-attacked).
- Wrong attempts are counted with `HINCRBY`, the per-table rate limit with `INCR` — atomic, never
  read-modify-write. Verify compares with `MessageDigest.isEqual` (constant-time).
- BR-CST-ACC-02: 180s validity, 3 wrong attempts → invalidate + lock the phone 900s, resend
  cooldown 60s, ≤ 20 requests/hour/table. The code is never returned or logged (except the dev sender).

## OTP ticket payload

Signed with `security.otp.signer-key` (falls back to `security.jwt.signer-key`).

| Property | Value |
| :-- | :-- |
| `type` | `OTP_TICKET` |
| Secret | `HMAC(otpSignerKey, "OTP_TICKET:" + branchId)` |
| `exp` | 5 min |
| Claims | `type`, `customerPhone` (normalized), `branchId`, `tableId`, `jti`, `iat`, `exp` |

`OtpTicketPayload = (customerPhone, branchId, tableId)` — no `organizationId` (branch implies it;
the uc-c-02 verifier only matches phone/branch/table).

> 🔒 The uc-c-02 `OtpTicketVerifier` takes `isValid(customerPhone, branchId, tableId, otpTicket)`:
> the ticket is bound to branch **and** table, so a ticket earned at table A cannot open table B or
> another branch (NFR-07). The verifier normalizes the incoming phone before comparing.

## Error codes (`OTP_*`)

| Code | HTTP | Meaning |
| :-- | :-- | :-- |
| `OTP_1000` | 400 | wrong code |
| `OTP_1001` | 410 | code expired / not found |
| `OTP_1002` | 429 | too many wrong attempts (phone locked) |
| `OTP_1003` | 429 | phone temporarily locked |
| `OTP_1004` | 429 | resend requested too soon |
| `OTP_1005` | 429 | per-table rate limit |
| `OTP_1006` | 502 | send failed (no provider wired) |
| `OTP_1007` | 403 | customer account locked |
| `OTP_1008` | 403 | OTP requested for a different table |
| `OTP_1009` | 500 | ticket generation failed |

## Sender (not wired yet)

`OtpSender` has two profile-scoped beans: `LogOtpSender` (`@Profile("dev")`, logs the code for local
testing) and `NoopOtpSender` (`@Profile("!dev") @Primary`, throws `OTP_SEND_FAILED`). A real provider
(Zalo ZNS / SMS gateway via RabbitMQ, SRS §IV) is a TODO and not in `pom.xml` yet.

---

# Shared group cart & order submission (uc-c-05)

The whole table shares one Redis-backed cart; only the OWNER submits; every item funnels into one
DB order via the existing `OrderService.create()` (no order logic is re-implemented here).

## Redis schema (all TTL = `SESSION_TTL_SECONDS`, on the `qr:session:{sessionId}` namespace)

| Key | Type | Value |
| :-- | :-- | :-- |
| `qr:session:{sessionId}:cart` | Hash | `cartItemId` → JSON(`GroupCartItem`) |
| `qr:session:{sessionId}:cart:lock:{cartItemId}` | String | `deviceId` (item edit lock, TTL 30s) |
| `qr:session:{sessionId}:cart:submitting` | String | `deviceId` (submit guard, TTL 30s) |

`GroupCartItem = (cartItemId, productId, comboId, quantity, note, modifierOptionIds, addedByDeviceId, addedAt)`.
**No price is stored** — display prices are read live from `Product`/`Combo`; the authoritative price
is resolved by `OrderServiceImpl.resolveUnitPrice()` at submit. Same-item-different-note stays as two
separate lines (never merged).

## Endpoints (all require a CUSTOMER_SESSION token)

| Method | Path | Who | Purpose |
| :-- | :-- | :-- | :-- |
| GET | `/api/v1/customer/cart` | any member | view cart + tentative subtotal |
| POST | `/api/v1/customer/cart/items` | any member | add item |
| PUT | `/api/v1/customer/cart/items/{cartItemId}` | any member | edit qty/note/modifiers |
| DELETE | `/api/v1/customer/cart/items/{cartItemId}` | any member | remove item |
| POST | `/api/v1/customer/cart/items/{cartItemId}/lock` | any member | hold edit lock |
| DELETE | `/api/v1/customer/cart/items/{cartItemId}/lock` | lock holder | release lock |
| POST | `/api/v1/customer/cart/submit` | **OWNER only** | submit → one order |
| GET | `/api/v1/customer/cart/subscribe` | any member | SSE cart updates |

`branchId`/`tableId`/`sessionId`/`deviceId` always come from the token (NFR-07). OWNER is
re-checked from the Redis member hash at submit — the token claim is never trusted.

## Item locking (BR-CST-GRP-03)

`SETNX` on the lock key (value = deviceId, TTL 30s). Editing/deleting a line held by another device
→ `CART_1003`; releasing a lock you don't hold → `CART_1004`. TTL is the self-heal: a device that
closes mid-edit frees the line automatically.

## Submit flow (duplicate-safe)

1. `SETNX` submit-guard first — else `CART_1005`; released in `finally`.
2. session OPEN, else `TQR_1008`/`TQR_1006`.
3. OWNER only (from Redis) — else `TQR_1013`.
4. empty cart → `CART_1000`.
5. any line locked by another device → `CART_1003`.
6. re-validate availability: drop unavailable lines, broadcast `CART_ITEM_REMOVED`, then `CART_1002`.
7. build `CreateOrderRequestDto` (`orderType = DINE_IN`, `customerPhone = ownerCustomerPhone`, `customerName = null`).
8. `orderService.create(dto)` → prices, merges into the table's PENDING order (FR-09.1), broadcasts KDS.
9. `bindOrder(sessionId, orderId)`.
10. clear cart + all locks; broadcast `CART_SUBMITTED`.

Calling submit again in the same session merges into the **same** `orderId` (the merge branch in
`create()`), so ordering more rounds during the meal is one order.

## Host handover (BR-CST-GRP-04, lazy)

Checked on every request (no `@Scheduled`): if the OWNER's `lastSeenAt` is older than
`HOST_IDLE_TIMEOUT_SECONDS` (10 min), the earliest-joined still-active MEMBER becomes OWNER,
`ownerDeviceId` is moved, and `SESSION_HOST_CHANGED` is broadcast. No active member → left as is.

## SSE events (`GroupCartEventType`, keyed by sessionId)

`CART_UPDATED`, `CART_ITEM_REMOVED`, `CART_ITEM_LOCKED`, `CART_ITEM_UNLOCKED`, `CART_SUBMITTED`,
`SESSION_HOST_CHANGED`.

## Error codes (`CART_*`)

`CART_1000` empty · `CART_1001` item not found · `CART_1002` unavailable · `CART_1003` locked ·
`CART_1004` lock not held · `CART_1005` submit in progress · `CART_1006` menu item not in branch ·
`CART_1007` modifier invalid · `CART_1008` member not found · `CART_1009` cart item request invalid
(added for Bean-Validation messages, since `GlobalExceptionHandler` maps every validation message
through `ErrorCode.valueOf`).

## Known gaps (out of uc-c-05 scope)

- **Nobody closes the session/cart on invoice `PAID`** — the payment hook lives in the `invoice`
  module; today the session just expires via TTL. Needs an `OrderStatus`/invoice listener.
- **`LOCKED_FOR_PAYMENT` is never set by anyone yet** — `OrderStatus` has no "paying" state, so
  BR-CST-PAY-01 is only partially wired (the cart honors the status if something sets it).
- **SSE latency** for item locking; WebSocket would be better but `pom.xml` has none.
- `customerName` submitted as `null` (Customer has only phone + status); order `note` is null (notes
  are per line).
