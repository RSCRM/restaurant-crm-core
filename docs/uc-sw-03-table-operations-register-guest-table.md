# UC-SW-03 - Table operations: Register guest table

## Business analysis

- **Actor:** Branch manager or staff with `TABLE_MANAGE` or `ORDER_CREATE`.
- **Goal:** Register a walk-in guest at an available table before an order is created.
- **Preconditions:** The context token contains a valid branch; the branch is active.
- **Postcondition:** An active table session is created and the table becomes occupied.

## Main flow

1. The employee selects an available table and enters guest information.
2. The system resolves the branch from the context token.
3. The system locks and validates the selected table.
4. The system validates guest name, phone, and party size.
5. The system creates an `ACTIVE` table session and changes the table to `OCCUPIED` in one transaction.

## Business rules and alternate flows

- The table must belong to the token branch and have `AVAILABLE` status.
- A table can have at most one `ACTIVE` session.
- Guest name is required.
- Party size must be positive and cannot exceed table capacity.
- Guest phone is optional; when provided, it must contain 9-15 digits.
- Concurrent registration requests cannot create two active sessions for one table.

## Acceptance criteria

1. A valid request creates one active session and occupies the table.
2. A table outside the token branch is not accessible.
3. Occupied or reserved tables and duplicate active sessions are rejected.
4. Invalid party size and phone values are rejected.
5. Session creation and table status update are atomic.

## API contract

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

## Database design

`table_sessions` stores the branch, current table, guest information, status (`ACTIVE` or `CLOSED`), `started_at`, `ended_at`, audit fields, and optimistic-lock version.
