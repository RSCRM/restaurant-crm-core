# UC-SW-05 - Table operations: Close table

## Business analysis

- **Actor:** Authenticated employee working in a branch.
- **Goal:** End an active table session and return the table to the available pool.
- **Preconditions:** The table session is active and belongs to the token branch.
- **Postcondition:** The session is closed and its table becomes available.

## Main flow

1. The employee requests to close an active table session.
2. The system resolves the branch from the context token.
3. The system locks and validates the session and table.
4. The system verifies that no `PENDING` order remains.
5. The system sets the session to `CLOSED`, records `endedAt`, and changes the table to `AVAILABLE` in one transaction.

## Business rules and alternate flows

- A session with a `PENDING` order cannot be closed because payment is incomplete.
- An active session without an order can be closed.
- Missing, cross-branch, and already-closed sessions are rejected.
- A failed operation does not partially update the session or table.

## Acceptance criteria

1. A paid or orderless active session closes successfully.
2. A pending order blocks closing.
3. A successful close records the end time.
4. A successful close makes the table available.
5. Session and table updates are atomic.

## API contract

`PUT /api/v1/table-sessions/{sessionId}/close`

The response returns the closed session and its end time.

## Database design

No new table is required. The operation updates `table_sessions.status`, `table_sessions.ended_at`, and `restaurant_tables.status`.
