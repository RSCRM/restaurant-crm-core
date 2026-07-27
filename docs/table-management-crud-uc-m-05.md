# DATABASE DESIGN — HIỆN TRẠNG (Reverse-engineered từ JPA Entities)

> Tài liệu này mô tả **thiết kế database hiện tại** được suy ra trực tiếp từ các `@Entity` trong
> `src/main/java/com/restaurant/crm/modules/**`. Đây là **as-built** (đúng với code đang chạy,
> `spring.jpa.hibernate.ddl-auto=update`), **không phải** thiết kế mục tiêu trong `documents/database/*`.
> Mọi khác biệt so với data dictionary được liệt kê ở mục "Khác biệt / Vấn đề" cuối file.

Tổng: **30 entity** / bảng nghiệp vụ + **3 bảng nối (join table)**.

---

## 0. Quy ước chung

### 0.1. `BaseEntity` (MappedSuperclass — mọi bảng đều kế thừa)

| Cột | Kiểu | Ghi chú |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK, `GenerationType.UUID`. Mặc định tên cột `id`; một số bảng override tên (xem cột PK riêng). |
| `version` | BIGINT | Optimistic locking (`@Version`). |
| `created_by` | VARCHAR | `@CreatedBy`, không cập nhật. |
| `updated_by` | VARCHAR | |
| `created_at` | TIMESTAMP | set ở `@PrePersist`, không cập nhật. |
| `updated_at` | TIMESTAMP | set ở `@PrePersist`/`@PreUpdate`. |

### 0.2. Quy ước ký hiệu

- **Enum** lưu dạng chuỗi (`@Enumerated(EnumType.STRING)`) → cột VARCHAR.
- **FK cứng** = quan hệ JPA (`@ManyToOne`/`@OneToOne`) → có ràng buộc FK thật.
- **FK mềm** 🔸 = cột `String` trần (chỉ lưu id, **không** có ràng buộc FK).
- `restaurant_id` (module CRM) và `branch_id` (module ERP) **cùng trỏ tới** `organization_branches.id` — khác tên, xem mục vấn đề.

---

## 1. Module `identity`

### 1.1. `users`
| Cột | Kiểu | Ràng buộc | Mặc định |
| :--- | :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK | |
| `username` | VARCHAR | NOT NULL, UNIQUE | |
| `password` | VARCHAR | NOT NULL | |
| `email` | VARCHAR | NOT NULL, UNIQUE | |
| `status` | VARCHAR | NOT NULL — enum `UserStatus` | |
| `enabled` | BOOLEAN | NOT NULL | false |

Quan hệ: `users N—M roles` qua join table **`user_roles`**(`user_id`,`role_id`).

### 1.2. `roles`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `role_name` | VARCHAR | NOT NULL, UNIQUE |

Quan hệ: `roles N—M permissions` qua join table ẩn (Hibernate tự đặt tên).

### 1.3. `permissions`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `permission_name` | VARCHAR | NOT NULL, UNIQUE |

---

## 2. Module `profile`

### 2.1. `user_profiles`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `user_id` | VARCHAR | NOT NULL, **UNIQUE** (`uk_user_profiles_user_id`), FK→`users.id` (OneToOne, ON DELETE CASCADE) |
| `full_name` | VARCHAR | |
| `phone` | VARCHAR | UNIQUE (`uk_user_profiles_phone`) |

---

## 3. Module `erp/organization`

### 3.1. `organizations`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `owner_id` | VARCHAR | NOT NULL 🔸 (trỏ `users.id`) |
| `organization_name` | VARCHAR | NOT NULL |
| `tax_code` | VARCHAR | |
| `address` | VARCHAR | |
| `phone` | VARCHAR | |
| `email` | VARCHAR | |
| `status` | VARCHAR | NOT NULL, default `ACTIVE` — enum `OrganizationStatus` |

### 3.2. `organization_branches`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `organization_id` | VARCHAR | NOT NULL, FK→`organizations.id` |
| `manager_id` | VARCHAR | UNIQUE 🔸 |
| `branch_name` | VARCHAR | NOT NULL |
| `address` | VARCHAR | |
| `phone` | VARCHAR | |
| `status` | VARCHAR | NOT NULL, default `ACTIVE` — enum `OrganizationBranchStatus` |

