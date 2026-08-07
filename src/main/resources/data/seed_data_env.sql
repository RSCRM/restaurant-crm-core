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

-- Attendance matrix: Chef Q1 on time, Chef Q2 over 5 minutes late,
-- Waiter Q1 not checked in, plus two days of mixed history.
DELETE FROM attendances
WHERE shift_assignment_id IN (
    'sa000000-0000-0000-0000-000000000008',
    'sa000000-0000-0000-0000-000000000009',
    'sa000000-0000-0000-0000-000000000010',
    'sa000000-0000-0000-0000-000000000108',
    'sa000000-0000-0000-0000-000000000109',
    'sa000000-0000-0000-0000-000000000110',
    'sa000000-0000-0000-0000-000000000208',
    'sa000000-0000-0000-0000-000000000209',
    'sa000000-0000-0000-0000-000000000210'
);

INSERT INTO shift_assignments
    (id, version, employee_id, branch_id, work_date, start_at, end_at, created_at, updated_at)
VALUES
    ('sa000000-0000-0000-0000-000000000008', 0,
     'f0000000-0000-0000-0000-000000000008',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE, date_trunc('minute', NOW()) - INTERVAL '2 hours', NOW() + INTERVAL '6 hours',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000009', 0,
     'f0000000-0000-0000-0000-000000000009',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE, NOW() - INTERVAL '5 minutes', CURRENT_DATE + INTERVAL '1 day',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000010', 0,
     'f0000000-0000-0000-0000-000000000010',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE, date_trunc('minute', NOW()) - INTERVAL '16 minutes', NOW() + INTERVAL '7 hours 44 minutes',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000108', 0,
     'f0000000-0000-0000-0000-000000000008',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE - 1, CURRENT_DATE - 1 + INTERVAL '8 hours', CURRENT_DATE - 1 + INTERVAL '16 hours',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000109', 0,
     'f0000000-0000-0000-0000-000000000009',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE - 1, CURRENT_DATE - 1 + INTERVAL '14 hours', CURRENT_DATE - 1 + INTERVAL '22 hours',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000110', 0,
     'f0000000-0000-0000-0000-000000000010',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE - 1, CURRENT_DATE - 1 + INTERVAL '9 hours', CURRENT_DATE - 1 + INTERVAL '17 hours',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000208', 0,
     'f0000000-0000-0000-0000-000000000008',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE - 2, CURRENT_DATE - 2 + INTERVAL '8 hours', CURRENT_DATE - 2 + INTERVAL '16 hours',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000209', 0,
     'f0000000-0000-0000-0000-000000000009',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE - 2, CURRENT_DATE - 2 + INTERVAL '14 hours', CURRENT_DATE - 2 + INTERVAL '22 hours',
     NOW(), NOW()),
    ('sa000000-0000-0000-0000-000000000210', 0,
     'f0000000-0000-0000-0000-000000000010',
     'e0000000-0000-0000-0000-000000000001',
     CURRENT_DATE - 2, CURRENT_DATE - 2 + INTERVAL '9 hours', CURRENT_DATE - 2 + INTERVAL '17 hours',
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
     date_trunc('minute', NOW()) - INTERVAL '2 hours', NULL, 'ON_TIME', NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000010', 0,
     'sa000000-0000-0000-0000-000000000010',
     date_trunc('minute', NOW()) - INTERVAL '10 minutes', NULL, 'LATE', NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000108', 0,
     'sa000000-0000-0000-0000-000000000108',
     CURRENT_DATE - 1 + INTERVAL '7 hours 55 minutes',
     CURRENT_DATE - 1 + INTERVAL '16 hours 2 minutes',
     'ON_TIME', NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000109', 0,
     'sa000000-0000-0000-0000-000000000109',
     CURRENT_DATE - 1 + INTERVAL '14 hours 18 minutes',
     CURRENT_DATE - 1 + INTERVAL '22 hours',
     'LATE', NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000208', 0,
     'sa000000-0000-0000-0000-000000000208',
     CURRENT_DATE - 2 + INTERVAL '8 hours 16 minutes',
     CURRENT_DATE - 2 + INTERVAL '16 hours',
     'LATE', NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000209', 0,
     'sa000000-0000-0000-0000-000000000209',
     CURRENT_DATE - 2 + INTERVAL '13 hours 58 minutes',
     CURRENT_DATE - 2 + INTERVAL '22 hours',
     'ON_TIME', NOW(), NOW()),
    ('a0000000-0000-0000-0000-000000000210', 0,
     'sa000000-0000-0000-0000-000000000210',
     CURRENT_DATE - 2 + INTERVAL '9 hours',
     CURRENT_DATE - 2 + INTERVAL '17 hours',
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
WHERE employee_id IN ('f0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000008', 'f0000000-0000-0000-0000-000000000009', 'f0000000-0000-0000-0000-000000000010');

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

-- UC-SW: Booking permissions seed.
-- Ensure all 3 booking permission rows exist in org_permissions.
INSERT INTO org_permissions (id, version, permission_name, created_at, updated_at)
VALUES
    ('p0000000-0000-0000-0000-000000000301', 0, 'BOOKING_CREATE', NOW(), NOW()),
    ('p0000000-0000-0000-0000-000000000302', 0, 'BOOKING_READ',   NOW(), NOW()),
    ('p0000000-0000-0000-0000-000000000303', 0, 'BOOKING_UPDATE', NOW(), NOW())
ON CONFLICT (permission_name) DO NOTHING;

-- Owner/manager handle reservations from the table-management screen.
-- Grant BOOKING_CREATE + BOOKING_READ + BOOKING_UPDATE to OWNER and MANAGER.
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT role.id, permission.id
FROM org_roles role
CROSS JOIN org_permissions permission
WHERE role.role_name IN ('OWNER', 'MANAGER')
  AND permission.permission_name IN ('BOOKING_CREATE', 'BOOKING_READ', 'BOOKING_UPDATE')
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

-- SW-04: reservations and active sessions used by table-management tests.
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
ON CONFLICT (table_session_id) DO UPDATE
SET branch_id = EXCLUDED.branch_id,
    table_id = EXCLUDED.table_id,
    guest_name = EXCLUDED.guest_name,
    guest_phone = EXCLUDED.guest_phone,
    party_size = EXCLUDED.party_size,
    status = EXCLUDED.status,
    started_at = EXCLUDED.started_at,
    ended_at = NULL,
    note = EXCLUDED.note,
    updated_at = NOW();

UPDATE restaurant_tables table_data
SET status = 'OCCUPIED',
    updated_at = NOW()
WHERE EXISTS (
    SELECT 1
    FROM table_sessions session
    WHERE session.table_id = table_data.table_id
      AND session.status = 'ACTIVE'
);

-- =============================================================================
-- CRM LOYALTY & VOUCHERS SEED DATA
-- =============================================================================

ALTER TABLE vouchers DROP COLUMN IF EXISTS restaurant_id;
ALTER TABLE customer_point DROP COLUMN IF EXISTS restaurant_id;
ALTER TABLE customer_point_history DROP COLUMN IF EXISTS restaurant_id;
ALTER TABLE customer_vouchers DROP COLUMN IF EXISTS restaurant_id;

-- Seed Vouchers for Branch Phở Việt Q1 ('e0000000-0000-0000-0000-000000000001')
INSERT INTO vouchers (id, version, branch_id, title, discount_percent, min_bill_amount, points_required, is_active, expired_at, created_at, updated_at)
VALUES
    ('v0000000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', 'Voucher Thành Viên Giảm 10%', 10, 50000, 50, 1, NOW() + INTERVAL '30 days', NOW(), NOW()),
    ('v0000000-0000-0000-0000-000000000002', 0, 'e0000000-0000-0000-0000-000000000001', 'Voucher Thân Thiết Giảm 20%', 20, 100000, 100, 1, NOW() + INTERVAL '60 days', NOW(), NOW()),
    ('v0000000-0000-0000-0000-000000000003', 0, 'e0000000-0000-0000-0000-000000000001', 'Voucher VIP Giảm 30%', 30, 200000, 200, 1, NOW() + INTERVAL '90 days', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Seed Customer accounts
INSERT INTO customers (id, version, phone, status, created_at, updated_at)
VALUES
    ('c0000000-0000-0000-0000-000000000099', 0, '0966888888', 'ACTIVE', NOW(), NOW()),
    ('c0000000-0000-0000-0000-000000000098', 0, '0987654321', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Seed Customer Point Wallets for Phở Việt Organization ('d0000000-0000-0000-0000-000000000001')
INSERT INTO customer_point (id, version, customer_id, organization_id, current_points, lifetime_points, created_at, updated_at)
VALUES
    ('cp000000-0000-0000-0000-000000000099', 0, 'c0000000-0000-0000-0000-000000000099', 'd0000000-0000-0000-0000-000000000001', 500, 500, NOW(), NOW()),
    ('cp000000-0000-0000-0000-000000000098', 0, 'c0000000-0000-0000-0000-000000000098', 'd0000000-0000-0000-0000-000000000001', 200, 200, NOW(), NOW())
ON CONFLICT (customer_id, organization_id) DO NOTHING;

-- Seed Customer Vouchers
INSERT INTO customer_vouchers (id, version, customer_id, branch_id, voucher_id, voucher_sn, status, used_at, order_id, created_at, updated_at)
VALUES
    ('cv000000-0000-0000-0000-000000000001', 0, 'c0000000-0000-0000-0000-000000000099', 'e0000000-0000-0000-0000-000000000001', 'v0000000-0000-0000-0000-000000000001', 'V10PERCENTTEST', 'AVAILABLE', NULL, NULL, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- TEST DATA FOR ORDERING (ADD DISHES & COMBOS WITH NO DUPLICATES)
-- =============================================================================

-- 1. Insert new categories for Cơm and Món Phụ to avoid conflicts
INSERT INTO categories (category_id, version, branch_id, category_name, description, display_order, created_at, updated_at) VALUES
    ('cat00000-0000-0000-0000-000000000201', 0, 'e0000000-0000-0000-0000-000000000001', 'Cơm Đặc Sản', 'Các món cơm đặc sắc', 5, NOW(), NOW()),
    ('cat00000-0000-0000-0000-000000000202', 0, 'e0000000-0000-0000-0000-000000000001', 'Món Ăn Kèm Thêm', 'Các món phụ ăn kèm', 6, NOW(), NOW())
    ON CONFLICT (category_id) DO NOTHING;

-- 2. Insert new products for Chi nhánh Q1 (e0000000-0000-0000-0000-000000000001)
-- Category: Phở & Bún (cat00000-0000-0000-0000-000000000001)
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000101', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Phở Bò Viên Đặc Biệt',   'Phở bò viên dai ngon giòn giòn',                           55000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000102', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Phở Bò Gân Giòn',        'Phở gân bò hầm mềm giòn sần sật',                          60000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000103', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Bún Mọc Thanh Đạm',      'Bún mọc giò heo sườn non thanh đạm',                       50000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000104', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Bún Chả Hà Nội Xưa',     'Bún chả nướng than hoa đậm vị truyền thống',               60000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000105', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Bún Thịt Nướng Thơm',     'Bún thịt nướng tẩm ướp đậm đà, chả giò giòn',              50000.00, NULL, 'AVAILABLE', true,  NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Category: Cơm Đặc Sản (cat00000-0000-0000-0000-000000000201)
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000106', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000201', 'Cơm Chiên Dương Châu Ngon', 'Cơm chiên lạp xưởng, xá xíu, tôm, đậu hà lan',             45000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000107', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000201', 'Cơm Bò Lúc Lắc Mềm',      'Cơm bò lúc lắc khoai tây chiên sốt đậm đà',                65000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000108', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000201', 'Cơm Đùi Gà Xối Mỡ Giòn',   'Cơm chiên tỏi đùi gà xối mỡ da giòn rụm',                 55000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000109', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000201', 'Cơm Sườn Que Thơm',         'Cơm sườn que nướng mật ong thơm phức',                      50000.00, NULL, 'AVAILABLE', true,  NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Category: Đồ Uống (cat00000-0000-0000-0000-000000000002)
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000110', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000002', 'Sinh Tố Bơ Béo',           'Bơ sáp xay sữa đặc béo ngậy thơm ngon',                    35000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000111', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000002', 'Sinh Tố Xoài Ngọt',         'Xoài cát chín xay mát lạnh',                                30000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000112', 0, 'e0000000-0000-0000-0000-000000000002', 'cat00000-0000-0000-0000-000000000002', 'Trà Sữa Trân Châu Dai',    'Trà sữa hồng trà trân châu đen dai giòn',                   35000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000113', 0, 'e0000000-0000-0000-0000-000000000002', 'cat00000-0000-0000-0000-000000000002', 'Nước Suối Tinh Khiết',     'Nước khoáng đóng chai Aquafina 500ml',                     10000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000114', 0, 'e0000000-0000-0000-0000-000000000002', 'cat00000-0000-0000-0000-000000000002', 'Bia Heineken Lạnh',        'Bia Heineken lon 330ml',                                   30000.00, NULL, 'AVAILABLE', false, NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Category: Món Ăn Kèm Thêm (cat00000-0000-0000-0000-000000000202)
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000115', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000202', 'Nem Rán Giòn Rụm',         'Nem rán nhân thịt heo nấm mèo giòn rụm',                   20000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000116', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000202', 'Gỏi Cuốn Tôm Thịt Ngon',   'Gỏi cuốn tôm thịt chấm sốt tương đậu phộng',                15000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000117', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000202', 'Bát Bò Viên Thêm',         'Thêm bát bò viên nước lèo ăn kèm',                         20000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000118', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000202', 'Tiết Hột Gà Chần',         'Bát tiết hột gà chần béo ngậy thơm ngon',                  15000.00, NULL, 'AVAILABLE', true,  NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- 3. Combos
INSERT INTO combos (combo_id, version, branch_id, combo_name, description, price, image_url, status, created_at, updated_at) VALUES
    ('cmb00000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', 'Combo Phở & Nước Ép', 'Phở bò tái kèm nước cam ép sành tươi ngon', 70000.00, NULL, 'AVAILABLE', NOW(), NOW()),
    ('cmb00000-0000-0000-0000-000000000002', 0, 'e0000000-0000-0000-0000-000000000001', 'Set Gia Đình Ấm Cúng', 'Bao gồm các món cơm đặc sản tự chọn', 250000.00, NULL, 'AVAILABLE', NOW(), NOW()),
    ('cmb00000-0000-0000-0000-000000000003', 0, 'e0000000-0000-0000-0000-000000000001', 'Set Ăn Sáng Tiện Lợi', '1 Phở Bò Viên + 1 Trà Đá', 60000.00, NULL, 'AVAILABLE', NOW(), NOW())
    ON CONFLICT (combo_id) DO NOTHING;

-- 4. Combo Items
INSERT INTO combo_items (combo_item_id, version, combo_id, product_id, quantity, created_at, updated_at) VALUES
    ('cbi00000-0000-0000-0000-000000000001', 0, 'cmb00000-0000-0000-0000-000000000001', 'prd00000-0000-0000-0000-000000000101', 1, NOW(), NOW()),
    ('cbi00000-0000-0000-0000-000000000002', 0, 'cmb00000-0000-0000-0000-000000000001', 'prd00000-0000-0000-0000-000000000110', 1, NOW(), NOW()),
    ('cbi00000-0000-0000-0000-000000000003', 0, 'cmb00000-0000-0000-0000-000000000002', 'prd00000-0000-0000-0000-000000000106', 2, NOW(), NOW()),
    ('cbi00000-0000-0000-0000-000000000004', 0, 'cmb00000-0000-0000-0000-000000000002', 'prd00000-0000-0000-0000-000000000107', 2, NOW(), NOW()),
    ('cbi00000-0000-0000-0000-000000000005', 0, 'cmb00000-0000-0000-0000-000000000003', 'prd00000-0000-0000-0000-000000000101', 1, NOW(), NOW()),
    ('cbi00000-0000-0000-0000-000000000006', 0, 'cmb00000-0000-0000-0000-000000000003', 'prd00000-0000-0000-0000-000000000113', 1, NOW(), NOW())
    ON CONFLICT (combo_item_id) DO NOTHING;


