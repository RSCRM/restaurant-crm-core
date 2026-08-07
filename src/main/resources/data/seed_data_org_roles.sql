-- =============================================================================
-- SEED DATA: ORG-SCOPED ROLES
-- Bổ sung org_roles có organization_id cho từng org, update employees map đúng
-- Run after seed_data.sql
-- =============================================================================

-- =============================================================================
-- 1. ORG-SCOPED ROLES (3 orgs × 5 roles = 15 roles)
-- =============================================================================

-- Phở Việt Chain (d001)
INSERT INTO org_roles (id, version, organization_id, role_name, data_scope, created_at, updated_at) VALUES
    ('r0000000-0000-0000-0000-000000000101', 0, 'd0000000-0000-0000-0000-000000000001', 'OWNER',   'ORGANIZATION', NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000102', 0, 'd0000000-0000-0000-0000-000000000001', 'MANAGER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000103', 0, 'd0000000-0000-0000-0000-000000000001', 'CASHIER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000104', 0, 'd0000000-0000-0000-0000-000000000001', 'WAITER',  'SELF',         NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000105', 0, 'd0000000-0000-0000-0000-000000000001', 'CHEF',    'SELF',         NOW(), NOW())
    ON CONFLICT (organization_id, role_name) DO NOTHING;

-- Sushi Tokyo Group (d002)
INSERT INTO org_roles (id, version, organization_id, role_name, data_scope, created_at, updated_at) VALUES
    ('r0000000-0000-0000-0000-000000000201', 0, 'd0000000-0000-0000-0000-000000000002', 'OWNER',   'ORGANIZATION', NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000202', 0, 'd0000000-0000-0000-0000-000000000002', 'MANAGER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000203', 0, 'd0000000-0000-0000-0000-000000000002', 'CASHIER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000204', 0, 'd0000000-0000-0000-0000-000000000002', 'WAITER',  'SELF',         NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000205', 0, 'd0000000-0000-0000-0000-000000000002', 'CHEF',    'SELF',         NOW(), NOW())
    ON CONFLICT (organization_id, role_name) DO NOTHING;

-- BBQ Garden (d003)
INSERT INTO org_roles (id, version, organization_id, role_name, data_scope, created_at, updated_at) VALUES
    ('r0000000-0000-0000-0000-000000000301', 0, 'd0000000-0000-0000-0000-000000000003', 'OWNER',   'ORGANIZATION', NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000302', 0, 'd0000000-0000-0000-0000-000000000003', 'MANAGER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000303', 0, 'd0000000-0000-0000-0000-000000000003', 'CASHIER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000304', 0, 'd0000000-0000-0000-0000-000000000003', 'WAITER',  'SELF',         NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000305', 0, 'd0000000-0000-0000-0000-000000000003', 'CHEF',    'SELF',         NOW(), NOW())
    ON CONFLICT (organization_id, role_name) DO NOTHING;