### 3.3. `employees`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `user_id` | VARCHAR | NOT NULL, FK→`users.id` |
| `org_role_id` | VARCHAR | NULL, FK→`org_roles.id` |
| `branch_id` | VARCHAR | NULL, FK→`organization_branches.id` |
| `status` | VARCHAR | NOT NULL, default `ACTIVE` — enum `EmployeeStatus` |
| `email` | VARCHAR | NOT NULL |
| `phone` | VARCHAR | NULL |
| `start_date` | DATE | NOT NULL |
| `end_date` | DATE | NULL |
| `salary` | DECIMAL(15,2) | NULL |

### 3.4. `org_roles`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `role_name` | VARCHAR(50) | NOT NULL, UNIQUE |

Quan hệ: `org_roles N—M org_permissions` qua join table ẩn.

### 3.5. `org_permissions`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `permission_name` | VARCHAR(100) | NOT NULL, UNIQUE |

---

## 4. Module `erp/table`

### 4.1. `table_areas`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `area_id` | VARCHAR(36) | PK (id override) |
| `branch_id` | VARCHAR(36) | NOT NULL 🔸 |
| `area_name` | VARCHAR(100) | NOT NULL |
| `description` | VARCHAR(255) | |
| `display_order` | INT | |
| — | | UNIQUE(`branch_id`,`area_name`) = `uk_table_areas_branch_area_name` |

### 4.2. `restaurant_tables`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `table_id` | VARCHAR(36) | PK (id override) |
| `area_id` | VARCHAR | NOT NULL, FK→`table_areas.area_id` |
| `table_number` | VARCHAR(20) | NOT NULL |
| `capacity` | INT | NOT NULL, ≥1 |
| `status` | VARCHAR(20) | NOT NULL, default `AVAILABLE` — enum `RestaurantTableStatus` |
| `position_x` | INT | |
| `position_y` | INT | |
| — | | UNIQUE(`area_id`,`table_number`); index: table_number, status, capacity |

---

## 5. Module `erp/menu`

### 5.1. `products`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `product_id` | VARCHAR(36) | PK (id override) |
| `branch_id` | VARCHAR(36) | NOT NULL 🔸 |
| `category_id` | VARCHAR(36) | NOT NULL 🔸 ⚠️ **không có entity/bảng category** |
| `product_name` | VARCHAR(150) | NOT NULL |
| `description` | TEXT | |
| `price` | DECIMAL(10,2) | NOT NULL, default 0 |
| `image_url` | VARCHAR(255) | |
| `status` | VARCHAR(20) | NOT NULL, default `AVAILABLE` — **String tự do, không enum** |
| `requires_preparation` | BOOLEAN | NOT NULL, default true |
| — | | UNIQUE(`branch_id`,`product_name`) |

### 5.2. `combos`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `combo_id` | VARCHAR(36) | PK (id override) |
| `branch_id` | VARCHAR(36) | NOT NULL 🔸 |
| `combo_name` | VARCHAR(150) | NOT NULL |
| `description` | TEXT | |
| `price` | DECIMAL(10,2) | NOT NULL, default 0 |
| `image_url` | VARCHAR(255) | |
| `status` | VARCHAR(20) | NOT NULL, default `AVAILABLE` — String tự do |
| — | | UNIQUE(`branch_id`,`combo_name`) |

> ⚠️ Không có bảng `combo_items` (thành phần combo).

### 5.3. `modifier_groups`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `modifier_group_id` | VARCHAR(36) | PK (id override) |
| `branch_id` | VARCHAR(36) | NOT NULL 🔸 |
| `group_name` | VARCHAR(100) | NOT NULL |
| `description` | VARCHAR(255) | |
| `min_selection` | INT | NOT NULL, default 0 |
| `max_selection` | INT | NOT NULL, default 1 |
| — | | UNIQUE(`branch_id`,`group_name`) |

### 5.4. `modifier_options`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `modifier_option_id` | VARCHAR(36) | PK (id override) |
| `modifier_group_id` | VARCHAR | NOT NULL, FK→`modifier_groups.modifier_group_id` |
| `option_name` | VARCHAR(100) | NOT NULL |
| `additional_price` | DECIMAL(10,2) | NOT NULL, default 0 |
| `status` | VARCHAR(20) | NOT NULL, default `AVAILABLE` — String tự do |
| — | | UNIQUE(`modifier_group_id`,`option_name`) |

> ⚠️ Không có bảng `product_modifier_groups` (gán nhóm modifier cho product).

---

## 6. Module `erp/order`

