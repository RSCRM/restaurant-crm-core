-- =============================================================================
-- SEED DATA: ORDER MODULE (Orders, OrderItems, OrderItemModifiers)
-- Run after seed_data.sql & seed_data_menu.sql
-- =============================================================================

-- =============================================================================
-- 1. ORDERS
-- =============================================================================

-- Order 1: Bàn 02 (t002) - Phở Việt Q1 - Đơn đang chờ xử lý (PENDING)
INSERT INTO orders (id, version, branch_id, table_id, reservation_id, order_code, order_type, status, customer_name, customer_phone, note, subtotal, discount_amount, total_amount, created_by, created_at, updated_at) VALUES
    ('ord00000-0000-0000-0000-000000000001', 0, 'e0000000-0000-0000-0000-000000000001', 't0000000-0000-0000-0000-000000000002', NULL, 'ORD-20260801-001', 'DINE_IN', 'PENDING', 'Nguyễn Văn A', '0901234567', 'Phở tái nhiều hành, bún bò cay vừa', 245000.00, 0.00, 245000.00, 'f0000000-0000-0000-0000-000000000009', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Order 2: Bàn 05 (t005) - Sushi Tokyo - Đang chờ xử lý (PENDING)
INSERT INTO orders (id, version, branch_id, table_id, reservation_id, order_code, order_type, status, customer_name, customer_phone, note, subtotal, discount_amount, total_amount, created_by, created_at, updated_at) VALUES
    ('ord00000-0000-0000-0000-000000000002', 0, 'e0000000-0000-0000-0000-000000000001', 't0000000-0000-0000-0000-000000000005', NULL, 'ORD-20260801-002', 'DINE_IN', 'PENDING', 'Trần Thị B', '0912345678', 'Sashimi cá hồi kèm sake nóng', 350000.00, 0.00, 350000.00, 'f0000000-0000-0000-0000-000000000001', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Order 3: Bàn 01 (t001) - Phở Việt Q1 - Đã thanh toán (PAID)
INSERT INTO orders (id, version, branch_id, table_id, reservation_id, order_code, order_type, status, customer_name, customer_phone, note, subtotal, discount_amount, total_amount, created_by, created_at, updated_at) VALUES
    ('ord00000-0000-0000-0000-000000000003', 0, 'e0000000-0000-0000-0000-000000000001', 't0000000-0000-0000-0000-000000000001', NULL, 'ORD-20260801-003', 'DINE_IN', 'PAID', 'Lê Văn C', '0923456789', '', 195000.00, 19500.00, 175500.00, 'f0000000-0000-0000-0000-000000000009', NOW() - INTERVAL '2 hours', NOW())
    ON CONFLICT (id) DO NOTHING;

-- Order 4: Bàn 03 (t003) - Phở Việt Q1 - Đơn mang về (TAKEAWAY), đang chờ
INSERT INTO orders (id, version, branch_id, table_id, reservation_id, order_code, order_type, status, customer_name, customer_phone, note, subtotal, discount_amount, total_amount, created_by, created_at, updated_at) VALUES
    ('ord00000-0000-0000-0000-000000000004', 0, 'e0000000-0000-0000-0000-000000000001', 't0000000-0000-0000-0000-000000000103', NULL, 'ORD-20260801-004', 'TAKEAWAY', 'PENDING', 'Phạm Thị D', '0934567890', 'Gói riêng nước dùng', 120000.00, 0.00, 120000.00, 'f0000000-0000-0000-0000-000000000009', NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 2. ORDER ITEMS
-- =============================================================================

-- Order 1 items: Phở Bò Tái (size Vừa + thêm tái) + Bún Bò Huế (cay vừa) + Trà Đá x2
INSERT INTO order_items (id, version, order_id, product_id, combo_id, quantity, unit_price, subtotal, status, note, prepared_by, cancel_reason, cancelled_by, created_at, updated_at) VALUES
    ('oi000000-0000-0000-0000-000000000001', 0, 'ord00000-0000-0000-0000-000000000001', 'prd00000-0000-0000-0000-000000000001', NULL, 1, 65000.00, 65000.00, 'PENDING', 'Phở tái nhiều hành', NULL, NULL, NULL, NOW(), NOW()),
    ('oi000000-0000-0000-0000-000000000002', 0, 'ord00000-0000-0000-0000-000000000001', 'prd00000-0000-0000-0000-000000000004', NULL, 1, 60000.00, 60000.00, 'PENDING', 'Bún bò cay vừa', NULL, NULL, NULL, NOW(), NOW()),
    ('oi000000-0000-0000-0000-000000000003', 0, 'ord00000-0000-0000-0000-000000000001', 'prd00000-0000-0000-0000-000000000008', NULL, 2, 5000.00,  10000.00, 'SERVED',  '', NULL, NULL, NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Order 1 extra: Quẩy x1
INSERT INTO order_items (id, version, order_id, product_id, combo_id, quantity, unit_price, subtotal, status, note, prepared_by, cancel_reason, cancelled_by, created_at, updated_at) VALUES
    ('oi000000-0000-0000-0000-000000000004', 0, 'ord00000-0000-0000-0000-000000000001', 'prd00000-0000-0000-0000-000000000013', NULL, 1, 8000.00, 8000.00, 'SERVED', '', NULL, NULL, NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Order 1: Trứng Non x1
INSERT INTO order_items (id, version, order_id, product_id, combo_id, quantity, unit_price, subtotal, status, note, prepared_by, cancel_reason, cancelled_by, created_at, updated_at) VALUES
    ('oi000000-0000-0000-0000-000000000005', 0, 'ord00000-0000-0000-0000-000000000001', 'prd00000-0000-0000-0000-000000000014', NULL, 1, 15000.00, 15000.00, 'PENDING', '', NULL, NULL, NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Order 2 items: Sashimi Cá Hồi x1 + California Roll x1 + Sake Nóng x1
INSERT INTO order_items (id, version, order_id, product_id, combo_id, quantity, unit_price, subtotal, status, note, prepared_by, cancel_reason, cancelled_by, created_at, updated_at) VALUES
    ('oi000000-0000-0000-0000-000000000006', 0, 'ord00000-0000-0000-0000-000000000002', 'prd00000-0000-0000-0000-000000000015', NULL, 1, 120000.00, 120000.00, 'IN_PROGRESS', '', NULL, NULL, NULL, NOW(), NOW()),
    ('oi000000-0000-0000-0000-000000000007', 0, 'ord00000-0000-0000-0000-000000000002', 'prd00000-0000-0000-0000-000000000017', NULL, 1, 95000.00,  95000.00, 'PENDING', 'Thêm cá hồi', NULL, NULL, NULL, NOW(), NOW()),
    ('oi000000-0000-0000-0000-000000000008', 0, 'ord00000-0000-0000-0000-000000000002', 'prd00000-0000-0000-0000-000000000022', NULL, 1, 80000.00,  80000.00, 'SERVED',  '', NULL, NULL, NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Order 2 extra: Tonkotsu Ramen x1 (cay vừa + thêm chashu)
INSERT INTO order_items (id, version, order_id, product_id, combo_id, quantity, unit_price, subtotal, status, note, prepared_by, cancel_reason, cancelled_by, created_at, updated_at) VALUES
    ('oi000000-0000-0000-0000-000000000009', 0, 'ord00000-0000-0000-0000-000000000002', 'prd00000-0000-0000-0000-000000000020', NULL, 1, 85000.00, 85000.00, 'IN_PROGRESS', 'Cay vừa, thêm chashu', NULL, NULL, NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- Order 3 items (completed): Phở Đặc Biệt x1 + Cà Phê Sữa Đá x1
INSERT INTO order_items (id, version, order_id, product_id, combo_id, quantity, unit_price, subtotal, status, note, prepared_by, cancel_reason, cancelled_by, created_at, updated_at) VALUES
    ('oi000000-0000-0000-0000-000000000010', 0, 'ord00000-0000-0000-0000-000000000003', 'prd00000-0000-0000-0000-000000000005', NULL, 1, 75000.00, 75000.00, 'SERVED', '', 'f0000000-0000-0000-0000-000000000008', NULL, NULL, NOW() - INTERVAL '2 hours', NOW()),
    ('oi000000-0000-0000-0000-000000000011', 0, 'ord00000-0000-0000-0000-000000000003', 'prd00000-0000-0000-0000-000000000010', NULL, 2, 20000.00, 40000.00, 'SERVED', '', NULL, NULL, NULL, NOW() - INTERVAL '2 hours', NOW()),
    ('oi000000-0000-0000-0000-000000000012', 0, 'ord00000-0000-0000-0000-000000000003', 'prd00000-0000-0000-0000-000000000006', NULL, 1, 45000.00, 45000.00, 'SERVED', 'Thêm sườn', 'f0000000-0000-0000-0000-000000000008', NULL, NULL, NOW() - INTERVAL '2 hours', NOW())
    ON CONFLICT (id) DO NOTHING;

-- Order 4 items (take away): Phở Bò Chín x2 + Bia Tiger x2
INSERT INTO order_items (id, version, order_id, product_id, combo_id, quantity, unit_price, subtotal, status, note, prepared_by, cancel_reason, cancelled_by, created_at, updated_at) VALUES
    ('oi000000-0000-0000-0000-000000000013', 0, 'ord00000-0000-0000-0000-000000000004', 'prd00000-0000-0000-0000-000000000002', NULL, 2, 55000.00, 110000.00, 'PENDING', 'Gói riêng nước dùng', NULL, NULL, NULL, NOW(), NOW()),
    ('oi000000-0000-0000-0000-000000000014', 0, 'ord00000-0000-0000-0000-000000000004', 'prd00000-0000-0000-0000-000000000011', NULL, 2, 25000.00, 50000.00,  'PENDING', '', NULL, NULL, NULL, NOW(), NOW())
    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 3. ORDER ITEM MODIFIERS
-- =============================================================================

-- Order 1, Item 1 (Phở Bò Tái): Size Vừa + Thêm Tái
INSERT INTO order_item_modifiers (id, version, order_item_id, modifier_option_id, additional_price, quantity, created_at, updated_at) VALUES
    ('om000000-0000-0000-0000-000000000001', 0, 'oi000000-0000-0000-0000-000000000001', 'mo000000-0000-0000-0000-000000000002', 10000.00, 1, NOW(), NOW()),  -- Size Vừa +10k
    ('om000000-0000-0000-0000-000000000002', 0, 'oi000000-0000-0000-0000-000000000001', 'mo000000-0000-0000-0000-000000000004', 20000.00, 1, NOW(), NOW())   -- Thêm Tái +20k
    ON CONFLICT (id) DO NOTHING;

-- Order 1, Item 2 (Bún Bò Huế): Cay Vừa
INSERT INTO order_item_modifiers (id, version, order_item_id, modifier_option_id, additional_price, quantity, created_at, updated_at) VALUES
    ('om000000-0000-0000-0000-000000000003', 0, 'oi000000-0000-0000-0000-000000000002', 'mo000000-0000-0000-0000-000000000014', 0.00, 1, NOW(), NOW())  -- Cay Vừa
    ON CONFLICT (id) DO NOTHING;

-- Order 2, Item 7 (California Roll): Thêm Cá Hồi
INSERT INTO order_item_modifiers (id, version, order_item_id, modifier_option_id, additional_price, quantity, created_at, updated_at) VALUES
    ('om000000-0000-0000-0000-000000000004', 0, 'oi000000-0000-0000-0000-000000000007', 'mo000000-0000-0000-0000-000000000027', 40000.00, 1, NOW(), NOW())  -- Thêm Cá Hồi +40k
    ON CONFLICT (id) DO NOTHING;

-- Order 2, Item 9 (Tonkotsu Ramen): Cay Vừa + Thêm Chashu
INSERT INTO order_item_modifiers (id, version, order_item_id, modifier_option_id, additional_price, quantity, created_at, updated_at) VALUES
    ('om000000-0000-0000-0000-000000000005', 0, 'oi000000-0000-0000-0000-000000000009', 'mo000000-0000-0000-0000-000000000021', 0.00, 1, NOW(), NOW()),     -- Cay Vừa
    ('om000000-0000-0000-0000-000000000006', 0, 'oi000000-0000-0000-0000-000000000009', 'mo000000-0000-0000-0000-000000000023', 30000.00, 1, NOW(), NOW())  -- Thêm Chashu +30k
    ON CONFLICT (id) DO NOTHING;

-- Order 3, Item 12 (Cơm Tấm): Thêm Sườn
INSERT INTO order_item_modifiers (id, version, order_item_id, modifier_option_id, additional_price, quantity, created_at, updated_at) VALUES
    ('om000000-0000-0000-0000-000000000007', 0, 'oi000000-0000-0000-0000-000000000012', 'mo000000-0000-0000-0000-000000000016', 25000.00, 1, NOW(), NOW())  -- Thêm Sườn +25k
    ON CONFLICT (id) DO NOTHING;

-- =============================================================================
-- 4. UPDATE TABLE STATUS (sync with orders)
-- =============================================================================

-- Bàn 02: OCCUPIED (Order 1 - PENDING)
UPDATE restaurant_tables SET status = 'OCCUPIED', updated_at = NOW()
WHERE table_id = 't0000000-0000-0000-0000-000000000002';

-- Bàn 05: OCCUPIED (Order 2 - PENDING)
UPDATE restaurant_tables SET status = 'OCCUPIED', updated_at = NOW()
WHERE table_id = 't0000000-0000-0000-0000-000000000005';
