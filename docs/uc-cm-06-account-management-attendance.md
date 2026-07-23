# UC-CM-06 — Account Management: Attendance

## 1. Mục tiêu

Cho phép nhân viên chấm công vào/ra đúng ca được phân công và xem lịch sử chấm công cá nhân.

## 2. Actor và phạm vi

- Actor chính: nhân viên đã đăng nhập và đã chọn organization context.
- Hệ thống lấy `userId`, `employeeId`, `branchId` từ JWT context; client không được truyền thay.
- Ngoài phạm vi: giao diện, tính lương, duyệt điều chỉnh công, thiết bị sinh trắc học.

## 3. Tiền điều kiện

- Employee tồn tại, thuộc user hiện tại và đang `ACTIVE`.
- Có ca làm việc chứa thời điểm check-in hiện tại.
- PostgreSQL đã có schema code-first cho `shift_assignments` và `attendances`.

## 4. Luồng chính

### Check-in

1. Nhân viên gọi `POST /api/v1/erp/attendances/check-in`.
2. Hệ thống xác thực employee từ JWT.
3. Hệ thống tìm ca hiện hành của employee.
4. Nếu chưa chấm công, hệ thống lưu thời điểm vào và trạng thái `ON_TIME` hoặc `LATE`.
5. Trả bản ghi chấm công.

### Check-out

1. Nhân viên gọi `POST /api/v1/erp/attendances/check-out`.
2. Hệ thống tìm bản ghi chưa check-out gần nhất của employee.
3. Lưu thời điểm ra và trả bản ghi cập nhật.

### Xem lịch sử

1. Nhân viên gọi `GET /api/v1/erp/attendances/me`.
2. Có thể lọc `from`, `to`, `page`, `size`.
3. Hệ thống chỉ trả dữ liệu của employee trong JWT.

## 5. Luồng lỗi

- Employee không tồn tại/không thuộc user: `EMP_1000`.
- Không có ca hiện hành: `ATT_1000`.
- Ca đã check-in: `ATT_1001`.
- Không có bản ghi đang mở để check-out: `ATT_1002`.
- Khoảng ngày không hợp lệ: `ATT_1003`.

## 6. Acceptance criteria

- AC1: Check-in đúng ca tạo đúng một bản ghi.
- AC2: Check-in cùng ca lần hai bị từ chối.
- AC3: Check-in ngoài ca bị từ chối.
- AC4: Check-out cập nhật đúng bản ghi đang mở.
- AC5: Lịch sử chỉ chứa dữ liệu của employee hiện tại và đúng khoảng ngày.
- AC6: Response dùng `ApiResponse<T>`; danh sách dùng `PagingResponse<T>`.
- AC7: Không nhận `employeeId`/`branchId` từ client.

## 7. DB code-first tạm thời

- `shift_assignments`: employee, branch, work_date, start_at, end_at.
- `attendances`: shift_assignment, check_in_at, check_out_at, status.
- Unique: một employee không có hai assignment cùng `start_at`; một assignment có tối đa một attendance.