### 6.1. `orders`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `branch_id` | VARCHAR(36) | NOT NULL 🔸 |
| `table_id` | VARCHAR(36) | NULL 🔸 |
| `reservation_id` | VARCHAR(36) | NULL 🔸 ⚠️ **không có bảng reservation** |
| `order_code` | VARCHAR | NOT NULL |
| `order_type` | VARCHAR | NOT NULL — enum `OrderType` |
| `status` | VARCHAR | NOT NULL, default `PENDING` — enum `OrderStatus` |
| `customer_name` | VARCHAR | NULL (denormalized) |
| `customer_phone` | VARCHAR | NULL (denormalized) |
| `note` | VARCHAR | |
| `subtotal` | DECIMAL | NOT NULL, default 0 |
| `discount_amount` | DECIMAL | NOT NULL, default 0 |
| `total_amount` | DECIMAL | NOT NULL, default 0 |
| — | | UNIQUE(`branch_id`,`order_code`) |

> Không có FK tới `customers`; khách chỉ lưu tên/sđt phẳng.

### 6.2. `order_items`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `order_id` | VARCHAR | NOT NULL, FK→`orders.id` |
| `product_id` | VARCHAR(36) | NULL 🔸 |
| `combo_id` | VARCHAR(36) | NULL 🔸 (product_id/combo_id không ràng buộc XOR) |
| `quantity` | INT | NOT NULL |
| `unit_price` | DECIMAL | NOT NULL, default 0 |
| `subtotal` | DECIMAL | NOT NULL, default 0 |
| `status` | VARCHAR | NOT NULL, default `PENDING` — enum `OrderItemStatus` |
| `note` | VARCHAR | |
| `prepared_by` | VARCHAR(36) | 🔸 |
| `cancel_reason` | VARCHAR | |
| `cancelled_by` | VARCHAR(36) | 🔸 |

### 6.3. `order_item_modifiers`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `order_item_id` | VARCHAR | NOT NULL, FK→`order_items.id` |
| `modifier_option_id` | VARCHAR(36) | NOT NULL 🔸 |
| `additional_price` | DECIMAL | NOT NULL, default 0 |
| `quantity` | INT | NOT NULL, default MIN_QUANTITY |

---

## 7. Module `erp/booking`

### 7.1. `bookings`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `branch_id` | VARCHAR | NOT NULL 🔸 (TODO refactor → FK) |
| `table_id` | VARCHAR | NULL 🔸 (TODO refactor → FK) |
| `customer_id` | VARCHAR | NOT NULL, FK→`customers.id` |
| `booking_time` | TIMESTAMP | NOT NULL |
| `guest_count` | INT | NOT NULL, ≥ MIN_GUEST_COUNT |
| `status` | VARCHAR | NOT NULL, default `PENDING` — enum `BookingStatus` |
| `note` | VARCHAR | |

> Đây là "reservation" nghiệp vụ nhưng bảng tên `bookings`; `orders.reservation_id` trỏ tới khái niệm không cùng tên.

---

## 8. Module `erp/invoice`

### 8.1. `invoices`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `order_id` | VARCHAR | NOT NULL, **UNIQUE**, FK→`orders.id` (OneToOne) |
| `invoice_code` | VARCHAR | NOT NULL, UNIQUE |
| `subtotal` | DECIMAL | NOT NULL, default 0 |
| `discount_amount` | DECIMAL | NOT NULL, default 0 |
| `total_amount` | DECIMAL | NOT NULL, default 0 |
| `payment_method` | VARCHAR | NOT NULL — enum `PaymentMethod` |
| `status` | VARCHAR | NOT NULL, default `PAID` — enum `InvoiceStatus` |
| `paid_at` | TIMESTAMP | NOT NULL |
| `refunded_at` | TIMESTAMP | NULL |
| `refund_reason` | VARCHAR | NULL |
| `note` | VARCHAR | NULL |

---

## 9. Module `erp/inventory`

### 9.1. `ingredient_categories`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `branch_id` | VARCHAR | NOT NULL, FK→`organization_branches.id` |
| `category_name` | VARCHAR | NOT NULL |
| `description` | VARCHAR | |

### 9.2. `ingredients`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `branch_id` | VARCHAR | NOT NULL, FK→`organization_branches.id` |
| `ingredient_category_id` | VARCHAR | NOT NULL, FK→`ingredient_categories.id` |
| `ingredient_name` | VARCHAR | NOT NULL |
| `unit` | VARCHAR | NOT NULL |
| `description` | VARCHAR | |

