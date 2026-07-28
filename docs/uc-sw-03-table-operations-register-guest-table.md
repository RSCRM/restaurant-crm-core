# UC-SW-03 - Register Guest Table

## Objective

Register a walk-in guest at an available table before an order is created.

## Actors

- Branch manager, cashier, or waiter with `TABLE_SESSION_CREATE`.
- The request uses a context token. The branch is read from the token.

## Business Rules

1. The branch in the context token must exist and be active.
2. The selected table must belong to that branch.
3. Only an `AVAILABLE` table can receive a new guest.
4. A table can have at most one `ACTIVE` table session.
5. Guest name is required.
6. Party size must be positive and cannot exceed table capacity.
7. Guest phone is optional; when provided it must contain 9-15 digits.
8. Registration creates an `ACTIVE` table session and changes the table to `OCCUPIED` in one transaction.
9. The table row is locked during registration to prevent concurrent double registration.

## API Contract

### Register guest

`POST /api/v1/table-sessions`

```json
{
  "tableId": "table-id",
  "guestName": "Nguyen Van A",
  "guestPhone": "0901234567",
  "partySize": 4,
  "note": "Window seat"
}
```

The response returns the session, table, guest information, status, and start time.

## Data Design

`table_sessions`

- `table_session_id`: primary key.
- `branch_id`: owning branch from the context token.
- `table_id`: current table.
- `guest_name`, `guest_phone`, `party_size`, `note`: guest information.
- `status`: `ACTIVE` or `CLOSED`.
- `started_at`, `ended_at`: session lifecycle.
- Base audit and optimistic locking columns.

Hibernate code-first creates or updates the table. No Flyway migration is added because the project has no agreed migration baseline.

## Acceptance Criteria

- A valid request creates one active session and occupies the table.
- A table outside the token branch is not accessible.
- Occupied/reserved tables and duplicate active sessions are rejected.
- Invalid party size and phone values are rejected.
- Concurrent requests cannot create two active sessions for the same table.
