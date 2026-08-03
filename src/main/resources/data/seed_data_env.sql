-- UC-CM-05 and UC-CM-06 local test data. Run after seed_data.sql.

UPDATE users
SET password = '$2a$10$CsRk2L1Tt5h8.MgskpXgyuwwmFD22yqhq1QPwunLYXAcBMlqGt.ga'
WHERE username IN ('owner_a', 'owner_b', 'manager', 'chef_q1', 'chef_q2', 'waiter_q1');

-- UC-CM-05: profile viewing and staff profile management.
INSERT INTO org_permissions (id, version, permission_name, created_at, updated_at)
VALUES
    ('p0000000-0000-0000-0000-000000000216', 0, 'PROFILE_VIEW', NOW(), NOW()),
    ('p0000000-0000-0000-0000-000000000217', 0, 'PROFILE_UPDATE', NOW(), NOW())
ON CONFLICT (permission_name) DO NOTHING;

INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT role.id, permission.id
FROM org_roles role
CROSS JOIN org_permissions permission
WHERE role.role_name IN ('OWNER', 'MANAGER')
  AND permission.permission_name IN ('PROFILE_VIEW', 'PROFILE_UPDATE')
ON CONFLICT DO NOTHING;

INSERT INTO user_profiles
    (id, version, user_id, full_name, phone, created_at, updated_at)
