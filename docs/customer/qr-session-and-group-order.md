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
