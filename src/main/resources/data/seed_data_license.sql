-- =============================================================================
-- SEED DATA: LICENSE & SUBSCRIPTION MODULE
-- Run after seed_data.sql (depends on organizations)
-- =============================================================================

-- =============================================================================
-- 1. LICENSES (10 plans)
-- =============================================================================

INSERT INTO license (id, version, code, name, description, price, billing_cycle, max_branch, max_employee, status, created_at, updated_at) VALUES
    -- Free tier
    ('lic00000-0000-0000-0000-000000000001', 0, 'FREE-M',       'Free (Monthly)',              'Gói miễn phí cho quán nhỏ, 1 chi nhánh, tối đa 5 nhân viên',                   0.00,       'MONTHLY', 1,  5,   'ACTIVE', NOW(), NOW()),
    -- Basic tier
    ('lic00000-0000-0000-0000-000000000002', 0, 'BASIC-M',      'Basic (Monthly)',             'Gói cơ bản cho nhà hàng mới bắt đầu, 2 chi nhánh, 15 nhân viên',              299000.00,  'MONTHLY', 2,  15,  'ACTIVE', NOW(), NOW()),
    ('lic00000-0000-0000-0000-000000000003', 0, 'BASIC-Y',      'Basic (Yearly)',              'Gói cơ bản thanh toán năm, tiết kiệm 20%',                                     2870000.00, 'YEARLY',  2,  15,  'ACTIVE', NOW(), NOW()),
    -- Standard tier
    ('lic00000-0000-0000-0000-000000000004', 0, 'STANDARD-M',   'Standard (Monthly)',          'Gói tiêu chuẩn cho chuỗi nhà hàng nhỏ, 5 chi nhánh, 50 nhân viên',            699000.00,  'MONTHLY', 5,  50,  'ACTIVE', NOW(), NOW()),
    ('lic00000-0000-0000-0000-000000000005', 0, 'STANDARD-Y',   'Standard (Yearly)',           'Gói tiêu chuẩn thanh toán năm, tiết kiệm 20%',                                 6710000.00, 'YEARLY',  5,  50,  'ACTIVE', NOW(), NOW()),
    -- Premium tier
    ('lic00000-0000-0000-0000-000000000006', 0, 'PREMIUM-M',    'Premium (Monthly)',           'Gói cao cấp cho chuỗi lớn, 20 chi nhánh, 200 nhân viên, hỗ trợ ưu tiên',      1499000.00, 'MONTHLY', 20, 200, 'ACTIVE', NOW(), NOW()),
    ('lic00000-0000-0000-0000-000000000007', 0, 'PREMIUM-Y',    'Premium (Yearly)',            'Gói cao cấp thanh toán năm, tiết kiệm 25%',                                    13490000.00,'YEARLY',  20, 200, 'ACTIVE', NOW(), NOW()),
    -- Enterprise tier
    ('lic00000-0000-0000-0000-000000000008', 0, 'ENTERPRISE-M', 'Enterprise (Monthly)',        'Gói doanh nghiệp, không giới hạn chi nhánh & nhân viên, hỗ trợ 24/7',          3999000.00, 'MONTHLY', -1, -1,  'ACTIVE', NOW(), NOW()),
    ('lic00000-0000-0000-0000-000000000009', 0, 'ENTERPRISE-Y', 'Enterprise (Yearly)',         'Gói doanh nghiệp thanh toán năm, tiết kiệm 30%',                               33590000.00,'YEARLY',  -1, -1,  'ACTIVE', NOW(), NOW()),
    -- Locked plan (legacy, no longer sold)
    ('lic00000-0000-0000-0000-000000000010', 0, 'LEGACY-M',     'Legacy Basic (Monthly)',      'Gói cũ đã ngừng bán',                                                          199000.00,  'MONTHLY', 1,  10,  'LOCKED', NOW(), NOW())
    ON CONFLICT (code) DO NOTHING;

-- =============================================================================
-- 2. SUBSCRIPTIONS
-- =============================================================================

INSERT INTO license_subscription (id, version, license_id, organization_id, start_date, end_date, status, price, billing_cycle, max_branch, max_employee, created_at, updated_at) VALUES
    -- Phở Việt Chain → Standard Monthly (active, started 3 months ago)
    ('sub00000-0000-0000-0000-000000000001', 0, 'lic00000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', '2026-05-01', '2026-06-01', 'EXPIRED', 699000.00,  'MONTHLY', 5,  50,  NOW(), NOW()),
    ('sub00000-0000-0000-0000-000000000002', 0, 'lic00000-0000-0000-0000-000000000004', 'd0000000-0000-0000-0000-000000000001', '2026-06-01', '2026-07-01', 'EXPIRED', 699000.00,  'MONTHLY', 5,  50,  NOW(), NOW()),
    ('sub00000-0000-0000-0000-000000000003', 0, 'lic00000-0000-0000-0000-000000000005', 'd0000000-0000-0000-0000-000000000001', '2026-07-01', '2027-07-01', 'ACTIVE',  6710000.00, 'YEARLY',  5,  50,  NOW(), NOW()),

    -- Sushi Tokyo Group → Premium Yearly (active)
    ('sub00000-0000-0000-0000-000000000004', 0, 'lic00000-0000-0000-0000-000000000007', 'd0000000-0000-0000-0000-000000000002', '2026-03-15', '2027-03-15', 'ACTIVE',  13490000.00,'YEARLY',  20, 200, NOW(), NOW()),

    -- BBQ Garden → Basic Monthly (active)
    ('sub00000-0000-0000-0000-000000000005', 0, 'lic00000-0000-0000-0000-000000000002', 'd0000000-0000-0000-0000-000000000003', '2026-07-01', '2026-08-01', 'ACTIVE',  299000.00,  'MONTHLY', 2,  15,  NOW(), NOW()),

    -- Phở Việt old Free trial (expired)
    ('sub00000-0000-0000-0000-000000000006', 0, 'lic00000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000001', '2026-04-01', '2026-05-01', 'EXPIRED', 0.00,       'MONTHLY', 1,  5,   NOW(), NOW()),

    -- Sushi Tokyo old Basic (revoked - upgraded to Premium)
    ('sub00000-0000-0000-0000-000000000007', 0, 'lic00000-0000-0000-0000-000000000003', 'd0000000-0000-0000-0000-000000000002', '2025-06-01', '2026-06-01', 'REVOKED', 2870000.00, 'YEARLY',  2,  15,  NOW(), NOW()),

    -- BBQ Garden old Free trial (expired)
    ('sub00000-0000-0000-0000-000000000008', 0, 'lic00000-0000-0000-0000-000000000001', 'd0000000-0000-0000-0000-000000000003', '2026-05-15', '2026-06-15', 'EXPIRED', 0.00,       'MONTHLY', 1,  5,   NOW(), NOW())
    ON CONFLICT DO NOTHING;
