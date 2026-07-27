# UC-SW-04 - Table operations: Transfer table

## Business analysis

- **Actor:** Authenticated employee working in a branch.
- **Goal:** Move an active guest session and its unpaid order to another available table.
- **Preconditions:** The source session is active and belongs to the token branch.
- **Postcondition:** The session and pending order reference the destination table; table statuses and transfer history are updated.

## Main flow

1. The employee selects an active table session and a destination table.
2. The system resolves the branch and employee from the context token.
3. The system locks and validates the source and destination tables.
4. The system moves the active session and pending order to the destination.
5. The source becomes `AVAILABLE`, the destination becomes `OCCUPIED`, and transfer history is recorded in one transaction.

## Business rules and alternate flows

- The destination must be a different `AVAILABLE` table in the same branch.
- The destination cannot already have an active session.
- The source session must have `ACTIVE` status.
- A pending order moves with the session; no new order is created.
- Cross-branch, same-table, inactive-session, and unavailable-target requests are rejected.

## Acceptance criteria

1. A valid transfer atomically moves the session and pending order.
2. The operation cannot expose or update another branch's data.
3. Both table statuses are updated consistently.
4. Every successful transfer creates one audit history row.
5. Concurrent operations cannot take the destination table twice.

## API contract

`PUT /api/v1/table-sessions/{sessionId}/transfer`

```json
{
  "targetTableId": "target-table-id"
}
```

## Database design

`table_transfer_history` records the session, source table, destination table, employee, and transfer time. Existing `table_sessions`, `restaurant_tables`, and `orders` rows are updated.
