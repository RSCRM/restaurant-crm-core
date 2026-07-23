-- =============================================================================
-- SEED DATA: Authentication & Context-Based Authorization Test Scenarios
-- =============================================================================

-- Ensure non-Hibernate entities tables exist
CREATE TABLE IF NOT EXISTS owners (
    owner_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS product_categories (
    category_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    branch_id UUID NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Clean up existing tables to prevent foreign key & duplicate key violations
TRUNCATE TABLE 
    order_items,
    orders,
    restaurant_tables,
    table_areas,
    products,
    product_categories,
    employees,
    org_roles_org_permissions,
    org_roles,
    org_permissions,
    organization_branches,
    organizations,
    owners,
    user_roles,
    roles_permissions,
    users,
    roles,
    permissions 
    CASCADE;

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

INSERT INTO roles (id, version, role_name, created_at, updated_at) VALUES
('b0000000-0000-0000-0000-000000000001', 0, 'ADMIN',  NOW(), NOW()),
('b0000000-0000-0000-0000-000000000002', 0, 'USER',   NOW(), NOW())
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
('p0000000-0000-0000-0000-000000000012', 0, 'ORG_MANAGE',      NOW(), NOW())
ON CONFLICT (permission_name) DO NOTHING;

-- =============================================================================
-- 7. ERP MODULE: ORG_ROLES
-- =============================================================================

INSERT INTO org_roles (id, version, role_name, created_at, updated_at) VALUES
('r0000000-0000-0000-0000-000000000001', 0, 'OWNER',   NOW(), NOW()),
('r0000000-0000-0000-0000-000000000002', 0, 'MANAGER', NOW(), NOW()),
('r0000000-0000-0000-0000-000000000003', 0, 'CASHIER', NOW(), NOW()),
('r0000000-0000-0000-0000-000000000004', 0, 'WAITER',  NOW(), NOW()),
('r0000000-0000-0000-0000-000000000005', 0, 'CHEF',    NOW(), NOW())
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
('r0000000-0000-0000-0000-000000000002', 'p0000000-0000-0000-0000-000000000009')
ON CONFLICT DO NOTHING;

-- CASHIER: orders + payments
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id) VALUES
('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000001'),
('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000002'),
('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000005'),
('r0000000-0000-0000-0000-000000000003', 'p0000000-0000-0000-0000-000000000006')
ON CONFLICT DO NOTHING;

-- WAITER: order read + create
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id) VALUES
('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000001'),
('r0000000-0000-0000-0000-000000000004', 'p0000000-0000-0000-0000-000000000002')
ON CONFLICT DO NOTHING;

-- CHEF: order read + update
INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id) VALUES
('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000001'),
('r0000000-0000-0000-0000-000000000005', 'p0000000-0000-0000-0000-000000000003')
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
INSERT INTO org_roles (id, version, role_name, created_at, updated_at) VALUES
('r0000000-0000-0000-0000-000000000005', 0, 'CHEF', NOW(), NOW())
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

