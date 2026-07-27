# UC-SW-04 - Transfer Table

## Objective

Move an active guest session and its unpaid order from the current table to another available table.

## Business Rules

1. The request uses a context token; branch and employee are read from its claims.
2. The table session must exist in the token branch and have `ACTIVE` status.
3. The destination must be a different table in the same branch.
4. The destination table must be `AVAILABLE` and have no active session.
5. The source table becomes `AVAILABLE`; the destination becomes `OCCUPIED`.
6. A pending order at the source table moves to the destination table.
7. Session, tables, order and transfer history are updated in one transaction.
8. Locked table rows prevent another operation from taking the destination during transfer.

## API Contract

`PUT /api/v1/table-sessions/{sessionId}/transfer`

```json
{
  "targetTableId": "target-table-id"
}
```

## Data Design

`table_transfer_history` records the session, source table, destination table, employee and transfer time. Existing `table_sessions`, `restaurant_tables` and `orders` rows are updated; no new order is created.

Hibernate code-first manages the schema. No Flyway migration is added before a project migration baseline is agreed.

## Acceptance Criteria

- A valid transfer atomically moves the session and pending order.
- Cross-branch, same-table, inactive-session and unavailable-target requests are rejected.
- Every successful transfer leaves an audit history row.