VALUES
    ('c1000000-0000-0000-0000-000000000008', 0,
     'c0000000-0000-0000-0000-000000000008',
     'Chef Q1', '0905000001', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- UC-CM-06: manager displays the branch QR; waiter_q1 checks in and out.
INSERT INTO org_permissions (id, version, permission_name, created_at, updated_at)
VALUES ('p0000000-0000-0000-0000-000000000115', 0,
        'ATTENDANCE_BRANCH_READ', NOW(), NOW())
ON CONFLICT (permission_name) DO NOTHING;

INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT role.id, permission.id
FROM org_roles role
CROSS JOIN org_permissions permission
WHERE (role.role_name = 'MANAGER'
       AND permission.permission_name IN (
           'ATTENDANCE_BRANCH_READ',
           'ATTENDANCE_QR_DISPLAY'))
   OR (role.role_name = 'OWNER'
       AND permission.permission_name IN (
           'ATTENDANCE_BRANCH_READ',
           'ATTENDANCE_QR_DISPLAY',
           'ORGANIZATION_BRANCH_VIEW'))
   OR (role.role_name IN ('CASHIER', 'WAITER', 'CHEF')
       AND permission.permission_name IN (
           'ATTENDANCE_SELF_WRITE',
           'ATTENDANCE_SELF_READ'))
ON CONFLICT DO NOTHING;

DELETE FROM org_roles_org_permissions mapping
USING org_roles role, org_permissions permission
WHERE mapping.org_role_id = role.id
  AND mapping.org_permissions_id = permission.id
  AND role.role_name = 'MANAGER'
  AND permission.permission_name IN (
      'ATTENDANCE_SELF_WRITE',
      'ATTENDANCE_SELF_READ');

DELETE FROM attendances
WHERE shift_assignment_id IN (
    'sa000000-0000-0000-0000-000000000009',
    'sa000000-0000-0000-0000-000000000010'
);

INSERT INTO shift_assignments
    (id, version, employee_id, branch_id, work_date, start_at, end_at, created_at, updated_at)
VALUES
    ('sa000000-0000-0000-0000-000000000009', 0,
     'f0000000-0000-0000-0000-000000000009',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE, NOW() - INTERVAL '5 minutes', CURRENT_DATE + INTERVAL '1 day',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000010', 0,
     'f0000000-0000-0000-0000-000000000010',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE, NOW() - INTERVAL '5 minutes', CURRENT_DATE + INTERVAL '1 day',
     NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET employee_id = EXCLUDED.employee_id,
    branch_id = EXCLUDED.branch_id,
    work_date = EXCLUDED.work_date,
    start_at = EXCLUDED.start_at,
    end_at = EXCLUDED.end_at,
    updated_at = NOW();

-- Display data: chef_q1 has history and is currently working.
INSERT INTO shift_assignments
    (id, version, employee_id, branch_id, work_date, start_at, end_at, created_at, updated_at)
VALUES
    ('sa000000-0000-0000-0000-000000000008', 0,
     'f0000000-0000-0000-0000-000000000008',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE, NOW() - INTERVAL '2 hours', NOW() + INTERVAL '6 hours',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000108', 0,
     'f0000000-0000-0000-0000-000000000008',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE - 1, NOW() - INTERVAL '1 day 8 hours', NOW() - INTERVAL '1 day',
     NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET employee_id = EXCLUDED.employee_id,
    branch_id = EXCLUDED.branch_id,
    work_date = EXCLUDED.work_date,
    start_at = EXCLUDED.start_at,
    end_at = EXCLUDED.end_at,
    updated_at = NOW();

INSERT INTO attendances
    (id, version, shift_assignment_id, check_in_at, check_out_at, status, created_at, updated_at)
VALUES
    ('a0000000-0000-0000-0000-000000000008', 0,
     'sa000000-0000-0000-0000-000000000008',
     NOW() - INTERVAL '2 hours', NULL, 'ON_TIME', NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000108', 0,
     'sa000000-0000-0000-0000-000000000108',
     NOW() - INTERVAL '1 day 8 hours', NOW() - INTERVAL '1 day',
     'ON_TIME', NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET shift_assignment_id = EXCLUDED.shift_assignment_id,
    check_in_at = EXCLUDED.check_in_at,
    check_out_at = EXCLUDED.check_out_at,
    status = EXCLUDED.status,
    updated_at = NOW();

-- UC-CM-07: manager/owner view and create staff schedules.
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT role.id, permission.id
FROM org_roles role
CROSS JOIN org_permissions permission
WHERE role.role_name IN ('OWNER', 'MANAGER')
  AND permission.permission_name IN ('SCHEDULE_STAFF_READ', 'SCHEDULE_MANAGE')
ON CONFLICT DO NOTHING;

DELETE FROM work_schedules
WHERE employee_id = 'f0000000-0000-0000-0000-000000000001';

INSERT INTO work_schedules
    (id, version, employee_id, branch_id, work_date, start_time, end_time, note, created_at, updated_at)
VALUES
    ('77000000-0000-0000-0000-000000000002', 0,
     'f0000000-0000-0000-0000-000000000008',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE, '08:00', '16:00', 'Chef morning shift', NOW(), NOW()),
    ('77000000-0000-0000-0000-000000000003', 0,
     'f0000000-0000-0000-0000-000000000009',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE, '14:00', '22:00', 'Waiter afternoon shift', NOW(), NOW()),
    ('77000000-0000-0000-0000-000000000004', 0,
     'f0000000-0000-0000-0000-000000000010',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE + 1, '09:00', '18:00', 'Chef next-day shift', NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET employee_id = EXCLUDED.employee_id,
    branch_id = EXCLUDED.branch_id,
    work_date = EXCLUDED.work_date,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    note = EXCLUDED.note,
    updated_at = NOW();

-- UC-SW-01: table status map for Phở Việt - Chi nhánh Q1.
-- UC-SW-01/02/03: permissions for viewing, searching and opening table sessions.
INSERT INTO org_permissions (id, version, permission_name, created_at, updated_at)
VALUES
    ('p0000000-0000-0000-0000-000000000101', 0, 'TABLE_MAP_READ', NOW(), NOW()),
    ('p0000000-0000-0000-0000-000000000102', 0, 'TABLE_SEARCH_READ', NOW(), NOW()),
    ('p0000000-0000-0000-0000-000000000103', 0, 'TABLE_SESSION_CREATE', NOW(), NOW())
ON CONFLICT (permission_name) DO NOTHING;

INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT role.id, permission.id
FROM org_roles role
CROSS JOIN org_permissions permission
WHERE (role.role_name IN ('OWNER', 'MANAGER', 'CASHIER', 'WAITER', 'CHEF')
       AND permission.permission_name IN ('TABLE_MAP_READ', 'TABLE_SEARCH_READ'))
   OR (role.role_name IN ('OWNER', 'MANAGER', 'CASHIER', 'WAITER')
       AND permission.permission_name = 'TABLE_SESSION_CREATE')
ON CONFLICT DO NOTHING;

-- Owner/manager handle reservations from the table-management screen.
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT role.id, permission.id
FROM org_roles role
CROSS JOIN org_permissions permission
WHERE role.role_name IN ('OWNER', 'MANAGER')
  AND permission.permission_name IN ('BOOKING_READ', 'BOOKING_UPDATE')
ON CONFLICT DO NOTHING;

INSERT INTO table_areas
    (area_id, version, branch_id, area_name, description, display_order, created_at, updated_at)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 0,
     'e0000000-0000-0000-0000-000000000001',
     'Khu A', 'Khu vực trong nhà', 1, NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000002', 0,
     'e0000000-0000-0000-0000-000000000001',
     'Sân vườn', 'Khu vực ngoài trời', 2, NOW(), NOW())
ON CONFLICT (area_id) DO UPDATE
SET branch_id = EXCLUDED.branch_id,
    area_name = EXCLUDED.area_name,
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    updated_at = NOW();

INSERT INTO restaurant_tables
    (table_id, version, area_id, table_number, capacity, status,
     position_x, position_y, created_at, updated_at)
VALUES
    ('t0000000-0000-0000-0000-000000000001', 0,
     'a0000000-0000-0000-0000-000000000001',
     'Bàn 01', 4, 'AVAILABLE', 0, 0, NOW(), NOW()),
    ('t0000000-0000-0000-0000-000000000002', 0,
     'a0000000-0000-0000-0000-000000000001',
     'Bàn 02', 4, 'OCCUPIED', 1, 0, NOW(), NOW()),
    ('t0000000-0000-0000-0000-000000000003', 0,
     'a0000000-0000-0000-0000-000000000001',
     'Bàn 03', 6, 'RESERVED', 2, 0, NOW(), NOW()),
    ('t0000000-0000-0000-0000-000000000004', 0,
     'a0000000-0000-0000-0000-000000000002',
     'Bàn 04', 2, 'AVAILABLE', 0, 0, NOW(), NOW()),
    ('t0000000-0000-0000-0000-000000000005', 0,
     'a0000000-0000-0000-0000-000000000002',
     'Bàn 05', 4, 'OCCUPIED', 1, 0, NOW(), NOW()),
    ('t0000000-0000-0000-0000-000000000006', 0,
     'a0000000-0000-0000-0000-000000000002',
     'Bàn 06', 8, 'AVAILABLE', 2, 0, NOW(), NOW()),
    ('t0000000-0000-0000-0000-000000000007', 0,
     'a0000000-0000-0000-0000-000000000001',
     'Bàn 07', 4, 'RESERVED', 3, 0, NOW(), NOW()),
    ('t0000000-0000-0000-0000-000000000008', 0,
     'a0000000-0000-0000-0000-000000000002',
     'Bàn 08', 6, 'RESERVED', 3, 0, NOW(), NOW()),
    ('t0000000-0000-0000-0000-000000000009', 0,
     'a0000000-0000-0000-0000-000000000002',
     'Bàn 09', 8, 'RESERVED', 4, 0, NOW(), NOW())
ON CONFLICT (table_id) DO UPDATE
SET area_id = EXCLUDED.area_id,
    table_number = EXCLUDED.table_number,
    capacity = EXCLUDED.capacity,
    position_x = EXCLUDED.position_x,
    position_y = EXCLUDED.position_y,
    updated_at = NOW();

INSERT INTO customers (id, version, phone, status, created_at, updated_at)
VALUES
    ('d1000000-0000-0000-0000-000000000003', 0, '0905000013', 'ACTIVE', NOW(), NOW()),
    ('d1000000-0000-0000-0000-000000000007', 0, '0905000017', 'ACTIVE', NOW(), NOW()),
    ('d1000000-0000-0000-0000-000000000008', 0, '0905000018', 'ACTIVE', NOW(), NOW()),
    ('d1000000-0000-0000-0000-000000000009', 0, '0905000019', 'ACTIVE', NOW(), NOW())
ON CONFLICT (phone) DO NOTHING;

INSERT INTO bookings
    (id, version, branch_id, table_id, customer_id, booking_time,
     guest_count, status, note, created_at, updated_at)
VALUES
    ('b1000000-0000-0000-0000-000000000003', 0,
     'e0000000-0000-0000-0000-000000000001',
     't0000000-0000-0000-0000-000000000003',
     (SELECT id FROM customers WHERE phone = '0905000013'),
     NOW() + INTERVAL '2 hours', 4, 'PENDING', 'Test reservation - table 03', NOW(), NOW()),
    ('b1000000-0000-0000-0000-000000000007', 0,
     'e0000000-0000-0000-0000-000000000001',
     't0000000-0000-0000-0000-000000000007',
     (SELECT id FROM customers WHERE phone = '0905000017'),
     NOW() + INTERVAL '3 hours', 3, 'PENDING', 'Test reservation - table 07', NOW(), NOW()),
    ('b1000000-0000-0000-0000-000000000008', 0,
     'e0000000-0000-0000-0000-000000000001',
     't0000000-0000-0000-0000-000000000008',
     (SELECT id FROM customers WHERE phone = '0905000018'),
     NOW() + INTERVAL '4 hours', 5, 'PENDING', 'Test reservation - table 08', NOW(), NOW()),
    ('b1000000-0000-0000-0000-000000000009', 0,
     'e0000000-0000-0000-0000-000000000001',
     't0000000-0000-0000-0000-000000000009',
     (SELECT id FROM customers WHERE phone = '0905000019'),
     NOW() + INTERVAL '5 hours', 6, 'PENDING', 'Test reservation - table 09', NOW(), NOW())
ON CONFLICT (id) DO UPDATE
SET branch_id = EXCLUDED.branch_id,
    table_id = EXCLUDED.table_id,
    customer_id = EXCLUDED.customer_id,
    booking_time = EXCLUDED.booking_time,
    guest_count = EXCLUDED.guest_count,
    status = EXCLUDED.status,
    note = EXCLUDED.note,
    updated_at = NOW();

UPDATE restaurant_tables table_data
SET status = 'RESERVED',
    updated_at = NOW()
WHERE table_data.table_id IN (
    SELECT booking.table_id
    FROM bookings booking
    WHERE booking.status IN ('PENDING', 'CONFIRMED')
)
AND NOT EXISTS (
    SELECT 1
    FROM table_sessions session
    WHERE session.table_id = table_data.table_id
      AND session.status = 'ACTIVE'
);

INSERT INTO table_sessions
    (table_session_id, version, branch_id, table_id, guest_name, guest_phone,
     party_size, status, started_at, ended_at, note, created_at, updated_at)
VALUES
    ('s0000000-0000-0000-0000-000000000002', 0,
     'e0000000-0000-0000-0000-000000000001',
     't0000000-0000-0000-0000-000000000002',
     'Khách bàn 02', NULL, 2, 'ACTIVE', NOW(), NULL, 'Dữ liệu môi trường', NOW(), NOW()),
    ('s0000000-0000-0000-0000-000000000005', 0,
     'e0000000-0000-0000-0000-000000000001',
     't0000000-0000-0000-0000-000000000005',
     'Khách bàn 05', NULL, 2, 'ACTIVE', NOW(), NULL, 'Dữ liệu môi trường', NOW(), NOW())
ON CONFLICT (table_session_id) DO NOTHING;

UPDATE restaurant_tables table_data
SET status = 'OCCUPIED',
    updated_at = NOW()
WHERE EXISTS (
    SELECT 1
    FROM table_sessions session
    WHERE session.table_id = table_data.table_id
      AND session.status = 'ACTIVE'
);
