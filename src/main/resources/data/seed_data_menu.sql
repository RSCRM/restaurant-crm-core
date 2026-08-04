-- =============================================================================
-- SEED DATA: MENU MODULE (Categories, Products, ModifierGroups, ModifierOptions)
-- Run after seed_data.sql (depends on organization_branches)
-- =============================================================================

-- =============================================================================
-- 1. CATEGORIES
-- =============================================================================

-- Phở Việt - Chi nhánh Q1 (e001)
INSERT INTO categories (category_id, version, branch_id, category_name, description, display_order, created_at, updated_at) VALUES
    ('cat00000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', 'Phở & Bún',     'Các món Phở & Bún truyền thống Việt Nam', 1, NOW(), NOW()),
    ('cat00000-0000-0000-0000-000000000002', 0, 'e0000000-0000-0000-0000-000000000001', 'Cơm',           'Các món cơm Việt Nam',                     2, NOW(), NOW()),
    ('cat00000-0000-0000-0000-000000000003', 0, 'e0000000-0000-0000-0000-000000000001', 'Đồ Uống',       'Nước giải khát & Trà',                     3, NOW(), NOW()),
    ('cat00000-0000-0000-0000-000000000004', 0, 'e0000000-0000-0000-0000-000000000001', 'Món Phụ',       'Đồ ăn kèm & Khai vị',                     4, NOW(), NOW())
    ON CONFLICT (category_id) DO NOTHING;

-- Sushi Tokyo - Nguyễn Huệ (e003)
INSERT INTO categories (category_id, version, branch_id, category_name, description, display_order, created_at, updated_at) VALUES
    ('cat00000-0000-0000-0000-000000000005', 0, 'e0000000-0000-0000-0000-000000000003', 'Sashimi',       'Cá hồi, cá ngừ tươi sống',                 1, NOW(), NOW()),
    ('cat00000-0000-0000-0000-000000000006', 0, 'e0000000-0000-0000-0000-000000000003', 'Sushi',         'Sushi cuộn & Nigiri',                      2, NOW(), NOW()),
    ('cat00000-0000-0000-0000-000000000007', 0, 'e0000000-0000-0000-0000-000000000003', 'Ramen & Mì',    'Mì Nhật Bản các loại',                     3, NOW(), NOW()),
    ('cat00000-0000-0000-0000-000000000008', 0, 'e0000000-0000-0000-0000-000000000003', 'Đồ Uống',       'Rượu Sake, Trà & Nước giải khát',         4, NOW(), NOW())
    ON CONFLICT (category_id) DO NOTHING;

-- =============================================================================
-- 2. PRODUCTS
-- =============================================================================

