# UC-SW-05 - Close Table

## Objective

End an active table session and return the table to the available pool.

## Business Rules

1. The request uses a context token and can only close a session in its branch.
2. The table session must exist and have `ACTIVE` status.
3. A session with a `PENDING` order cannot be closed because payment is incomplete.
4. A session without an order can be closed.
5. Closing sets the session to `CLOSED`, records `endedAt`, and changes the table to `AVAILABLE`.
6. Session and table changes run in one transaction with locked rows.

## API Contract

`PUT /api/v1/table-sessions/{sessionId}/close`

The response returns the closed session and its end time.

## Data Design

No new table is required. The operation updates `table_sessions.status`, `table_sessions.ended_at` and `restaurant_tables.status`. Hibernate code-first manages these columns.

## Acceptance Criteria

- A paid or orderless active session closes successfully.
- A pending order blocks closing.
- Missing, cross-branch and already-closed sessions are rejected.
- A successful close makes the table available.
