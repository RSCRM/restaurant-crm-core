# UC-CM-07 - Account management: View personal schedule

## Business analysis

- **Actor:** Authenticated employee.
- **Goal:** View the employee's own assigned shifts by day, week, or a custom date range.
- **Preconditions:** The access token contains valid `userId` and `employeeId` claims; the employee is active.
- **Postcondition:** The system returns only schedule entries assigned to the authenticated employee.

## Main flow

1. The employee requests their personal schedule.
2. The system resolves the employee from the authentication context.
3. The system validates the requested date range.
4. The system returns assigned shifts ordered by work date and start time.

## Alternate flows

- If no date range is supplied, the system returns today's schedule.
- If only `from` is supplied, `to` is treated as the same date.
- An invalid or greater-than-31-day range is rejected.
- A missing or inactive employee is rejected.
- A range with no assignments returns an empty list.

## Acceptance criteria

1. An employee can view only their own schedule.
2. The API supports a single day, a calendar week, and a custom range through `from` and `to`.
3. Results are ordered chronologically.
4. Each result contains schedule, employee, branch, time, and note information.
5. The implementation does not expose an API for assigning or changing shifts.

## API contract

`GET /api/v1/erp/schedules/me?from=YYYY-MM-DD&to=YYYY-MM-DD`

- `from`: optional, defaults to today.
- `to`: optional, defaults to `from`.
- Response: `ApiResponse<List<PersonalScheduleResponse>>`.

## Database design (code-first draft)

Table `work_schedules`:

| Column | Type | Constraint |
|---|---|---|
| id | varchar | primary key |
| employee_id | varchar | not null, foreign key |
| branch_id | varchar | not null, foreign key |
| work_date | date | not null |
| start_time | time | not null |
| end_time | time | not null |
| note | varchar(500) | nullable |
| version/audit fields | inherited | `BaseEntity` |

Unique assignment key: `(employee_id, work_date, start_time)`.

