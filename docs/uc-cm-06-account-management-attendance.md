# UC-CM-06 — Account Management: Attendance

## 1. Mục tiêu

Cho phép nhân viên check-in bằng QR động tại đúng chi nhánh, check-out và xem lịch sử chấm công cá nhân.

## 2. Actor và phạm vi

- Actor chính: nhân viên đã đăng nhập và đã chọn organization context.
- Màn hình tại chi nhánh lấy QR hiện hành bằng context có authority `BRANCH_MANAGER`; nhân viên thường không được tự phát hành QR.
- Hệ thống lấy `userId`, `employeeId`, `organizationId`, `branchId` từ JWT context; client không được truyền thay.
- Ngoài phạm vi: giao diện, tính lương, duyệt điều chỉnh công, thiết bị sinh trắc học và xác minh vị trí địa lý.

## 3. Business rules của QR động

- Mỗi branch có một `dailySecret` mới theo từng ngày UTC.
- Server dẫn xuất `dailySecret` từ secret hệ thống, ngày UTC, `organizationId` và `branchId`; không lưu hoặc trả secret cho client.
- QR được đổi theo phiên 60 giây.
- QR là token HS512 đã ký gồm `organizationId`, `branchId`, `qrSessionId`, `issuedAt`, `expiresAt`, `nonce` và signature.
- Đồng hồ server là nguồn thời gian duy nhất để phát hành và kiểm tra QR.
- Một QR có thể được nhiều nhân viên tại cùng branch quét trong thời gian hiệu lực; chống check-in lặp dựa trên shift assignment, không vô hiệu QR sau lần quét đầu tiên.

## 4. Tiền điều kiện

- Employee tồn tại, thuộc user hiện tại và đang `ACTIVE`.
- Employee có ca làm việc chứa thời điểm check-in hiện tại.
- Ca thuộc cùng organization và branch với QR và JWT context.
- PostgreSQL đã có schema code-first cho `shift_assignments` và `attendances`.

## 5. Luồng chính

### Lấy QR hiện hành

1. Màn hình tại branch gọi `GET /api/v1/erp/attendances/qr` bằng context token có authority `BRANCH_MANAGER`.
2. Server kiểm tra branch đang `ACTIVE` và thuộc organization trong JWT.
3. Server trả QR token của phiên 60 giây hiện hành cùng `issuedAt` và `expiresAt`.

### Check-in

1. Nhân viên quét QR và gọi `POST /api/v1/erp/attendances/check-in` với body `{ "qrToken": "..." }`.
2. Server xác thực employee từ JWT và kiểm tra employee đang `ACTIVE`.
3. Server kiểm tra thuật toán và chữ ký QR bằng daily secret của branch.
4. Server kiểm tra `issuedAt`, `expiresAt`, loại token, `qrSessionId` và `nonce`.
5. Server kiểm tra organization/branch trong QR khớp JWT context và ca làm việc.
6. Server tìm ca hiện hành và từ chối nếu ca đã check-in.
7. Server lưu thời điểm vào và trạng thái `ON_TIME` hoặc `LATE`.

### Check-out

1. Nhân viên gọi `POST /api/v1/erp/attendances/check-out`.
2. Server tìm bản ghi chưa check-out gần nhất của employee.
3. Server lưu thời điểm ra và trả bản ghi cập nhật.

### Xem lịch sử

1. Nhân viên gọi `GET /api/v1/erp/attendances/me`.
2. Có thể lọc `from`, `to`, `page`, `size`.
3. Server chỉ trả dữ liệu của employee trong JWT.

## 6. Luồng lỗi

- Employee không tồn tại/không thuộc user: `EMP_1000`.
- Không có ca hiện hành: `ATT_1000`.
- Ca đã check-in: `ATT_1001`.
- Không có bản ghi đang mở để check-out: `ATT_1002`.
- Khoảng ngày không hợp lệ: `ATT_1003`.
- QR sai định dạng, thuật toán hoặc chữ ký: `ATT_1004`.
- QR hết hạn: `ATT_1005`.
- QR sai organization/branch hoặc không khớp ca: `ATT_1006`.
- Server không thể phát hành QR: `ATT_1007`.

## 7. Acceptance criteria

- AC1: QR của branch thay đổi sau mỗi 60 giây và hết hiệu lực khi hết phiên.
- AC2: QR bị sửa, sai chữ ký hoặc sai thuật toán bị từ chối.
- AC3: QR hết hạn bị từ chối theo đồng hồ server.
- AC4: QR khác organization, branch hoặc branch của shift bị từ chối.
- AC5: Employee không `ACTIVE` hoặc không có ca hiện hành bị từ chối.
- AC6: Check-in đúng QR và đúng ca tạo đúng một attendance với trạng thái `ON_TIME` hoặc `LATE`.
- AC7: Check-in cùng ca lần hai bị từ chối.
- AC8: Check-out cập nhật đúng attendance đang mở.
- AC9: Lịch sử chỉ chứa dữ liệu của employee hiện tại và đúng khoảng ngày.
- AC10: API không nhận `employeeId`, `organizationId` hoặc `branchId` từ client.

## 8. DB code-first tạm thời

- `shift_assignments`: employee, branch, work_date, start_at, end_at.
- `attendances`: shift_assignment, check_in_at, check_out_at, status.
- Unique: một employee không có hai assignment cùng `start_at`; một assignment có tối đa một attendance.
- QR và daily secret không cần bảng mới vì token tự chứa thời hạn và daily secret được dẫn xuất ở server.

## 9. Rủi ro còn lại

QR động giới hạn thời gian chia sẻ còn tối đa 60 giây nhưng không ngăn hoàn toàn relay theo thời gian thực. Nếu nghiệp vụ yêu cầu xác minh nhân viên thật sự có mặt tại branch, cần bổ sung geolocation, Wi-Fi nội bộ hoặc kiosk/device binding.