-- Phở Việt - Chi nhánh Q1: Phở & Bún
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Phở Bò Tái',            'Phở bò tái truyền thống với nước dùng đậm đà',             55000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000002', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Phở Bò Chín',           'Phở bò chín mềm nước dùng xương hầm',                     55000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000003', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Phở Gà Đặc Biệt',       'Phở gà ta thịt đùi xé phay kèm trứng non',                65000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000004', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Bún Bò Huế',            'Bún bò Huế cay nồng đặc trưng miền Trung',                60000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000005', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000001', 'Phở Đặc Biệt Đặc Ruột', 'Phở đặc biệt đầy đủ: tái, chín, gầu, gân, sách',         75000.00, NULL, 'AVAILABLE', true,  NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Phở Việt - Chi nhánh Q1: Cơm
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000006', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000002', 'Cơm Tấm Sườn Nướng',    'Cơm tấm sườn nướng than hoa, chả, bì, trứng',            45000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000007', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000002', 'Cơm Gà Chiên Giòn',     'Cơm gà chiên giòn sốt mắm tỏi',                           50000.00, NULL, 'AVAILABLE', true,  NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Phở Việt - Chi nhánh Q1: Đồ Uống
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000008', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000003', 'Trà Đá',                 'Trà đá ướp hoa lài ướp lạnh',                              5000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000009', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000003', 'Nước Cam Ép',            'Cam sành ép tươi 100% nguyên chất',                       25000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000010', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000003', 'Cà Phê Sữa Đá',          'Cà phê sữa đá pha phin truyền thống',                     20000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000011', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000003', 'Bia Tiger',              'Bia Tiger lon 330ml',                                      25000.00, NULL, 'AVAILABLE', false, NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Phở Việt - Chi nhánh Q1: Món Phụ
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000012', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000004', 'Giá Chần',               'Giá đỗ chần tái',                                          5000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000013', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000004', 'Quẩy',                   'Quẩy giòn ăn kèm phở',                                     8000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000014', 0, 'e0000000-0000-0000-0000-000000000001', 'cat00000-0000-0000-0000-000000000004', 'Trứng Non',               'Trứng non luộc',                                           15000.00, NULL, 'AVAILABLE', true,  NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Sushi Tokyo - Nguyễn Huệ: Sashimi
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000015', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000005', 'Sashimi Cá Hồi',         '5 lát cá hồi Na Uy tươi sống',                            120000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000016', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000005', 'Sashimi Cá Ngừ',         '5 lát cá ngừ đỏ đại dương',                               150000.00, NULL, 'AVAILABLE', false, NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Sushi Tokyo - Nguyễn Huệ: Sushi
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000017', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000006', 'California Roll',         'Cua, bơ, dưa chuột, trứng cá',                             95000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000018', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000006', 'Salmon Nigiri',           'Cá hồi nướng nhẹ trên cơm sushi',                         85000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000019', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000006', 'Dragon Roll',             'Tôm tempura, bơ, lươn nướng sốt',                        110000.00, NULL, 'AVAILABLE', true,  NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Sushi Tokyo - Nguyễn Huệ: Ramen & Mì
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000020', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000007', 'Tonkotsu Ramen',          'Mì ramen nước dùng xương heo hầm 12 giờ',                 85000.00, NULL, 'AVAILABLE', true,  NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000021', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000007', 'Mì Udon Tempura',         'Mì udon nóng với tôm tempura giòn',                       75000.00, NULL, 'AVAILABLE', true,  NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- Sushi Tokyo - Nguyễn Huệ: Đồ Uống
INSERT INTO products (product_id, version, branch_id, category_id, product_name, description, price, image_url, status, requires_preparation, created_at, updated_at) VALUES
    ('prd00000-0000-0000-0000-000000000022', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000008', 'Sake Nóng',               'Rượu sake Nhật Bản ấm 180ml',                             80000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000023', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000008', 'Trà Xanh Matcha',         'Trà xanh matcha Nhật Bản',                                35000.00, NULL, 'AVAILABLE', false, NOW(), NOW()),
    ('prd00000-0000-0000-0000-000000000024', 0, 'e0000000-0000-0000-0000-000000000003', 'cat00000-0000-0000-0000-000000000008', 'Asahi Beer',              'Bia Asahi lon 330ml',                                      35000.00, NULL, 'AVAILABLE', false, NOW(), NOW())
    ON CONFLICT (product_id) DO NOTHING;

-- =============================================================================
-- 3. MODIFIER GROUPS
-- =============================================================================

-- Phở Bò Tái: Chọn size & Topping thêm
INSERT INTO modifier_groups (modifier_group_id, version, product_id, group_name, description, min_selection, max_selection, created_at, updated_at) VALUES
    ('mg000000-0000-0000-0000-000000000001', 0, 'prd00000-0000-0000-0000-000000000001', 'Chọn Size',     'Chọn size phần ăn',       1, 1, NOW(), NOW()),
    ('mg000000-0000-0000-0000-000000000002', 0, 'prd00000-0000-0000-0000-000000000001', 'Topping Thêm',  'Thêm topping cho bát phở', 0, 3, NOW(), NOW())
    ON CONFLICT (modifier_group_id) DO NOTHING;

-- Phở Gà Đặc Biệt: Chọn size
INSERT INTO modifier_groups (modifier_group_id, version, product_id, group_name, description, min_selection, max_selection, created_at, updated_at) VALUES
    ('mg000000-0000-0000-0000-000000000003', 0, 'prd00000-0000-0000-0000-000000000003', 'Chọn Size',     'Chọn size phần ăn',       1, 1, NOW(), NOW())
    ON CONFLICT (modifier_group_id) DO NOTHING;

-- Bún Bò Huế: Chọn cay
INSERT INTO modifier_groups (modifier_group_id, version, product_id, group_name, description, min_selection, max_selection, created_at, updated_at) VALUES
    ('mg000000-0000-0000-0000-000000000004', 0, 'prd00000-0000-0000-0000-000000000004', 'Mức Độ Cay',    'Chọn mức độ cay',         1, 1, NOW(), NOW())
    ON CONFLICT (modifier_group_id) DO NOTHING;

-- Cơm Tấm Sườn Nướng: Chọn topping
INSERT INTO modifier_groups (modifier_group_id, version, product_id, group_name, description, min_selection, max_selection, created_at, updated_at) VALUES
    ('mg000000-0000-0000-0000-000000000005', 0, 'prd00000-0000-0000-0000-000000000006', 'Topping Thêm',  'Thêm topping cho dĩa cơm', 0, 3, NOW(), NOW())
    ON CONFLICT (modifier_group_id) DO NOTHING;

-- Tonkotsu Ramen: Chọn mức cay & Topping
INSERT INTO modifier_groups (modifier_group_id, version, product_id, group_name, description, min_selection, max_selection, created_at, updated_at) VALUES
    ('mg000000-0000-0000-0000-000000000006', 0, 'prd00000-0000-0000-0000-000000000020', 'Mức Độ Cay',    'Chọn mức độ cay',         1, 1, NOW(), NOW()),
    ('mg000000-0000-0000-0000-000000000007', 0, 'prd00000-0000-0000-0000-000000000020', 'Topping Thêm',  'Thêm topping cho ramen',   0, 3, NOW(), NOW())
    ON CONFLICT (modifier_group_id) DO NOTHING;

-- California Roll: Chọn topping
INSERT INTO modifier_groups (modifier_group_id, version, product_id, group_name, description, min_selection, max_selection, created_at, updated_at) VALUES
    ('mg000000-0000-0000-0000-000000000008', 0, 'prd00000-0000-0000-0000-000000000017', 'Topping Thêm',  'Thêm topping cho roll',    0, 2, NOW(), NOW())
    ON CONFLICT (modifier_group_id) DO NOTHING;

-- =============================================================================
-- 4. MODIFIER OPTIONS
-- =============================================================================

-- Phở Bò Tái: Chọn Size
INSERT INTO modifier_options (modifier_option_id, version, modifier_group_id, option_name, additional_price, status, created_at, updated_at) VALUES
    ('mo000000-0000-0000-0000-000000000001', 0, 'mg000000-0000-0000-0000-000000000001', 'Nhỏ',          0.00,      'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000002', 0, 'mg000000-0000-0000-0000-000000000001', 'Vừa',          10000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000003', 0, 'mg000000-0000-0000-0000-000000000001', 'Đặc Biệt',     25000.00,  'AVAILABLE', NOW(), NOW())
    ON CONFLICT (modifier_option_id) DO NOTHING;

-- Phở Bò Tái: Topping Thêm
INSERT INTO modifier_options (modifier_option_id, version, modifier_group_id, option_name, additional_price, status, created_at, updated_at) VALUES
    ('mo000000-0000-0000-0000-000000000004', 0, 'mg000000-0000-0000-0000-000000000002', 'Thêm Tái',     20000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000005', 0, 'mg000000-0000-0000-0000-000000000002', 'Thêm Chín',    20000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000006', 0, 'mg000000-0000-0000-0000-000000000002', 'Thêm Gầu',     25000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000007', 0, 'mg000000-0000-0000-0000-000000000002', 'Thêm Gân',     25000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000008', 0, 'mg000000-0000-0000-0000-000000000002', 'Trứng Non',    15000.00,  'AVAILABLE', NOW(), NOW())
    ON CONFLICT (modifier_option_id) DO NOTHING;

-- Phở Gà Đặc Biệt: Chọn Size
INSERT INTO modifier_options (modifier_option_id, version, modifier_group_id, option_name, additional_price, status, created_at, updated_at) VALUES
    ('mo000000-0000-0000-0000-000000000009', 0, 'mg000000-0000-0000-0000-000000000003', 'Nhỏ',          0.00,      'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000010', 0, 'mg000000-0000-0000-0000-000000000003', 'Vừa',          10000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000011', 0, 'mg000000-0000-0000-0000-000000000003', 'Đặc Biệt',     25000.00,  'AVAILABLE', NOW(), NOW())
    ON CONFLICT (modifier_option_id) DO NOTHING;

-- Bún Bò Huế: Mức Độ Cay
INSERT INTO modifier_options (modifier_option_id, version, modifier_group_id, option_name, additional_price, status, created_at, updated_at) VALUES
    ('mo000000-0000-0000-0000-000000000012', 0, 'mg000000-0000-0000-0000-000000000004', 'Không Cay',    0.00,      'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000013', 0, 'mg000000-0000-0000-0000-000000000004', 'Cay Ít',       0.00,      'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000014', 0, 'mg000000-0000-0000-0000-000000000004', 'Cay Vừa',      0.00,      'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000015', 0, 'mg000000-0000-0000-0000-000000000004', 'Cay Nhiều',    0.00,      'AVAILABLE', NOW(), NOW())
    ON CONFLICT (modifier_option_id) DO NOTHING;

-- Cơm Tấm Sườn Nướng: Topping Thêm
INSERT INTO modifier_options (modifier_option_id, version, modifier_group_id, option_name, additional_price, status, created_at, updated_at) VALUES
    ('mo000000-0000-0000-0000-000000000016', 0, 'mg000000-0000-0000-0000-000000000005', 'Thêm Sườn',    25000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000017', 0, 'mg000000-0000-0000-0000-000000000005', 'Thêm Chả',     15000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000018', 0, 'mg000000-0000-0000-0000-000000000005', 'Thêm Bì',      10000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000019', 0, 'mg000000-0000-0000-0000-000000000005', 'Trứng Ốp La',  10000.00,  'AVAILABLE', NOW(), NOW())
    ON CONFLICT (modifier_option_id) DO NOTHING;

-- Tonkotsu Ramen: Mức Độ Cay
INSERT INTO modifier_options (modifier_option_id, version, modifier_group_id, option_name, additional_price, status, created_at, updated_at) VALUES
    ('mo000000-0000-0000-0000-000000000020', 0, 'mg000000-0000-0000-0000-000000000006', 'Không Cay',    0.00,      'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000021', 0, 'mg000000-0000-0000-0000-000000000006', 'Cay Vừa',      0.00,      'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000022', 0, 'mg000000-0000-0000-0000-000000000006', 'Cay Nhiều',    0.00,      'AVAILABLE', NOW(), NOW())
    ON CONFLICT (modifier_option_id) DO NOTHING;

-- Tonkotsu Ramen: Topping Thêm
INSERT INTO modifier_options (modifier_option_id, version, modifier_group_id, option_name, additional_price, status, created_at, updated_at) VALUES
    ('mo000000-0000-0000-0000-000000000023', 0, 'mg000000-0000-0000-0000-000000000007', 'Thêm Chashu',  30000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000024', 0, 'mg000000-0000-0000-0000-000000000007', 'Thêm Trứng',   10000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000025', 0, 'mg000000-0000-0000-0000-000000000007', 'Thêm Măng',    15000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000026', 0, 'mg000000-0000-0000-0000-000000000007', 'Thêm Ngô',     10000.00,  'AVAILABLE', NOW(), NOW())
    ON CONFLICT (modifier_option_id) DO NOTHING;

-- California Roll: Topping Thêm
INSERT INTO modifier_options (modifier_option_id, version, modifier_group_id, option_name, additional_price, status, created_at, updated_at) VALUES
    ('mo000000-0000-0000-0000-000000000027', 0, 'mg000000-0000-0000-0000-000000000008', 'Thêm Cá Hồi',  40000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000028', 0, 'mg000000-0000-0000-0000-000000000008', 'Thêm Bơ',      15000.00,  'AVAILABLE', NOW(), NOW()),
    ('mo000000-0000-0000-0000-000000000029', 0, 'mg000000-0000-0000-0000-000000000008', 'Thêm Phô Mai', 20000.00,  'AVAILABLE', NOW(), NOW())
    ON CONFLICT (modifier_option_id) DO NOTHING;
