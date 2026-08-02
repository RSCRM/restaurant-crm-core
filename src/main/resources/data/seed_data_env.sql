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

-- UC-CM-07: manager/owner view staff schedules; manager's own row verifies self-exclusion.
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT role.id, permission.id
FROM org_roles role
CROSS JOIN org_permissions permission
WHERE role.role_name IN ('OWNER', 'MANAGER')
  AND permission.permission_name = 'SCHEDULE_STAFF_READ'
ON CONFLICT DO NOTHING;

INSERT INTO work_schedules
    (id, version, employee_id, branch_id, work_date, start_time, end_time, note, created_at, updated_at)
VALUES
    ('77000000-0000-0000-0000-000000000001', 0,
     'f0000000-0000-0000-0000-000000000001',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE, '08:00', '17:00', 'Manager - must not appear', NOW(), NOW()),
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
-- UC-SW-01/02: permissions for viewing and searching tables.
INSERT INTO org_permissions (id, version, permission_name, created_at, updated_at)
VALUES
    ('p0000000-0000-0000-0000-000000000101', 0, 'TABLE_MAP_READ', NOW(), NOW()),
    ('p0000000-0000-0000-0000-000000000102', 0, 'TABLE_SEARCH_READ', NOW(), NOW())
ON CONFLICT (permission_name) DO NOTHING;

INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT role.id, permission.id
FROM org_roles role
CROSS JOIN org_permissions permission
WHERE role.role_name IN ('OWNER', 'MANAGER', 'CASHIER', 'WAITER', 'CHEF')
  AND permission.permission_name IN ('TABLE_MAP_READ', 'TABLE_SEARCH_READ')
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
     'Bàn 06', 8, 'AVAILABLE', 2, 0, NOW(), NOW())
ON CONFLICT (table_id) DO UPDATE
SET area_id = EXCLUDED.area_id,
    table_number = EXCLUDED.table_number,
    capacity = EXCLUDED.capacity,
    status = EXCLUDED.status,
    position_x = EXCLUDED.position_x,
    position_y = EXCLUDED.position_y,
    updated_at = NOW();