-- Employees:
-- Chef at Phở Việt Q1
INSERT INTO employees (id, version, user_id, org_role_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
('f0000000-0000-0000-0000-000000000008', 0, 'c0000000-0000-0000-0000-000000000008', 'r0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'chef_q1@restaurant.com', '0905000001', '2024-01-15', NULL, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Waiter at Phở Việt Q1
INSERT INTO employees (id, version, user_id, org_role_id, branch_id, status, email, phone, start_date, end_date, created_at, updated_at) VALUES
('f0000000-0000-0000-0000-000000000009', 0, 'c0000000-0000-0000-0000-000000000009', 'r0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000001', 'ACTIVE', 'waiter_q1@restaurant.com', '0905000002', '2024-01-15', NULL, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Table Area: Khu A at branch Phở Việt Q1 (e0000000-0000-0000-0000-000000000001)
INSERT INTO table_areas (area_id, version, branch_id, area_name, description, created_at, updated_at) VALUES
('a0000000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', 'Khu A', 'Khu vực trong nhà', NOW(), NOW())
ON CONFLICT (area_id) DO NOTHING;

-- Restaurant Table: Bàn 01 inside Khu A
INSERT INTO restaurant_tables (table_id, version, area_id, table_number, capacity, status, created_at, updated_at) VALUES
('t0000000-0000-0000-0000-000000000001', 0, 'a0000000-0000-0000-0000-000000000001', 'Bàn 01', 4, 'AVAILABLE', NOW(), NOW())
ON CONFLICT (table_id) DO NOTHING;

-- Product Category: Món nước
INSERT INTO product_categories (category_id, branch_id, category_name, description, display_order, created_at, updated_at) VALUES
('ac000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'Món nước', 'Các món bún, phở', 1, NOW(), NOW())
ON CONFLICT (category_id) DO NOTHING;

-- Product: Phở Bò
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, created_at, updated_at) VALUES
('p0000000-0000-0000-0000-000000000101', 0, 'e0000000-0000-0000-0000-000000000001', 'ac000000-0000-0000-0000-000000000001', 'Phở Bò chín', 'Phở bò tái nạm chín', 55000.00, NULL, 'AVAILABLE', NOW(), NOW())
ON CONFLICT (product_id) DO NOTHING;

-- Order: Order for Table 01 created by Waiter f0000000-0000-0000-0000-000000000009
INSERT INTO orders (id, version, branch_id, table_id, reservation_id, order_code, order_type, status, note, subtotal, discount_amount, total_amount, created_by, created_at, updated_at) VALUES
('o0000000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', 't0000000-0000-0000-0000-000000000001', NULL, 'ORD-TEST001', 'DINE_IN', 'PENDING', 'Nước dùng trong', 55000.00, 0.00, 55000.00, 'f0000000-0000-0000-0000-000000000009', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Order Item: 1 Phở Bò chín
INSERT INTO order_items (id, version, order_id, product_id, combo_id, quantity, unit_price, subtotal, note, status, created_at, updated_at) VALUES
('oi000000-0000-0000-0000-000000000101', 0, 'o0000000-0000-0000-0000-000000000001', 'p0000000-0000-0000-0000-000000000101', NULL, 1, 55000.00, 55000.00, NULL, 'PENDING', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- TEST SCENARIOS SUMMARY
-- =============================================================================
--
-- Login: POST /api/v1/auth/login  |  Password: "password123"
--
-- ┌──────────────────────────────────────────────────────────────────────────────┐
-- │ Case 1: ADMIN                                                               │
-- │   email: admin@system.local                                                 │
-- │   → contexts: [] (no employee records)                                      │
-- │   → systemRoles: ["ADMIN"]                                                  │
-- ├──────────────────────────────────────────────────────────────────────────────┤
-- │ Case 2: OWNER A (1 org)                                                     │
-- │   email: owner_a@restaurant.com                                             │
-- │   → contexts: [] (owner is not an employee)                                 │
-- │   → systemRoles: ["USER"]                                                   │
-- │   → POST /context: { organizationId:"d0...01", role:"OWNER" }              │
-- ├──────────────────────────────────────────────────────────────────────────────┤
-- │ Case 3: OWNER B (2 orgs)                                                    │
-- │   email: owner_b@restaurant.com                                             │
-- │   → contexts: [] (owner is not an employee)                                 │
-- │   → systemRoles: ["USER"]                                                   │
-- │   → POST /context: { organizationId:"d0...02", role:"OWNER" }              │
-- │   → POST /context: { organizationId:"d0...03", role:"OWNER" }              │
-- ├──────────────────────────────────────────────────────────────────────────────┤
-- │ Case 4: MANAGER (1 branch)                                                  │
-- │   email: manager@restaurant.com                                             │
-- │   → POST /context: { employeeId:"f0...01" }                                │
-- │   → Context Token: orgRole=MANAGER, permissions=[ORDER_*,PAYMENT_*,MENU...] │
-- ├──────────────────────────────────────────────────────────────────────────────┤
-- │ Case 5: MANAGER (2+ branches)                                               │
-- │   email: manager@restaurant.com (same user, 3 employee records)             │
-- │   → contexts:                                                               │
-- │     { employeeId:"f0...01", org:"Phở Việt",    branch:"Q1",     MANAGER }  │
-- │     { employeeId:"f0...02", org:"Sushi Tokyo", branch:"Ng.Huệ", CASHIER }  │
-- │     { employeeId:"f0...03", org:"BBQ Garden",  branch:"Q3",     MANAGER }  │
-- └──────────────────────────────────────────────────────────────────────────────┘