### 9.3. `inventories`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `ingredient_id` | VARCHAR | NOT NULL, **UNIQUE**, FK→`ingredients.id` (OneToOne) |
| `quantity` | DECIMAL | NOT NULL, default 0 |
| `minimum_quantity` | DECIMAL | NOT NULL, default 0 |
| `status` | VARCHAR | NOT NULL, default `GOOD` — enum `InventoryStatus` |

### 9.4. `inventory_transactions`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `inventory_id` | VARCHAR | NOT NULL, FK→`inventories.id` |
| `employee_id` | VARCHAR | NULL, FK→`employees.id` |
| `transaction_type` | VARCHAR | NOT NULL — enum `InventoryTransactionType` |
| `transaction_direction` | VARCHAR | NOT NULL — enum `InventoryTransactionDirection` |
| `quantity` | DECIMAL | NOT NULL |
| `note` | VARCHAR | |
| `reference_id` | VARCHAR | 🔸 |
| `transaction_time` | TIMESTAMP | NOT NULL |

---

## 10. Module `erp/schedule` & `erp/attendance`

### 10.1. `work_schedules`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `employee_id` | VARCHAR | NOT NULL, FK→`employees.id` |
| `branch_id` | VARCHAR | NOT NULL, FK→`organization_branches.id` |
| `work_date` | DATE | NOT NULL |
| `start_time` | TIME | NOT NULL |
| `end_time` | TIME | NOT NULL |
| `note` | VARCHAR | |
| — | | UNIQUE(`employee_id`,`work_date`,`start_time`) |

> ⚠️ **Không có cột `status`** (khác dictionary vốn có SCHEDULED/COMPLETED/CANCELLED).

### 10.2. `shift_assignments`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `employee_id` | VARCHAR | NOT NULL, FK→`employees.id` |
| `branch_id` | VARCHAR | NOT NULL, FK→`organization_branches.id` |
| `work_date` | DATE | NOT NULL |
| `start_at` | TIMESTAMP | NOT NULL |
| `end_at` | TIMESTAMP | NOT NULL |
| — | | UNIQUE(`employee_id`,`start_at`) = `uk_shift_assignments_employee_start` |

> `shift_assignments` và `work_schedules` **song song, không liên kết** với nhau.

### 10.3. `attendances`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `shift_assignment_id` | VARCHAR | NOT NULL, **UNIQUE**, FK→`shift_assignments.id` (OneToOne) |
| `check_in_at` | TIMESTAMP | NOT NULL |
| `check_out_at` | TIMESTAMP | NULL |
| `status` | VARCHAR | NOT NULL — enum `AttendanceStatus` (chỉ `ON_TIME`,`LATE` — **không có ABSENT**) |

---

## 11. Module `erp/notification`

### 11.1. `notifications`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `branch_id` | VARCHAR | NOT NULL 🔸 |
| `recipient_id` | VARCHAR | NULL 🔸 |
| `sender_id` | VARCHAR | NOT NULL 🔸 |
| `title` | VARCHAR | NOT NULL |
| `content` | VARCHAR | NOT NULL |
| `type` | VARCHAR | NOT NULL — enum `NotificationType` (chỉ `READY_TO_SERVE`) |
| `status` | VARCHAR | NOT NULL, default `UNREAD` — enum `NotificationStatus` |

---

## 12. Module `crm`

### 12.1. `customers`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `phone` | VARCHAR | NOT NULL, UNIQUE |
| `status` | VARCHAR | NOT NULL, default `ACTIVE` — enum `CustomerStatus` |

### 12.2. `customer_point`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `customer_id` | VARCHAR | NOT NULL, FK→`customers.id` |
| `restaurant_id` | VARCHAR | NOT NULL 🔸 (= branch) |
| `current_points` | INT | NOT NULL, default 0 |
| `lifetime_points` | INT | NOT NULL, default 0 |
| — | | UNIQUE(`customer_id`,`restaurant_id`) = `uk_customer_restaurant` |

### 12.3. `customer_point_history`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `customer_id` | VARCHAR | NOT NULL, FK→`customers.id` |
| `restaurant_id` | VARCHAR | NOT NULL 🔸 |
| `transaction_type` | VARCHAR | NOT NULL — enum `PointTransactionType` |
| `points_changed` | INT | NOT NULL |
| `reference_id` | VARCHAR | 🔸 (thường trỏ `orders.id`) |

