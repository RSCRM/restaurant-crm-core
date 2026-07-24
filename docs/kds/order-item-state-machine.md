# Order Item State Machine

Đặc tả vòng đời trạng thái của `order_items`. Tài liệu dùng chung cho:

| Task | PIC | Vai trò |
|---|---|---|
| uc-scf-01 → uc-scf-06 | DuyNHN3 | Chuyển trạng thái phía bếp |
| uc-sw-11, uc-sw-14 | SangTD6, VuongTH6 | Tạo món, chuyển sang SERVED |
| uc-scr-01 | VuongTH6 | Đọc trạng thái phía thu ngân |
| uc-c-06 | KhoaHD19 | Đọc trạng thái phía khách |

**Trạng thái quyết định (22/07/2026):** A1 duyệt thêm cột, A2 dùng `ddl-auto` không tạo file migration, A3 `prepared_by` trỏ `users`, A4 tách `READY_TO_SERVE` và `SERVED`, A5 trừ kho thuộc bước `SERVED`.

---

## 1. Năm trạng thái

| Status | Ý nghĩa | Ai chuyển vào | Task |
|---|---|---|---|
| `PENDING` | Món vừa được gọi, chờ bếp nhận | Hệ thống, khi tạo order item | uc-sw-06 / uc-c-05 |
| `IN_PROGRESS` | Đầu bếp đã nhận, đang chế biến | Chef | uc-scf-03 |
| `READY_TO_SERVE` | Bếp làm xong, chờ phục vụ bưng ra | Chef | uc-scf-05 |
| `SERVED` | Phục vụ đã bưng ra bàn | Waiter | uc-sw-14 |
| `CANCELLED` | Hủy chế biến | Chef | uc-scf-06 |

`SERVED` và `CANCELLED` là trạng thái cuối, không chuyển đi đâu được nữa.

---

## 2. Chuyển trạng thái hợp lệ

```
PENDING ──────► IN_PROGRESS ──────► READY_TO_SERVE ──────► SERVED
   │                  │
   │                  │
   └──────────────────┴──────────► CANCELLED
```

| Từ | Sang | Ai | Điều kiện |
|---|---|---|---|
| `PENDING` | `IN_PROGRESS` | Chef | Món chưa có ai nhận |
| `PENDING` | `CANCELLED` | Chef | Bắt buộc có `cancel_reason` |
| `IN_PROGRESS` | `READY_TO_SERVE` | Chef | Chỉ người đã nhận món mới được báo xong |
| `IN_PROGRESS` | `CANCELLED` | Chef | Bắt buộc có `cancel_reason` |
| `READY_TO_SERVE` | `SERVED` | Waiter | Ngoài phạm vi KDS |

**Mọi chuyển đổi không có trong bảng đều bị từ chối.** Cụ thể cấm:

- Nhảy cóc: `PENDING → READY_TO_SERVE`, `PENDING → SERVED`, `IN_PROGRESS → SERVED`
- Đi lùi: `IN_PROGRESS → PENDING`, `READY_TO_SERVE → IN_PROGRESS`
- Rời trạng thái cuối: `SERVED → *`, `CANCELLED → *`
- `READY_TO_SERVE → CANCELLED` (xem mục 8, câu hỏi còn mở)

Validate ở **tầng service**, không tin dữ liệu từ client.

---

## 3. Cột bổ sung trên `order_items`

| Cột | Kiểu | Null | Mặc định | Set khi nào |
|---|---|---|---|---|
| `status` | enum / varchar(20) | NOT NULL | `PENDING` | Mỗi lần chuyển trạng thái |
| `prepared_by` | char(36) FK `users.id` | NULL | | Chuyển sang `IN_PROGRESS` |
| `started_at` | timestamp | NULL | | Chuyển sang `IN_PROGRESS` |
| `completed_at` | timestamp | NULL | | Chuyển sang `READY_TO_SERVE` |
| `cancel_reason` | varchar(255) | NULL | | Chuyển sang `CANCELLED` |
| `priority_flag` | boolean | NOT NULL | `false` | Khi tạo, hoặc Manager bật |

`prepared_by` trỏ `users.id` chứ không phải `employees.id` — nhất quán với `createdBy`/`updatedBy` của `BaseEntity`, và giữ được lịch sử ai nấu kể cả khi nhân viên nghỉ việc.

**Lưu ý về A2 (không tạo file migration):** với `ddl-auto=update`, Hibernate chỉ **thêm** cột mới, không sửa và không xóa cột cũ. Nếu sau này đổi độ dài `varchar` hoặc đổi kiểu, schema sẽ không tự cập nhật — phải sửa tay trên DB. Hiện bảng chưa có dữ liệu thật nên chưa ảnh hưởng.

---