-- =============================================================================
-- 2. PERMISSION MAPPINGS — OWNER (all permissions per org)
-- =============================================================================
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r, org_permissions p
WHERE r.role_name = 'OWNER' AND r.organization_id IN (
    'd0000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000002',
    'd0000000-0000-0000-0000-000000000003'
)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 3. PERMISSION MAPPINGS — MANAGER
-- =============================================================================
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r, org_permissions p
WHERE r.role_name = 'MANAGER' AND r.organization_id IN (
    'd0000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000002',
    'd0000000-0000-0000-0000-000000000003'
) AND p.permission_name IN (
    'ORDER_READ', 'ORDER_CREATE', 'ORDER_UPDATE',
    'PAYMENT_READ', 'PAYMENT_CREATE',
    'MENU_MANAGE', 'TABLE_MANAGE', 'REPORT_VIEW',
    'SCHEDULE_STAFF_READ', 'SCHEDULE_MANAGE', 'ATTENDANCE_SELF_READ',
    'EMPLOYEE_DELETE', 'EMPLOYEE_ROLE_ASSIGN', 'EMPLOYEE_ROLE_REVOKE',
    'TABLE_MAP_READ', 'TABLE_SEARCH_READ',
    'BOOKING_READ', 'BOOKING_CREATE', 'BOOKING_UPDATE', 'BOOKING_DELETE',
    'CUSTOMER_READ', 'POINT_WALLET_READ', 'VOUCHER_CREATE', 'VOUCHER_READ',
    'VOUCHER_UPDATE', 'CUSTOMER_VOUCHER_READ', 'CUSTOMER_VOUCHER_REDEEM',
    'CUSTOMER_VOUCHER_GIVE', 'CUSTOMER_VOUCHER_USE'
)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 4. PERMISSION MAPPINGS — CASHIER
-- =============================================================================
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r, org_permissions p
WHERE r.role_name = 'CASHIER' AND r.organization_id IN (
    'd0000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000002',
    'd0000000-0000-0000-0000-000000000003'
) AND p.permission_name IN (
    'ORDER_READ', 'ORDER_CREATE', 'PAYMENT_READ', 'PAYMENT_CREATE',
    'SCHEDULE_STAFF_READ', 'SCHEDULE_MANAGE', 'ATTENDANCE_SELF_READ',
    'TABLE_MAP_READ', 'TABLE_SEARCH_READ'
)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 5. PERMISSION MAPPINGS — WAITER
-- =============================================================================
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r, org_permissions p
WHERE r.role_name = 'WAITER' AND r.organization_id IN (
    'd0000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000002',
    'd0000000-0000-0000-0000-000000000003'
) AND p.permission_name IN (
    'ORDER_READ', 'ORDER_CREATE', 'SCHEDULE_STAFF_READ', 'SCHEDULE_MANAGE',
    'ATTENDANCE_SELF_READ', 'TABLE_MAP_READ', 'TABLE_SEARCH_READ'
)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 6. PERMISSION MAPPINGS — CHEF
-- =============================================================================
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r, org_permissions p
WHERE r.role_name = 'CHEF' AND r.organization_id IN (
    'd0000000-0000-0000-0000-000000000001',
    'd0000000-0000-0000-0000-000000000002',
    'd0000000-0000-0000-0000-000000000003'
) AND p.permission_name IN (
    'ORDER_READ', 'ORDER_UPDATE', 'SCHEDULE_STAFF_READ', 'SCHEDULE_MANAGE',
    'ATTENDANCE_SELF_READ', 'TABLE_MAP_READ', 'TABLE_SEARCH_READ'
)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- 7. UPDATE EMPLOYEES → map đúng org-scoped role
-- =============================================================================

-- f001: manager user → MANAGER @ Phở Việt Q1 (branch e001 → org d001)
UPDATE employees SET org_role_id = 'r0000000-0000-0000-0000-000000000102'
WHERE id = 'f0000000-0000-0000-0000-000000000001';

-- f002: manager user → CASHIER @ Sushi Tokyo NH (branch e003 → org d002)
UPDATE employees SET org_role_id = 'r0000000-0000-0000-0000-000000000203'
WHERE id = 'f0000000-0000-0000-0000-000000000002';

-- f003: manager user → MANAGER @ BBQ Q3 (branch e005 → org d003)
UPDATE employees SET org_role_id = 'r0000000-0000-0000-0000-000000000302'
WHERE id = 'f0000000-0000-0000-0000-000000000003';

-- f008: chef_q1 → CHEF @ Phở Việt Q1 (branch e001 → org d001)
UPDATE employees SET org_role_id = 'r0000000-0000-0000-0000-000000000105'
WHERE id = 'f0000000-0000-0000-0000-000000000008';

-- f009: waiter_q1 → WAITER @ Phở Việt Q1 (branch e001 → org d001)
UPDATE employees SET org_role_id = 'r0000000-0000-0000-0000-000000000104'
WHERE id = 'f0000000-0000-0000-0000-000000000009';

-- f010: chef_q2 → CHEF @ Phở Việt Q1 (branch e001 → org d001)
UPDATE employees SET org_role_id = 'r0000000-0000-0000-0000-000000000105'
WHERE id = 'f0000000-0000-0000-0000-000000000010';
