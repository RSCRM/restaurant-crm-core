-- =============================================================================
-- 1. IDENTITY MODULE: PERMISSIONS
-- =============================================================================

INSERT INTO permissions (id, version, permission_name, created_at, updated_at) VALUES
                                                                                   ('a0000000-0000-0000-0000-000000000001', 0, 'SYSTEM_VIEW',       NOW(), NOW()),
                                                                                   ('a0000000-0000-0000-0000-000000000002', 0, 'SYSTEM_MANAGE',     NOW(), NOW()),
                                                                                   ('a0000000-0000-0000-0000-000000000003', 0, 'USER_VIEW',         NOW(), NOW()),
                                                                                   ('a0000000-0000-0000-0000-000000000004', 0, 'USER_UPDATE',       NOW(), NOW()),
                                                                                   ('a0000000-0000-0000-0000-000000000005', 0, 'USER_DELETE',       NOW(), NOW()),
                                                                                   ('a0000000-0000-0000-0000-000000000006', 0, 'ROLE_VIEW',         NOW(), NOW()),
                                                                                   ('a0000000-0000-0000-0000-000000000007', 0, 'ROLE_MANAGE',       NOW(), NOW()),
                                                                                   ('a0000000-0000-0000-0000-000000000008', 0, 'PERMISSION_VIEW',   NOW(), NOW()),
                                                                                   ('a0000000-0000-0000-0000-000000000009', 0, 'PERMISSION_MANAGE', NOW(), NOW()),
                                                                                   ('a0000000-0000-0000-0000-000000000010', 0, 'AUDIT_VIEW',        NOW(), NOW())
    ON CONFLICT (permission_name) DO NOTHING;

-- =============================================================================
-- 2. IDENTITY MODULE: ROLES
-- =============================================================================

INSERT INTO roles (id, version, role_name, data_scope, created_at, updated_at) VALUES
                                                                                   ('b0000000-0000-0000-0000-000000000001', 0, 'ADMIN',  'SYSTEM', NOW(), NOW()),
                                                                                   ('b0000000-0000-0000-0000-000000000002', 0, 'USER',   'TENANT', NOW(), NOW())
    ON CONFLICT (role_name) DO NOTHING;

-- =============================================================================
-- 3. IDENTITY MODULE: ROLE <-> PERMISSION (roles_permissions)
-- =============================================================================

-- ADMIN: all system permissions
INSERT INTO roles_permissions (role_id, permissions_id) VALUES
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001'),
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002'),
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003'),
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004'),
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000005'),
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000006'),
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000007'),
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000008'),
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000009'),
                                                            ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000010')
    ON CONFLICT DO NOTHING;

-- =============================================================================
-- 4. USERS
-- =============================================================================

