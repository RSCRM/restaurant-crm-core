

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
                                                                                       ('p0000000-0000-0000-0000-000000000301', 0, 'KITCHEN_ORDER_READ',  NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000302', 0, 'KITCHEN_ITEM_ACCEPT', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000303', 0, 'KITCHEN_ITEM_UPDATE', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000304', 0, 'KITCHEN_ITEM_CANCEL', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000305', 0, 'KITCHEN_ITEM_RELEASE', NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000306', 0, 'SERVICE_ITEM_READY',  NOW(), NOW()),
                                                                                       ('p0000000-0000-0000-0000-000000000307', 0, 'SERVICE_ITEM_SERVE',  NOW(), NOW())
    ON CONFLICT (permission_name) DO NOTHING;

-- =============================================================================
-- 7. ERP MODULE: ORG_ROLES
-- =============================================================================

INSERT INTO org_roles (id, version, role_name, data_scope, created_at, updated_at) VALUES
                                                                                       ('r0000000-0000-0000-0000-000000000001', 0, 'OWNER',   'ORGANIZATION', NOW(), NOW()),
                                                                                       ('r0000000-0000-0000-0000-000000000002', 0, 'MANAGER', 'BRANCH',       NOW(), NOW()),
                                                                                       ('r0000000-0000-0000-0000-000000000003', 0, 'CASHIER', 'BRANCH',       NOW(), NOW()),
                                                                                       ('r0000000-0000-0000-0000-000000000004', 0, 'WAITER',  'SELF',         NOW(), NOW()),
                                                                                       ('r0000000-0000-0000-0000-000000000005', 0, 'CHEF',    'SELF',         NOW(), NOW())
    ON CONFLICT (role_name) DO NOTHING;

-- =============================================================================
-- 8. ERP MODULE: ORG_ROLE <-> ORG_PERMISSION (org_roles_org_permissions)
-- =============================================================================

-- OWNER: all org permissions
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id) VALUES
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000001'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000002'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000003'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000004'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000005'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000006'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000007'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000008'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000009'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000010'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000011'),
                                                                            ('r0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000012')
    ON CONFLICT DO NOTHING;

-- MANAGER: daily operations
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id) VALUES
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000001'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000002'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000003'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000005'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000006'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000007'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000008'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000009'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000013'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000014'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000015'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000016'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000017'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000018'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000101'),
                                                                            ('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000102')
    ON CONFLICT DO NOTHING;


-- CASHIER: orders + payments
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id) VALUES
                                                                            ('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000001'),
                                                                            ('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000002'),
                                                                            ('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000005'),
                                                                            ('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000006'),
                                                                            ('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000013'),
                                                                            ('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000014'),
                                                                            ('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000015'),
                                                                            ('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000101'),
                                                                            ('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000102')
    ON CONFLICT DO NOTHING;

-- WAITER: order read + create + KDS service (ready/serve)
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id) VALUES
                                                                            ('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000001'),
                                                                            ('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000002'),
                                                                            ('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000013'),
                                                                            ('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000014'),
                                                                            ('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000015'),
                                                                            ('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000101'),
                                                                            ('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000102'),
                                                                            ('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000306'),
                                                                            ('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000307')
    ON CONFLICT DO NOTHING;

-- CHEF: order read + update + KDS kitchen (read/accept/complete/cancel/release)
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id) VALUES
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000001'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000003'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000013'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000014'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000015'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000101'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000102'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000301'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000302'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000303'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000304'),
                                                                            ('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000305')
    ON CONFLICT DO NOTHING;

-- =============================================================================
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
--   f001: MANAGER @ Phở Việt Q1       (Owner A)
--   f002: CASHIER @ Sushi Tokyo Huệ   (Owner B, org1)
--   f003: MANAGER @ BBQ Garden Q3     (Owner B, org2)
INSERT INTO employees (id, version, user_id, org_role_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
                                                                                                                                             ('f0000000-0000-0000-0000-000000000001', 0, 'c0000000-0000-0000-0000-000000000004', 'r0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'manager@restaurant.com', '0904000001', '2024-01-15', NULL, NOW(), NOW()),
                                                                                                                                             ('f0000000-0000-0000-0000-000000000002', 0, 'c0000000-0000-0000-0000-000000000004', 'r0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003', 'ACTIVE', 'manager@restaurant.com', '0904000001', '2024-03-01', NULL, NOW(), NOW()),
                                                                                                                                             ('f0000000-0000-0000-0000-000000000003', 0, 'c0000000-0000-0000-0000-000000000004', 'r0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000005', 'ACTIVE', 'manager@restaurant.com', '0904000001', '2024-06-01', NULL, NOW(), NOW())
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

-- Add CHEF role
INSERT INTO org_roles (id, version, role_name, data_scope, created_at, updated_at) VALUES
    ('r0000000-0000-0000-0000-000000000005', 0, 'CHEF', 'SELF', NOW(), NOW())
    ON CONFLICT (role_name) DO NOTHING;

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
-- Chef at Phở Việt Q1
INSERT INTO employees (id, version, user_id, org_role_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000008', 0, 'c0000000-0000-0000-0000-000000000008', 'r0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'chef_q1@restaurant.com', '0905000001', '2024-01-15', NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Waiter at Phở Việt Q1
INSERT INTO employees (id, version, user_id, org_role_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000009', 0, 'c0000000-0000-0000-0000-000000000009', 'r0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'waiter_q1@restaurant.com', '0905000002', '2024-01-15', NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Chef 2 at Phở Việt Q1
INSERT INTO employees (id, version, user_id, org_role_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000010', 0, 'c0000000-0000-0000-0000-000000000010', 'r0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'chef_q2@restaurant.com', '0905000003', '2024-01-15', NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;



