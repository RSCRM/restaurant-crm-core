# UC-SW-02 - Table operations: Search table

## Business analysis

- **Actor:** Authenticated employee working in a branch.
- **Goal:** Search and filter tables in the current branch.
- **Preconditions:** The access token contains a valid `branchId`; the branch exists and is active.
- **Postcondition:** The system returns a paginated list containing only matching tables in that branch.

## Main flow

1. The employee supplies zero or more search criteria.
2. The system resolves the branch from the authentication context.
3. The system applies the keyword and filters.
4. The system returns matching tables ordered by area and table number.

## Search criteria

- Partial, case-insensitive table number.
- Area.
- Table status.
- Minimum and/or maximum capacity.
- Page and page size.

## Alternate flows

- No criteria returns all tables in the current branch.
- No match returns an empty page.
- An invalid capacity range or pagination request is rejected.
- Filters cannot expose tables from another branch.

## Acceptance criteria

1. Every query is scoped by the token's `branchId`.
2. Filters can be used independently or together.
3. Keyword matching is partial and case-insensitive.
4. Results are paginated with deterministic ordering.
5. Empty results are successful and contain an empty data list.

## API contract

`GET /api/v1/erp/tables/search`

Optional parameters: `keyword`, `areaId`, `status`, `minCapacity`, `maxCapacity`, `page` (default 1), and `size` (default 10).

Response: `ApiResponse<PagingResponse<TableSearchResponse>>`.

## Database design (code-first draft)

The existing `restaurant_tables` and `table_areas` tables are reused. Draft indexes are declared for:

- `restaurant_tables.table_number`
- `restaurant_tables.status`
- `restaurant_tables.capacity`

The existing unique `(area_id, table_number)` constraint remains unchanged.