### 12.4. `vouchers`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `restaurant_id` | VARCHAR | NOT NULL 🔸 |
| `title` | VARCHAR | NOT NULL |
| `discount_percent` | INT | NOT NULL (0..100) |
| `min_bill_amount` | DECIMAL | NOT NULL, default 0 |
| `points_required` | INT | NOT NULL, default 0 |
| `is_active` | SMALLINT | NOT NULL, default 1 (**không phải boolean**) |
| `expired_at` | TIMESTAMP | NULL |

### 12.5. `customer_vouchers`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `customer_id` | VARCHAR | NOT NULL, FK→`customers.id` |
| `restaurant_id` | VARCHAR | NOT NULL 🔸 |
| `voucher_id` | VARCHAR | NOT NULL, FK→`vouchers.id` |
| `voucher_sn` | VARCHAR | NOT NULL, UNIQUE |
| `status` | VARCHAR | NOT NULL, default `AVAILABLE` — enum `CustomerVoucherStatus` |
| `used_at` | TIMESTAMP | NULL |
| `order_id` | VARCHAR | NULL 🔸 |

### 12.6. `feedbacks`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `customer_id` | VARCHAR | NOT NULL, FK→`customers.id` |
| `restaurant_id` | VARCHAR | NOT NULL 🔸 |
| `order_id` | VARCHAR | NOT NULL, UNIQUE 🔸 |
| `rating_food` | INT | NOT NULL (1..5) |
| `rating_service` | INT | NOT NULL (1..5) |
| `comment` | VARCHAR | |

---

## 13. Module `licensemanagement`

### 13.1. `license`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `code` | VARCHAR | NOT NULL, UNIQUE |
| `name` | VARCHAR | NOT NULL |
| `description` | TEXT | |
| `price` | DECIMAL | NOT NULL |
| `billing_cycle` | VARCHAR | NOT NULL — enum `BillingCycle` |
| `max_branch` | INT | NOT NULL |
| `max_employee` | INT | NOT NULL |
| `status` | VARCHAR | NOT NULL — enum `LicenseStatus` |
| `deleted_at` | TIMESTAMP | NULL (soft-delete) |

### 13.2. `license_subscription`
| Cột | Kiểu | Ràng buộc |
| :--- | :--- | :--- |
| `id` | VARCHAR(36) | PK |
| `license_id` | VARCHAR | NOT NULL 🔸 |
| `organization_id` | VARCHAR | NOT NULL 🔸 |
| `start_date` | DATE | NOT NULL |
| `end_date` | DATE | NOT NULL |
| `status` | VARCHAR | NOT NULL — enum `SubscriptionStatus` |
| `price` | DECIMAL | NOT NULL |
| `billing_cycle` | VARCHAR | NOT NULL — enum `BillingCycle` |
| `max_branch` | INT | NOT NULL |
| `max_employee` | INT | NOT NULL |

> Bảng nối license↔organization, nhưng cả 2 khóa là **FK mềm** (String).

---

## 14. Bảng nối (join tables)

| Join table | Cột | Nguồn | Ghi chú |
| :--- | :--- | :--- | :--- |
| `user_roles` | `user_id`, `role_id` | `User.roles` | khai báo tường minh |
| (ẩn) roles↔permissions | | `Role.permissions` | **không** khai `@JoinTable` → Hibernate tự đặt tên |
| (ẩn) org_roles↔org_permissions | | `OrgRole.orgPermissions` | tương tự |

---

## 15. Danh mục Enum (lưu VARCHAR)