## 4. Quy tắc sắp xếp FIFO

```sql
WHERE oi.status IN ('PENDING', 'IN_PROGRESS')
  AND o.branch_id = :currentBranchId
ORDER BY oi.priority_flag DESC, oi.created_at ASC
```

- Món ưu tiên (`priority_flag = true`, khách VVIP theo BR-RES-ORD-04) luôn nằm trên
- Trong cùng mức ưu tiên: món gọi trước hiện trước
- `branch_id` lấy từ bảng `orders`, không lưu trùng trên `order_items`
- **Bắt buộc filter theo `branch_id`** — bếp chi nhánh A không được thấy món chi nhánh B (NFR-07)

Món `READY_TO_SERVE`, `SERVED`, `CANCELLED` không hiện trên màn hình bếp.

---

## 5. Acceptance criteria

**uc-scf-01 — Xem danh sách món chờ (FIFO)**
- Màn hình hiện món `PENDING` và `IN_PROGRESS` của đúng chi nhánh hiện tại
- Thứ tự đúng quy tắc mục 4
- Món mới xuất hiện trong vòng 2 giây, không cần bấm tải lại
- Mỗi thẻ món hiện: tên món, số lượng, số bàn, ghi chú, thời gian đã chờ
- Đăng nhập bằng tài khoản chi nhánh khác không thấy được món của chi nhánh này

**uc-scf-03 — Nhận chế biến**
- Món `PENDING` → `IN_PROGRESS`, ghi `prepared_by` và `started_at`
- Hai đầu bếp bấm nhận cùng một món cùng lúc: chỉ một người thành công, người còn lại nhận lỗi rõ ràng
- Món đã `IN_PROGRESS` không cho nhận lại

**uc-scf-05 — Báo hoàn thành**
- Món `IN_PROGRESS` → `READY_TO_SERVE`, ghi `completed_at`
- Chỉ người đang giữ món (`prepared_by`) mới được báo xong
- Món biến mất khỏi màn hình bếp
- Bắn sự kiện sang phục vụ (hợp đồng ở tài liệu riêng)

**uc-scf-06 — Hủy chế biến**
- Món `PENDING` hoặc `IN_PROGRESS` → `CANCELLED`
- `cancel_reason` để trống thì từ chối, báo lỗi ở cả FE và BE
- Món biến mất khỏi màn hình bếp

---

## 6. Ai gọi gì

| Chuyển đổi | Task | Người làm |
|---|---|---|
| Tạo món, `PENDING` | uc-sw-06, uc-c-05 | SangTD6, KhoaHD19 |
| `PENDING → IN_PROGRESS` | uc-scf-03 | DuyNHN3 |
| `IN_PROGRESS → READY_TO_SERVE` | uc-scf-05 | DuyNHN3 |
| `* → CANCELLED` | uc-scf-06 | DuyNHN3 |
| `READY_TO_SERVE → SERVED` | uc-sw-14 | VuongTH6 |
| Trừ kho khi `SERVED` | BR-RES-MNU-01 | VuongTH6 gọi sang HuyTD70 |

Theo A5, bếp **không** kích hoạt trừ kho. Việc đó xảy ra ở bước `SERVED` do phục vụ thực hiện.

---

## 7. Ràng buộc kỹ thuật

- **Chống nhận trùng:** dùng `@Version` sẵn có trong `BaseEntity` (optimistic locking). Hai request đồng thời thì request thứ hai nhận `OptimisticLockException` và trả lỗi nghiệp vụ.
- **Không sửa `status` trực tiếp từ controller.** Mọi chuyển đổi đi qua một method service duy nhất có validate.
- **Tên enum viết hoa, khớp đúng chuỗi ở mục 1.** FE, BE, WebSocket payload dùng chung một bộ giá trị.

---

## 8. Câu hỏi còn mở

| # | Câu hỏi | Đề xuất |
|---|---|---|
| 1 | `READY_TO_SERVE → CANCELLED` có cho phép không? Món đã nấu xong nhưng bị đổ, rơi | Không cho từ màn hình bếp. Nếu cần thì là thao tác của Manager |
| 2 | Có cần cho đầu bếp "nhả món" (`IN_PROGRESS → PENDING`) không? | Không làm trong Week 1 |
| 3 | Cần thêm cột `served_at` không? | Có, nhưng thuộc phạm vi VuongTH6 (uc-sw-14) |
| 4 | Món `CANCELLED` có tính tiền vào bill không? | Không. Cần VuongTH6 xác nhận ở uc-scr-01 |
| 5 | Ai được bật `priority_flag`, và làm ở task nào? | Chưa có task nào. Week 1 chỉ thêm cột, chưa làm UI |

---

*Cập nhật: 22/07/2026 — DuyNHN3*
