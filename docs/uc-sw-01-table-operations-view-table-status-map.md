# UC-SW-01 - Table operations: View table status map

## Business analysis

- **Actor:** Authenticated employee working in a branch.
- **Goal:** View table areas, table positions, capacities, and live statuses for the current branch.
- **Preconditions:** The access token contains a valid `branchId`; the branch exists and is active.
- **Postcondition:** The system returns a read-only table map grouped by area.

## Main flow

1. The employee opens the table map.
2. The system resolves the branch from the authentication context.
3. The system loads the branch's areas and tables.
4. The system groups tables by area and returns them in display order.

## Alternate flows

- The employee can request one area with `areaId`.
- An area outside the authenticated branch is rejected.
- A branch without areas returns an empty list.
- Nullable positions are returned for tables that have not been placed on the map.

## Acceptance criteria

1. Data is always scoped by the token's `branchId`.
2. The response contains area and table identifiers, labels, positions, capacity, and status.
3. Areas and tables have deterministic display ordering.
4. Filtering by `areaId` cannot expose another branch's data.
5. The API does not update table status or layout.

## API contract

`GET /api/v1/erp/tables/map?areaId={optional-area-id}`

Response: `ApiResponse<TableMapResponse>`.

## Database design (code-first draft)

Existing tables are reused:

- `table_areas`: adds nullable `display_order` for map ordering.
- `restaurant_tables`: adds nullable `position_x` and `position_y`.
- Existing relations and the unique `(area_id, table_number)` key remain unchanged.