-- Case 1: Admin
INSERT INTO users (id, version, username, password, email, status, enabled, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000001', 0, 'admin', '$2a$10$LCKw9m993mk/Hz4v7C5u0u4ye3RA.GqVzpd9SC30euP/8pztZdxZq', 'admin@system.local', 'ACTIVE', true, NOW(), NOW())
    ON CONFLICT (username) DO NOTHING;

-- Case 2: Owner A (1 organization)
INSERT INTO users (id, version, username, password, email, status, enabled, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000002', 0, 'owner_a', '$2a$10$LCKw9m993mk/Hz4v7C5u0u4ye3RA.GqVzpd9SC30euP/8pztZdxZq', 'owner_a@restaurant.com', 'ACTIVE', true, NOW(), NOW())
    ON CONFLICT (username) DO NOTHING;

-- Case 3: Owner B (2 organizations)
INSERT INTO users (id, version, username, password, email, status, enabled, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000003', 0, 'owner_b', '$2a$10$LCKw9m993mk/Hz4v7C5u0u4ye3RA.GqVzpd9SC30euP/8pztZdxZq', 'owner_b@restaurant.com', 'ACTIVE', true, NOW(), NOW())
    ON CONFLICT (username) DO NOTHING;

-- Case 4 & 5: Manager
INSERT INTO users (id, version, username, password, email, status, enabled, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000004', 0, 'manager', '$2a$10$LCKw9m993mk/Hz4v7C5u0u4ye3RA.GqVzpd9SC30euP/8pztZdxZq', 'manager@restaurant.com', 'ACTIVE', true, NOW(), NOW())
    ON CONFLICT (username) DO NOTHING;

-- =============================================================================
-- 4.1. USER PROFILES
-- =============================================================================

INSERT INTO user_profiles (id, version, user_id, full_name, phone, created_at, updated_at) VALUES
                                                                                               ('c1000000-0000-0000-0000-000000000001', 0, 'c0000000-0000-0000-0000-000000000001', 'System Admin',       '0900000000', NOW(), NOW()),
                                                                                               ('c1000000-0000-0000-0000-000000000002', 0, 'c0000000-0000-0000-0000-000000000002', 'Restaurant Owner A', '0901000000', NOW(), NOW()),
                                                                                               ('c1000000-0000-0000-0000-000000000003', 0, 'c0000000-0000-0000-0000-000000000003', 'Restaurant Owner B', '0902000000', NOW(), NOW()),
                                                                                               ('c1000000-0000-0000-0000-000000000004', 0, 'c0000000-0000-0000-0000-000000000004', 'Restaurant Manager', '0904000001', NOW(), NOW())
    ON CONFLICT DO NOTHING;

-- =============================================================================
-- 5. USER <-> ROLE (user_roles)
-- =============================================================================

INSERT INTO user_roles (user_id, role_id) VALUES
                                              ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001'),  -- admin   -> ADMIN
                                              ('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002'),  -- owner_a -> USER
                                              ('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002'),  -- owner_b -> USER
                                              ('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000002')   -- manager -> USER
    ON CONFLICT DO NOTHING;

-- =============================================================================
-- 6. ERP MODULE: ORG_PERMISSIONS
-- =============================================================================

INSERT INTO org_permissions (id, version, permission_name, created_at, updated_at) VALUES
                                                                                       ('p0000000-0000-0000-0000-000000000001', 0, 'ORDER_READ',      NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000002', 0, 'ORDER_CREATE',    NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000003', 0, 'ORDER_UPDATE',    NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000004', 0, 'ORDER_DELETE',    NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000005', 0, 'PAYMENT_READ',    NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000006', 0, 'PAYMENT_CREATE',  NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000007', 0, 'MENU_MANAGE',     NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000008', 0, 'TABLE_MANAGE',    NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000009', 0, 'REPORT_VIEW',     NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000010', 0, 'STAFF_MANAGE',    NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000011', 0, 'BRANCH_MANAGE',   NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000012', 0, 'ORG_MANAGE',      NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000013', 0, 'SCHEDULE_STAFF_READ', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000014', 0, 'SCHEDULE_MANAGE',  NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000101', 0, 'TABLE_MAP_READ',   NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000113', 0, 'ATTENDANCE_QR_DISPLAY', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000114', 0, 'ATTENDANCE_SELF_WRITE', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000015', 0, 'ATTENDANCE_SELF_READ', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000213', 0, 'PROFILE_SELF_UPDATE', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000214', 0, 'EMPLOYEE_ADD', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000215', 0, 'EMPLOYEE_UPDATE', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000016', 0, 'EMPLOYEE_DELETE', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000017', 0, 'EMPLOYEE_ROLE_ASSIGN', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000018', 0, 'EMPLOYEE_ROLE_REVOKE', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000101', 0, 'TABLE_MAP_READ',   NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000102', 0, 'TABLE_SEARCH_READ', NOW(), NOW()),
																					   ('p0000000-0000-0000-0000-000000000201', 0, 'INVENTORY_CATEGORY_VIEW',   NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000202', 0, 'INVENTORY_CATEGORY_MANAGE', NOW(), NOW()),
																					   ('p0000000-0000-0000-0000-000000000203', 0, 'INVENTORY_VIEW',   NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000204', 0, 'INVENTORY_MANAGE', NOW(), NOW()),
																					   ('p0000000-0000-0000-0000-000000000205', 0, 'INVENTORY_TRANSACTION_VIEW',   NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000206', 0, 'INVENTORY_TRANSACTION_MANAGE', NOW(), NOW())
    ON CONFLICT (permission_name) DO NOTHING;



-- =============================================================================
-- 7. ERP MODULE: ORG_ROLES (per-organization, composite unique: organization_id + role_name)
-- =============================================================================

-- Phở Việt Chain (d001) roles
INSERT INTO org_roles (id, version, organization_id, role_name, data_scope, created_at, updated_at) VALUES
    ('r0000000-0000-0000-0000-000000000001', 0, 'd0000000-0000-0000-0000-000000000001', 'OWNER',   'ORGANIZATION', NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000002', 0, 'd0000000-0000-0000-0000-000000000001', 'MANAGER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000003', 0, 'd0000000-0000-0000-0000-000000000001', 'CASHIER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000004', 0, 'd0000000-0000-0000-0000-000000000001', 'WAITER',  'SELF',         NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000005', 0, 'd0000000-0000-0000-0000-000000000001', 'CHEF',    'SELF',         NOW(), NOW())
    ON CONFLICT (organization_id, role_name) DO NOTHING;

-- Sushi Tokyo Group (d002) roles
INSERT INTO org_roles (id, version, organization_id, role_name, data_scope, created_at, updated_at) VALUES
    ('r0000000-0000-0000-0000-000000000011', 0, 'd0000000-0000-0000-0000-000000000002', 'OWNER',   'ORGANIZATION', NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000012', 0, 'd0000000-0000-0000-0000-000000000002', 'MANAGER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000013', 0, 'd0000000-0000-0000-0000-000000000002', 'CASHIER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000014', 0, 'd0000000-0000-0000-0000-000000000002', 'WAITER',  'SELF',         NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000015', 0, 'd0000000-0000-0000-0000-000000000002', 'CHEF',    'SELF',         NOW(), NOW())
    ON CONFLICT (organization_id, role_name) DO NOTHING;

-- BBQ Garden (d003) roles
INSERT INTO org_roles (id, version, organization_id, role_name, data_scope, created_at, updated_at) VALUES
    ('r0000000-0000-0000-0000-000000000021', 0, 'd0000000-0000-0000-0000-000000000003', 'OWNER',   'ORGANIZATION', NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000022', 0, 'd0000000-0000-0000-0000-000000000003', 'MANAGER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000023', 0, 'd0000000-0000-0000-0000-000000000003', 'CASHIER', 'BRANCH',       NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000024', 0, 'd0000000-0000-0000-0000-000000000003', 'WAITER',  'SELF',         NOW(), NOW()),
    ('r0000000-0000-0000-0000-000000000025', 0, 'd0000000-0000-0000-0000-000000000003', 'CHEF',    'SELF',         NOW(), NOW())
    ON CONFLICT (organization_id, role_name) DO NOTHING;

-- =============================================================================
-- 8. ERP MODULE: ORG_ROLE <-> ORG_PERMISSION (org_roles_org_permissions)
-- Uses cross-join to map permissions to ALL per-org roles at once.
-- =============================================================================

-- OWNER: all org permissions (per-org OWNER roles: r001, r011, r021)
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r CROSS JOIN org_permissions p
WHERE r.role_name = 'OWNER'
    ON CONFLICT DO NOTHING;

-- MANAGER: daily operations (per-org MANAGER roles: r002, r012, r022)
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r CROSS JOIN org_permissions p
WHERE r.role_name = 'MANAGER'
  AND p.permission_name IN (
    'ORDER_READ', 'ORDER_CREATE', 'ORDER_UPDATE',
    'PAYMENT_READ', 'PAYMENT_CREATE',
    'MENU_MANAGE', 'TABLE_MANAGE', 'REPORT_VIEW',
    'SCHEDULE_STAFF_READ', 'SCHEDULE_MANAGE', 'ATTENDANCE_SELF_READ',
    'EMPLOYEE_DELETE', 'EMPLOYEE_ROLE_ASSIGN', 'EMPLOYEE_ROLE_REVOKE',
    'TABLE_MAP_READ', 'TABLE_SEARCH_READ',
    'INVENTORY_CATEGORY_VIEW', 'INVENTORY_CATEGORY_MANAGE',
    'INVENTORY_VIEW', 'INVENTORY_MANAGE',
    'INVENTORY_TRANSACTION_VIEW', 'INVENTORY_TRANSACTION_MANAGE'
)
    ON CONFLICT DO NOTHING;

-- CASHIER: orders + payments (per-org CASHIER roles: r003, r013, r023)
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r CROSS JOIN org_permissions p
WHERE r.role_name = 'CASHIER'
  AND p.permission_name IN (
    'ORDER_READ', 'ORDER_CREATE', 'PAYMENT_READ', 'PAYMENT_CREATE',
    'SCHEDULE_STAFF_READ', 'SCHEDULE_MANAGE', 'ATTENDANCE_SELF_READ',
    'TABLE_MAP_READ', 'TABLE_SEARCH_READ'
)
    ON CONFLICT DO NOTHING;

-- WAITER: order read + create (per-org WAITER roles: r004, r014, r024)
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r CROSS JOIN org_permissions p
WHERE r.role_name = 'WAITER'
  AND p.permission_name IN (
    'ORDER_READ', 'ORDER_CREATE', 'SCHEDULE_STAFF_READ', 'SCHEDULE_MANAGE',
    'ATTENDANCE_SELF_READ', 'TABLE_MAP_READ', 'TABLE_SEARCH_READ'
)
    ON CONFLICT DO NOTHING;

-- CHEF: order read + update (per-org CHEF roles: r005, r015, r025)
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
SELECT r.id, p.id
FROM org_roles r CROSS JOIN org_permissions p
WHERE r.role_name = 'CHEF'
  AND p.permission_name IN (
    'ORDER_READ', 'ORDER_UPDATE', 'SCHEDULE_STAFF_READ', 'SCHEDULE_MANAGE',
    'ATTENDANCE_SELF_READ', 'TABLE_MAP_READ', 'TABLE_SEARCH_READ'
)
    ON CONFLICT DO NOTHING;


-- 9. ORGANIZATIONS
-- =============================================================================

-- Owner A: 1 organization
INSERT INTO organizations (id, version, owner_id, organization_name, tax_code, address, phone, email, status, created_at, updated_at) VALUES
    ('d0000000-0000-0000-0000-000000000001', 0, 'c0000000-0000-0000-0000-000000000002', 'Phở Việt Chain', 'TAX-A001', '123 Lê Lợi, Q1, TP.HCM', '0901000001', 'phoviet@restaurant.com', 'ACTIVE', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Owner B: 2 organizations
INSERT INTO organizations (id, version, owner_id, organization_name, tax_code, address, phone, email, status, created_at, updated_at) VALUES
                                                                                                                                          ('d0000000-0000-0000-0000-000000000002', 0, 'c0000000-0000-0000-0000-000000000003', 'Sushi Tokyo Group', 'TAX-B001', '456 Nguyễn Huệ, Q1, TP.HCM', '0902000001', 'sushitokyo@restaurant.com', 'ACTIVE', NOW(), NOW()),
                                                                                                                                          ('d0000000-0000-0000-0000-000000000003', 0, 'c0000000-0000-0000-0000-000000000003', 'BBQ Garden',       'TAX-B002', '789 Cách Mạng T8, Q3, TP.HCM', '0902000002', 'bbqgarden@restaurant.com', 'ACTIVE', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 10. ORGANIZATION BRANCHES
-- =============================================================================

-- Owner A branches (Phở Việt Chain)
INSERT INTO organization_branches (id, version, organization_id, manager_id, branch_name, address, phone, status, created_at, updated_at) VALUES
                                                                                                                                              ('e0000000-0000-0000-0000-000000000001', 0, 'd0000000-0000-0000-0000-000000000001', NULL, 'Phở Việt - Chi nhánh Q1', '123 Lê Lợi, Q1, TP.HCM', '0901001001', 'ACTIVE', NOW(), NOW()),
                                                                                                                                              ('e0000000-0000-0000-0000-000000000002', 0, 'd0000000-0000-0000-0000-000000000001', NULL, 'Phở Việt - Chi nhánh Q7', '56 Nguyễn Thị Thập, Q7, TP.HCM', '0901001002', 'ACTIVE', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Owner B branches - org1 (Sushi Tokyo Group)
INSERT INTO organization_branches (id, version, organization_id, manager_id, branch_name, address, phone, status, created_at, updated_at) VALUES
                                                                                                                                              ('e0000000-0000-0000-0000-000000000003', 0, 'd0000000-0000-0000-0000-000000000002', NULL, 'Sushi Tokyo - Nguyễn Huệ', '456 Nguyễn Huệ, Q1, TP.HCM', '0902001001', 'ACTIVE', NOW(), NOW()),
                                                                                                                                              ('e0000000-0000-0000-0000-000000000004', 0, 'd0000000-0000-0000-0000-000000000002', NULL, 'Sushi Tokyo - Thủ Đức', '100 Võ Văn Ngân, Thủ Đức, TP.HCM', '0902001002', 'ACTIVE', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Owner B branches - org2 (BBQ Garden)
INSERT INTO organization_branches (id, version, organization_id, manager_id, branch_name, address, phone, status, created_at, updated_at) VALUES
    ('e0000000-0000-0000-0000-000000000005', 0, 'd0000000-0000-0000-0000-000000000003', NULL, 'BBQ Garden - Q3', '789 Cách Mạng T8, Q3, TP.HCM', '0903001001', 'ACTIVE', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 11. EMPLOYEES
-- =============================================================================

-- Manager user as employee at 3 branches:
--   f001: MANAGER @ Phở Việt Q1       (org d001, role r002)
--   f002: CASHIER @ Sushi Tokyo Huế   (org d002, role r013)
--   f003: MANAGER @ BBQ Garden Q3     (org d003, role r022)
INSERT INTO employees (id, version, user_id, org_role_id, organization_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000001', 0, 'c0000000-0000-0000-0000-000000000004', 'r0000000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'manager@restaurant.com', '0904000001', '2024-01-15', NULL, NOW(), NOW()),
    ('f0000000-0000-0000-0000-000000000002', 0, 'c0000000-0000-0000-0000-000000000004', 'r0000000-0000-0000-0000-000000000013', 'd0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000003', 'ACTIVE', 'manager@restaurant.com', '0904000001', '2024-03-01', NULL, NOW(), NOW()),
    ('f0000000-0000-0000-0000-000000000003', 0, 'c0000000-0000-0000-0000-000000000004', 'r0000000-0000-0000-0000-000000000022', 'd0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000005', 'ACTIVE', 'manager@restaurant.com', '0904000001', '2024-06-01', NULL, NOW(), NOW()),
    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 12. UPDATE BRANCH MANAGER REFERENCES
-- =============================================================================

UPDATE organization_branches SET manager_id = 'f0000000-0000-0000-0000-000000000001' WHERE id = 'e0000000-0000-0000-0000-000000000001';
UPDATE organization_branches SET manager_id = 'f0000000-0000-0000-0000-000000000002' WHERE id = 'e0000000-0000-0000-0000-000000000003';
UPDATE organization_branches SET manager_id = 'f0000000-0000-0000-0000-000000000003' WHERE id = 'e0000000-0000-0000-0000-000000000005';

-- =============================================================================
-- 13. ADDITIONAL SEED DATA FOR KITCHEN READY-TO-SERVE TESTING (uc-sw-14)
-- =============================================================================


-- Chef user: username = chef_q1, email = chef_q1@restaurant.com
INSERT INTO users (id, version, username, password, email, status, enabled, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000008', 0, 'chef_q1', '$2a$10$LCKw9m993mk/Hz4v7C5u0u4ye3RA.GqVzpd9SC30euP/8pztZdxZq', 'chef_q1@restaurant.com', 'ACTIVE', true, NOW(), NOW())
    ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000002')
    ON CONFLICT DO NOTHING;

-- Waiter user: username = waiter_q1, email = waiter_q1@restaurant.com
INSERT INTO users (id, version, username, password, email, status, enabled, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000009', 0, 'waiter_q1', '$2a$10$LCKw9m993mk/Hz4v7C5u0u4ye3RA.GqVzpd9SC30euP/8pztZdxZq', 'waiter_q1@restaurant.com', 'ACTIVE', true, NOW(), NOW())
    ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000002')
    ON CONFLICT DO NOTHING;

-- Chef 2 user: username = chef_q2, email = chef_q2@restaurant.com
INSERT INTO users (id, version, username, password, email, status, enabled, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000010', 0, 'chef_q2', '$2a$10$LCKw9m993mk/Hz4v7C5u0u4ye3RA.GqVzpd9SC30euP/8pztZdxZq', 'chef_q2@restaurant.com', 'ACTIVE', true, NOW(), NOW())
    ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000002')
    ON CONFLICT DO NOTHING;

-- Employees:
-- Employees:
-- Chef at Phở Việt Q1 (org d001, role r005)
INSERT INTO employees (id, version, user_id, org_role_id, organization_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000008', 0, 'c0000000-0000-0000-0000-000000000008', 'r0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'chef_q1@restaurant.com', '0905000001', '2024-01-15', NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Waiter at Phở Việt Q1 (org d001, role r004)
INSERT INTO employees (id, version, user_id, org_role_id, organization_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000009', 0, 'c0000000-0000-0000-0000-000000000009', 'r0000000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'waiter_q1@restaurant.com', '0905000002', '2024-01-15', NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Chef 2 at Phở Việt Q1 (org d001, role r005)
INSERT INTO employees (id, version, user_id, org_role_id, organization_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000010', 0, 'c0000000-0000-0000-0000-000000000010', 'r0000000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'chef_q2@restaurant.com', '0905000003', '2024-01-15', NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;






-- =========================================================================
-- 16. BUSINESS TEST DATA: WBS 69 & WBS 70
-- =========================================================================
-- 16.1. Table Areas
INSERT INTO table_areas (
    area_id,
    version,
    branch_id,
    area_name,
    description,
    display_order,
    created_at,
    updated_at
)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    0,
    'e0000000-0000-0000-0000-000000000001',
    'Main Area',
    'Default dining area',
    1,
    NOW(),
    NOW()
);

-- 16.1. Table Areas
INSERT INTO table_areas (area_id, branch_id, area_name, version) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'Khu Vực A (Tầng trệt)', 0),
    ('a0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003', 'Khu A - Sushi Tokyo', 0)
    ON CONFLICT (area_id) DO NOTHING;

-- 16.2. Restaurant Tables
INSERT INTO restaurant_tables (table_id, area_id, table_number, capacity, status, version) VALUES
                                                                                               ('t0000000-0000-0000-0000-000000000101', 'a0000000-0000-0000-0000-000000000001', '101', 2, 'AVAILABLE', 0),
                                                                                               ('t0000000-0000-0000-0000-000000000102', 'a0000000-0000-0000-0000-000000000001', '102', 4, 'AVAILABLE', 0),
                                                                                               ('t0000000-0000-0000-0000-000000000103', 'a0000000-0000-0000-0000-000000000001', '103', 6, 'AVAILABLE', 0),
                                                                                               ('t0000000-0000-0000-0000-000000000104', 'a0000000-0000-0000-0000-000000000001', '104', 8, 'AVAILABLE', 0),
                                                                                               ('t0000000-0000-0000-0000-000000000301', 'a0000000-0000-0000-0000-000000000003', '301', 4, 'AVAILABLE', 0),
                                                                                               ('t0000000-0000-0000-0000-000000000302', 'a0000000-0000-0000-0000-000000000003', '302', 2, 'AVAILABLE', 0)
    ON CONFLICT (area_id, table_number) DO NOTHING;

-- 16.3. Test Customers
INSERT INTO customers (id, phone, status, version, created_at, updated_at) VALUES
                                                                               ('c0000000-0000-0000-0000-000000000001', '0987654321', 'ACTIVE', 0, NOW(), NOW()),
                                                                               ('c0000000-0000-0000-0000-000000000002', '0912345678', 'ACTIVE', 0, NOW(), NOW()),
                                                                               ('c0000000-0000-0000-0000-000000000099', '0966888888', 'ACTIVE', 0, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- 16.4. Bookings (WBS 69)
INSERT INTO bookings (id, branch_id, table_id, customer_id, booking_time, guest_count, status, note, version) VALUES
                                                                                                                  (
                                                                                                                      'b0000000-0000-0000-0000-000000000001',
                                                                                                                      'e0000000-0000-0000-0000-000000000001',
                                                                                                                      't0000000-0000-0000-0000-000000000102',
                                                                                                                      'c0000000-0000-0000-0000-000000000001',
                                                                                                                      '2026-07-30 19:00:00',
                                                                                                                      3,
                                                                                                                      'CONFIRMED',
                                                                                                                      'Khách đặt trước ăn tối gia đình. Lưu ý: Ngồi cạnh cửa sổ.',
                                                                                                                      0
                                                                                                                  ),
                                                                                                                  (
                                                                                                                      'b0000000-0000-0000-0000-000000000002',
                                                                                                                      'e0000000-0000-0000-0000-000000000001',
                                                                                                                      't0000000-0000-0000-0000-000000000103',
                                                                                                                      'c0000000-0000-0000-0000-000000000002',
                                                                                                                      '2026-07-30 20:30:00',
                                                                                                                      5,
                                                                                                                      'PENDING',
                                                                                                                      'Sinh nhật anh Nam. Có mang theo bánh kem.',
                                                                                                                      0
                                                                                                                  )
    ON CONFLICT (id) DO NOTHING;

-- 16.5. Customer Point Wallet (WBS 70 - Chain-wide Organization ID: d0000000-0000-0000-0000-000000000001)
INSERT INTO customer_point (id, version, customer_id, organization_id, current_points, lifetime_points, created_at, updated_at)
VALUES (
           'cp000000-0000-0000-0000-000000000001',
           0,
           'c0000000-0000-0000-0000-000000000099',
           'd0000000-0000-0000-0000-000000000001',
           500,
           1000,
           NOW(),
           NOW()
       )
    ON CONFLICT (id) DO NOTHING;

-- 16.6. Point Wallet History (WBS 70)
INSERT INTO customer_point_history (id, version, customer_id, organization_id, transaction_type, points_changed, reference_id, created_at, updated_at) VALUES
                                                                                                                                                           ('cph00000-0000-0000-0000-000000000001', 0, 'c0000000-0000-0000-0000-000000000099', 'd0000000-0000-0000-0000-000000000001', 'EARN', 600, 'BILL-1001', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
                                                                                                                                                           ('cph00000-0000-0000-0000-000000000002', 0, 'c0000000-0000-0000-0000-000000000099', 'd0000000-0000-0000-0000-000000000001', 'EARN', 400, 'BILL-1002', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days'),
                                                                                                                                                           ('cph00000-0000-0000-0000-000000000003', 0, 'c0000000-0000-0000-0000-000000000099', 'd0000000-0000-0000-0000-000000000001', 'REDEEM', -500, 'VOU-888888', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day')
    ON CONFLICT (id) DO NOTHING;

-- 16.7. System Vouchers Catalog (WBS 70 - Branch-specific Branch ID: e0000000-0000-0000-0000-000000000001)
INSERT INTO vouchers (id, version, branch_id, title, discount_percent, min_bill_amount, points_required, is_active, expired_at, created_at, updated_at) VALUES
                                                                                                                                                            ('v0000000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', '🎁 Voucher Giảm 10% Chào Mới (Miễn phí 0 Điểm)', 10, 0.00, 0, 1, NOW() + INTERVAL '30 days', NOW(), NOW()),
                                                                                                                                                            ('v0000000-0000-0000-0000-000000000002', 0, 'e0000000-0000-0000-0000-000000000001', '👑 Voucher VIP Giảm 30% (Yêu cầu 100 Điểm)', 30, 50000.00, 100, 1, NOW() + INTERVAL '60 days', NOW(), NOW()),
                                                                                                                                                            ('v0000000-0000-0000-0000-000000000003', 0, 'e0000000-0000-0000-0000-000000000001', '💎 Voucher Kim Cương Giảm 50% (Yêu cầu 300 Điểm)', 50, 150000.00, 300, 1, NOW() + INTERVAL '90 days', NOW(), NOW()),
                                                                                                                                                            ('v0000000-0000-0000-0000-000000000004', 0, 'e0000000-0000-0000-0000-000000000003', '🎁 Voucher Sushi 10% Chào Mới (Miễn phí 0 Điểm)', 10, 0.00, 0, 1, NOW() + INTERVAL '30 days', NOW(), NOW()),
                                                                                                                                                            ('v0000000-0000-0000-0000-000000000005', 0, 'e0000000-0000-0000-0000-000000000003', '👑 Voucher Sushi VIP 30% (Yêu cầu 100 Điểm)', 30, 50000.00, 100, 1, NOW() + INTERVAL '60 days', NOW(), NOW()),
                                                                                                                                                            ('v0000000-0000-0000-0000-000000000006', 0, 'e0000000-0000-0000-0000-000000000003', '💎 Voucher Sushi Kim Cương 50% (Yêu cầu 300 Điểm)', 50, 150000.00, 300, 1, NOW() + INTERVAL '90 days', NOW(), NOW())
    ON CONFLICT (id) DO UPDATE SET
    title = EXCLUDED.title,
                            discount_percent = EXCLUDED.discount_percent,
                            min_bill_amount = EXCLUDED.min_bill_amount,
                            points_required = EXCLUDED.points_required,
                            expired_at = EXCLUDED.expired_at;

-- ================
-- CATEGORIES & PRODUCTS SEED DATA FOR DIGITAL MENU (uc-c-04)
-- ================
INSERT INTO categories (category_id, version, branch_id, category_name, description, display_order, created_at, updated_at) VALUES
                                                                                                                                ('cat00000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', 'Phở & Bún', 'Các món Phở truyền thống Việt Nam', 1, NOW(), NOW()),
                                                                                                                                ('cat00000-0000-0000-0000-000000000002', 0, 'e0000000-0000-0000-0000-000000000001', 'Đồ Uống', 'Nước giải khát & Trà thanh nhiệt', 2, NOW(), NOW())
    ON CONFLICT (category_id) DO NOTHING;

INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
                                                                                                                                                                          ('prd00000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Phở Bò Tái', 'Phở bò tái truyền thống với nước dùng đậm đà', 55000.00, NULL, 'AVAILABLE', true, NOW(), NOW()),
                                                                                                                                                                          ('prd00000-0000-0000-0000-000000000002', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Phở Gà Đặc Biệt', 'Phở gà ta thịt đùi xé phay kèm trứng non', 65000.00, NULL, 'AVAILABLE', true, NOW(), NOW()),
                                                                                                                                                                          ('prd00000-0000-0000-0000-000000000003', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000002', 'Trà Đá', 'Trà đá ướp hoa lài ướp lạnh', 5000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
                                                                                                                                                                          ('prd00000-0000-0000-0000-000000000004', 0, 'e0000000-0000-0000-0000-000000000002', 'cat00000-0000-0000-0000-000000000002', 'Nước Cam Ép', 'Cam sành ép tươi 100% nguyên chất', 25000.00, NULL, 'AVAILABLE', false, NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- 16.9. Tự động gắn Voucher 0 điểm mẫu cho tất cả Khách hàng hiện có (nếu chưa có trong ví)
INSERT INTO customer_vouchers (id, version, customer_id, branch_id, voucher_id, voucher_sn, status, created_at, updated_at)
SELECT
    gen_random_uuid(), 0, c.id, 'e0000000-0000-0000-0000-000000000001',
    'v0000000-0000-0000-0000-000000000001', 'VFREE' || substring(gen_random_uuid()::text, 1, 8),
    'AVAILABLE', NOW(), NOW()
FROM customers c
WHERE NOT EXISTS (
    SELECT 1 FROM customer_vouchers cv WHERE cv.customer_id = c.id AND cv.voucher_id = 'v0000000-0000-0000-0000-000000000001'
);

INSERT INTO customer_vouchers (id, version, customer_id, branch_id, voucher_id, voucher_sn, status, created_at, updated_at)
SELECT
    gen_random_uuid(), 0, c.id, 'e0000000-0000-0000-0000-000000000003',
    'v0000000-0000-0000-0000-000000000004', 'VSUSHI' || substring(gen_random_uuid()::text, 1, 8),
    'AVAILABLE', NOW(), NOW()
FROM customers c
WHERE NOT EXISTS (
    SELECT 1 FROM customer_vouchers cv WHERE cv.customer_id = c.id AND cv.voucher_id = 'v0000000-0000-0000-0000-000000000004'
);

-- ============================================================================
-- CÁC CÂU LỆNH HỖ TRỢ TEST & RESET (MẶC ĐỊNH ĐÃ COMMENT OUT KHÓA LẠI)
-- (Mở comment '--' trước câu lệnh bạn muốn chạy trong DBeaver/pgAdmin khi test)
-- ============================================================================

-- 1. Lệnh Redis xóa session (chạy trong Terminal):
-- docker exec -it redis-crm redis-cli flushall

-- 2. XÓA SẠCH 100% TẤT CẢ ĐƠN HÀNG VÀ MÓN ĂN CŨ (Reset từ đầu như hệ thống mới):
-- TRUNCATE TABLE order_item_modifiers, order_items, orders RESTART IDENTITY CASCADE;
-- UPDATE restaurant_tables SET status = 'AVAILABLE', updated_at = NOW();

-- 3. Reset Bàn ăn về AVAILABLE & Hủy đơn dở dang cũ (Nếu không muốn xóa hẳn dữ liệu đơn cũ):
-- UPDATE restaurant_tables SET status = 'AVAILABLE', updated_at = NOW();
-- UPDATE orders SET status = 'CANCELLED', updated_at = NOW() WHERE status = 'PENDING';
-- UPDATE order_items SET status = 'CANCELLED', updated_at = NOW() WHERE status = 'PENDING';

-- 3. Reset Voucher của tất cả khách hàng về AVAILABLE (để test lại luồng áp dụng voucher):
-- UPDATE customer_vouchers SET status = 'AVAILABLE', used_at = NULL, order_id = NULL, updated_at = NOW();
-- UPDATE orders SET discount_amount = 0.00, total_amount = subtotal, updated_at = NOW() WHERE status = 'PENDING';

-- 4. Xem danh sách các món trong đơn hàng đang mở (join với bảng products để xem tên món):
-- SELECT oi.id, p.product_name, oi.quantity, oi.status
-- FROM order_items oi
-- LEFT JOIN products p ON oi.product_id = p.product_id
-- WHERE oi.status != 'CANCELLED'
-- ORDER BY oi.created_at DESC;

-- 5. Cập nhật trạng thái cho TỪNG MÓN CỤ THỂ theo tên món:
-- 5.1. Chuyển 'Phở Bò Tái' sang '🔥 Đang nấu' (IN_PROGRESS):
-- UPDATE order_items SET status = 'IN_PROGRESS', updated_at = NOW()
-- WHERE product_id IN (SELECT product_id FROM products WHERE product_name LIKE '%Phở Bò Tái%') AND status = 'PENDING';

-- 5.2. Chuyển 'Phở Bò Tái' sang '✅ Sẵn sàng phục vụ' (READY_TO_SERVE) + Bắn Thông báo cho Phục vụ:
-- UPDATE order_items SET status = 'READY_TO_SERVE', updated_at = NOW()
-- WHERE product_id IN (SELECT product_id FROM products WHERE product_name LIKE '%Phở Bò Tái%') AND status IN ('PENDING', 'IN_PROGRESS');

-- (Nếu test bằng SQL, chạy thêm câu lệnh INSERT bên dưới để hiện Thẻ Thông Báo cho Nhân Viên Phục Vụ):
-- INSERT INTO notifications (id, version, branch_id, recipient_id, sender_id, title, content, type, status, created_at, updated_at)
-- VALUES (gen_random_uuid(), 0, 'e0000000-0000-0000-0000-000000000001', NULL, 'f0000000-0000-0000-0000-000000000008', 'Dish Ready to Serve', 'Khu Vực A (Tầng trệt) - Bàn 101: Phở Bò Tái x1 đã sẵn sàng phục vụ!', 'READY_TO_SERVE', 'UNREAD', NOW(), NOW());

-- 5.3. Chuyển 'Phở Bò Tái' sang '🍽️ Đã phục vụ' (SERVED):
-- UPDATE order_items SET status = 'SERVED', updated_at = NOW()
-- WHERE product_id IN (SELECT product_id FROM products WHERE product_name LIKE '%Phở Bò Tái%') AND status IN ('PENDING', 'IN_PROGRESS', 'READY_TO_SERVE');

-- 5.4. Hủy 1 món cụ thể theo tên ('Nước Cam Ép'):
-- UPDATE order_items SET status = 'CANCELLED', updated_at = NOW()
-- WHERE product_id IN (SELECT product_id FROM products WHERE product_name LIKE '%Nước Cam Ép%') AND status = 'PENDING';

-- 6. Cập nhật trạng thái HÀNG LOẠT CHO TẤT CẢ MÓN CÙNG LÚC:
-- UPDATE order_items SET status = 'IN_PROGRESS', updated_at = NOW() WHERE status = 'PENDING';
-- UPDATE order_items SET status = 'READY_TO_SERVE', updated_at = NOW() WHERE status IN ('PENDING', 'IN_PROGRESS');
-- UPDATE order_items SET status = 'SERVED', updated_at = NOW() WHERE status IN ('PENDING', 'IN_PROGRESS', 'READY_TO_SERVE');

-- ==========================================
-- INVENTORY CATEGORY
-- ==========================================

INSERT INTO inventory_categories (
    id,
    version,
    branch_id,
    category_name,
    description
)
VALUES
(
    '11111111-1111-1111-1111-111111111111',
    0,
    'e0000000-0000-0000-0000-000000000001',
    'Vegetables',
    'Fresh vegetables'
),
(
    '22222222-2222-2222-2222-222222222222',
    0,
    'e0000000-0000-0000-0000-000000000001',
    'Meat',
    'Fresh meat products'
),
(
    '33333333-3333-3333-3333-333333333333',
    0,
    'e0000000-0000-0000-0000-000000000001',
    'Beverages',
    'Drinks and beverages'
);

-- ==========================================
-- INVENTORY
-- ==========================================

INSERT INTO inventories (
    id,
    version,
    branch_id,
    inventory_category_id,
    inventory_name,
    unit,
    description,
    quantity,
    minimum_quantity,
    status
)
VALUES
(
    '44444444-4444-4444-4444-444444444444',
    0,
    'e0000000-0000-0000-0000-000000000001',
    '11111111-1111-1111-1111-111111111111',
    'Tomato',
    'kg',
    'Fresh tomato',
    80,
    20,
    'GOOD'
),
(
    '55555555-5555-5555-5555-555555555555',
    0,
    'e0000000-0000-0000-0000-000000000001',
    '11111111-1111-1111-1111-111111111111',
    'Potato',
    'kg',
    'Yellow potato',
    10,
    20,
    'LOW'
),
(
    '66666666-6666-6666-6666-666666666666',
    0,
    'e0000000-0000-0000-0000-000000000001',
    '22222222-2222-2222-2222-222222222222',
    'Chicken Breast',
    'kg',
    'Boneless chicken breast',
    0,
    15,
    'OUT_OF_STOCK'
),
(
    '77777777-7777-7777-7777-777777777777',
    0,
    'e0000000-0000-0000-0000-000000000001',
    '33333333-3333-3333-3333-333333333333',
    'Coca Cola',
    'can',
    '330ml can',
    120,
    30,
    'GOOD'
);

-- ==========================================
-- INVENTORY TRANSACTION
-- ==========================================
INSERT INTO inventory_transactions (
    id,
    version,
    inventory_id,
    employee_id,
    transaction_type,
    transaction_direction,
    quantity,
    note,
    transaction_time
)
VALUES
(
    '88888888-8888-8888-8888-888888888888',
    0,
    '44444444-4444-4444-4444-444444444444',
    'f0000000-0000-0000-0000-000000000001',
    'PURCHASE',
    'IN',
    100,
    'Purchased from supplier',
    NOW() - INTERVAL '7 days'
),
(
    '99999999-9999-9999-9999-999999999999',
    0,
    '44444444-4444-4444-4444-444444444444',
    'f0000000-0000-0000-0000-000000000001',
    'SALE',
    'OUT',
    20,
    'Used in kitchen',
    NOW() - INTERVAL '3 days'
),
(
    'aaaaaaaa-1111-2222-3333-444444444444',
    0,
    '55555555-5555-5555-5555-555555555555',
    'f0000000-0000-0000-0000-000000000001',
    'SALE',
    'OUT',
    15,
    'Daily cooking',
    NOW() - INTERVAL '2 days'
),
(
    'cccccccc-1111-2222-3333-444444444444',
    0,
    '66666666-6666-6666-6666-666666666666',
    'f0000000-0000-0000-0000-000000000001',
    'PURCHASE',
    'IN',
    50,
    'Restocked chicken',
    NOW() - INTERVAL '1 day'
),
(
    'dddddddd-1111-2222-3333-444444444444',
    0,
    '77777777-7777-7777-7777-777777777777',
    'f0000000-0000-0000-0000-000000000001',
    'ADJUSTMENT',
    'IN',
    10,
    'Inventory correction',
    NOW()
);