| Enum | Giá trị | Dùng ở |
| :--- | :--- | :--- |
| `UserStatus` | ACTIVE, BLOCKED, DELETED | users.status |
| `CustomerStatus` | ACTIVE, LOCKED | customers.status |
| `EmployeeStatus` | ACTIVE, INACTIVE, TERMINATED | employees.status |
| `OrganizationStatus` | ACTIVE, INACTIVE, SUSPENDED | organizations.status |
| `OrganizationBranchStatus` | ACTIVE, INACTIVE, CLOSED | organization_branches.status |
| `RestaurantTableStatus` | AVAILABLE, OCCUPIED, RESERVED | restaurant_tables.status |
| `OrderType` | DINE_IN, TAKEAWAY, DELIVERY | orders.order_type |
| `OrderStatus` | PENDING, PAID, CANCELLED | orders.status |
| `OrderItemStatus` | PENDING, IN_PROGRESS, READY_TO_SERVE, SERVED, CANCELLED | order_items.status |
| `BookingStatus` | PENDING, CONFIRMED, SEATED, CANCELLED, EXPIRED | bookings.status |
| `InvoiceStatus` | PAID, REFUNDED | invoices.status |
| `PaymentMethod` | CASH, BANKING, CREDIT_CARD | invoices.payment_method |
| `InventoryStatus` | GOOD, LOW, OUT_OF_STOCK | inventories.status |
| `InventoryTransactionType` | PURCHASE, SALE, ADJUSTMENT, WASTE, RETURN | inventory_transactions.transaction_type |
| `InventoryTransactionDirection` | IN, OUT | inventory_transactions.transaction_direction |
| `AttendanceStatus` | ON_TIME, LATE | attendances.status |
| `NotificationStatus` | UNREAD, READ | notifications.status |
| `NotificationType` | READY_TO_SERVE | notifications.type |
| `CustomerVoucherStatus` | AVAILABLE, USED, EXPIRED, CANCELLED | customer_vouchers.status |
| `PointTransactionType` | EARN, REDEEM, EXPIRE, ADJUSTMENT | customer_point_history.transaction_type |
| `LicenseStatus` | ACTIVE, LOCKED | license.status |
| `SubscriptionStatus` | ACTIVE, EXPIRED, REVOKED | license_subscription.status |
| `BillingCycle` | MONTHLY, YEARLY | license/subscription.billing_cycle |
| products/combos/modifier_options `.status` | (String tự do) default "AVAILABLE" | **chưa enum hóa** |

---

## 16. Khác biệt / Vấn đề so với data dictionary (`documents/database/*`)

1. **FK xuyên module chủ yếu là "mềm"** (`String`): mọi `branch_id`, `restaurant_id`, `owner_id`, `manager_id`, `product_id`, `combo_id`, `table_id`, `reservation_id`, `modifier_option_id`… không có ràng buộc FK. Chỉ vài quan hệ trong cùng module là FK cứng.
2. **Lệch danh pháp `restaurant_id` (CRM) vs `branch_id` (ERP)** — cùng trỏ `organization_branches.id`.
3. **`products.category_id` là FK treo** — không có entity/bảng `product_categories`/`categories`.
4. **Thiếu bảng so với dictionary**: `categories`, `combo_items`, `product_modifier_groups`, `reservations` (chỉ có `bookings`), `promotions`, `promotion_conditions`, `promotion_products`, `branch_promotions`, `employee_roles`, `role_permissions` (đang là join ẩn).
5. **`work_schedules` không có cột `status`**; **`attendances.status` không có `ABSENT`** → không biểu diễn "vắng mặt".
6. **Enum code lệch dictionary**: `orders.status` (PENDING/PAID/CANCELLED vs dict 6 giá trị), `invoices.status` (thiếu PARTIAL_REFUND), `payment_method` (BANKING/CREDIT_CARD vs CARD/ONLINE), `inventory_transactions.transaction_type` (PURCHASE/SALE/WASTE/RETURN vs IMPORT/EXPORT/USAGE), `bookings.status`, `customer_vouchers.status`, `point.transaction_type`.
7. **`restaurant_tables.status` chỉ 3 giá trị** nhưng BR-RES-TBL-02 (SRS) yêu cầu 5 trạng thái vòng đời → state machine chưa map được.
8. **products/combos/modifier_options.status để String tự do**, không enum → không ràng buộc giá trị.
9. **`vouchers.is_active` là SMALLINT (0/1)** thay vì boolean.
10. **`orders` không liên kết `customers`** — chỉ lưu `customer_name`/`customer_phone` phẳng; trong khi feedback/point cần cả `customer_id` lẫn `order_id`.
11. **RBAC hai hệ song song**: `roles`/`permissions` (identity) và `org_roles`/`org_permissions` (erp) — trùng khái niệm, khác bảng.
12. **`license_subscription` khóa mềm** và trùng lặp field cấu hình (`price`,`billing_cycle`,`max_branch`,`max_employee`) với `license`.

---

_Sinh từ: đọc trực tiếp `@Entity` (ddl-auto=update). VARCHAR không ghi length nghĩa là entity không khai `columnDefinition`/`length` → Hibernate dùng mặc định (thường VARCHAR(255))._
