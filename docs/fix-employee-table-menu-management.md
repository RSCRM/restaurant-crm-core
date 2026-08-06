# API Contract — Org Role Management & Org Employee Management

Dành cho FE coding agent. Backend: Restaurant CRM Core.

## Quy ước chung

- **Base URL**: `/` (ví dụ `http://localhost:8080`).
- **Auth**: mọi API cần header `Authorization: Bearer <accessToken>` (Context Token sau khi chọn org context).
- **Ai gọi được**:
  - Org Role APIs: chỉ **owner**. Backend gate bằng permission `ORG_ROLE_MANAGE` **và** guard kiểm tra `orgRole == OWNER` (đọc từ claim token). `organizationId` **lấy từ token**, FE không gửi.
  - Employee / Menu / Table APIs: bất kỳ ai có **permission tương ứng** (`hasAuthority`, đọc từ permission claim). Owner giờ cũng là một employee với `orgRole = OWNER` (full permission) nên vào được như nhau.
  - **Branch guard** (tầng service, các API thao tác theo branch): owner (`orgRole == OWNER`) đi thẳng; employee thì `branchId` mục tiêu phải trùng `branchId` trong token, sai branch → `AUTHZ_UNAUTHORIZED`.
- **Content-Type**: `application/json`.

### Response envelope (mọi response)

Thành công:
```json
{
  "success": true,
  "data": { }
}
```
Lỗi:
```json
{
  "success": false,
  "errorMessage": {
    "errorCode": "ORG_ROLE_NAME_EXISTS",
    "message": "Role name already exists in this organization"
  }
}
```
FE đọc `success` để phân nhánh, `errorMessage.errorCode` để map thông báo.

---

# A. ORG ROLE MANAGEMENT

Quản lý org_role (vai trò cấp tổ chức) + gán permission. `organizationId` backend tự lấy từ token — FE **không** gửi. Mọi org_role tạo qua CRUD luôn có `dataScope = BRANCH` (backend set cứng, FE không gửi/không chọn).

**Phân quyền:**
- **Ghi (create/update)** + **list permission**: chỉ **owner** — permission `ORG_ROLE_MANAGE` + guard `orgRole == OWNER`.
- **Xem org-role (list/get)**: **owner HOẶC người có `EMPLOYEE_ROLE_ASSIGN`** — gate `hasAuthority('ORG_ROLE_MANAGE') or hasAuthority('EMPLOYEE_ROLE_ASSIGN')`, chỉ scope theo org trong token (không ép owner). Mục đích: người được quyền gán role cho nhân viên có thể xem danh sách org-role để chọn.

## A1. List permission hệ thống
- **GET** `/api/v1/erp/org-permissions`
- Request: (none)
- Response 200:
```json
{
  "success": true,
  "data": [
    { "id": "perm-uuid-1", "permissionName": "EMPLOYEE_ADD" },
    { "id": "perm-uuid-2", "permissionName": "EMPLOYEE_UPDATE" },
    { "id": "perm-uuid-3", "permissionName": "TABLE_AREA_ADD" }
  ]
}
```

## A2. List org_role của org hiện tại — `ORG_ROLE_MANAGE` hoặc `EMPLOYEE_ROLE_ASSIGN`
- **GET** `/api/v1/erp/org-roles`
- Request: (none) — org lấy từ token, không truyền query.
- **Ai gọi được**: owner (có `ORG_ROLE_MANAGE`) HOẶC người có `EMPLOYEE_ROLE_ASSIGN`. Không ép phải là owner.
- **Lưu ý**: role hệ thống `OWNER` **không** nằm trong danh sách trả về (bị loại) — FE không hiển thị/quản lý role này.
- Response 200:
```json
{
  "success": true,
  "data": [
    {
      "id": "role-uuid-1",
      "organizationId": "org-uuid",
      "roleName": "Cashier",
      "dataScope": "BRANCH",
      "permissions": [
        { "id": "perm-uuid-1", "permissionName": "ORDER_CREATE" },
        { "id": "perm-uuid-2", "permissionName": "PAYMENT_CREATE" }
      ]
    }
  ]
}
```
- Lỗi: `AUTHZ_UNAUTHORIZED` (403) nếu không phải owner của org.

## A3. Get 1 org_role
- **GET** `/api/v1/erp/org-roles/{id}`
- Response 200: object `OrgRole` như một phần tử ở A2.
- Lỗi: `ORG_ROLE_NOT_FOUND` (404), `AUTHZ_UNAUTHORIZED` (403).

## A4. Tạo org_role (kèm map permission)
- **POST** `/api/v1/erp/org-roles`
- Request body:
```json
{
  "roleName": "Cashier",
  "permissionIds": ["perm-uuid-1", "perm-uuid-2"]
}
```
  - `roleName` (bắt buộc, 1–50 ký tự, **unique trong org**), `permissionIds` (mảng, có thể rỗng `[]`).
  - **Không** gửi `organizationId` (lấy từ token) và **không** gửi `dataScope` (backend luôn set `BRANCH`).
- Response 201: object `OrgRole` (như A2), `dataScope` = `BRANCH`.
- Lỗi: `ORG_ROLE_NAME_REQUIRED`, `ORG_ROLE_NAME_EXISTS`, `ORG_PERMISSION_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

## A5. Sửa org_role (đổi tên + remap permission)
- **PUT** `/api/v1/erp/org-roles/{id}`
- Request body (không đổi organization; `dataScope` giữ nguyên `BRANCH`, không gửi):
```json
{
  "roleName": "Senior Cashier",
  "permissionIds": ["perm-uuid-1", "perm-uuid-3"]
}
```
- Response 200: object `OrgRole`.
- Lỗi: `ORG_ROLE_NOT_FOUND`, `ORG_ROLE_NAME_REQUIRED`, `ORG_ROLE_NAME_EXISTS`, `ORG_PERMISSION_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

> **Delete org_role**: chưa hỗ trợ (backend hoãn tới khi có soft-delete).

---

# B. ORG EMPLOYEE MANAGEMENT

Base path: `/api/v1/personal/branches`. Employee = 1 user account (1-1) + thông tin nhân sự. Authz: owner hoặc employee có permission ghi trong ngoặc.

### Luồng onboarding (3 bước, bắt buộc theo thứ tự)

Tạo employee **không** kèm gán role. Employee mới luôn ở trạng thái `INACTIVE` và **chưa đăng nhập vào org được** (chọn context sẽ bị chặn) cho tới khi hoàn tất cả 3 bước:

1. **B1** `POST /employees` → tạo account + hồ sơ. Backend set `status = INACTIVE`, `orgRoleName = null`.
2. **B3** `PUT /employees/{id}/role` → gán org_role.
3. **B2** `PUT /employees/{id}` với `{ "status": "ACTIVE" }` → kích hoạt.

Bước 3 **không thể** làm trước bước 2: activate một employee chưa có org_role sẽ trả `EMPLOYEE_ACTIVATE_REQUIRES_ORG_ROLE`. FE nên disable nút "Kích hoạt" khi `orgRoleName == null`.

### `EmployeeResponse` (trả ở mọi API employee bên dưới)
```json
{
  "id": "emp-uuid",
  "username": "cashier01",
  "email": "cashier01@shop.com",
  "phone": "0900000000",
  "fullName": "Nguyen Van A",
  "branchId": "branch-uuid",
  "orgRoleName": "Cashier",
  "salary": 8000000,
  "status": "ACTIVE",
  "startDate": "2026-07-01"
}
```
`status` ∈ `ACTIVE | INACTIVE | TERMINATED` — employee vừa tạo luôn là `INACTIVE`. `orgRoleName` = `null` khi chưa gán role. `fullName` lấy từ `UserProfile` (có thể `null` nếu chưa có profile). `phone` cũng đồng bộ từ profile.

## B0. List employee — permission `EMPLOYEE_VIEW`
- **GET** `/api/v1/personal/branches/employees`
- Request: (none) — không truyền `branchId`/`organizationId`, backend lấy từ token.
- **Phạm vi dữ liệu (theo `dataScope` trong token)**:
  - `dataScope = ORGANIZATION` (vd owner): trả **toàn bộ** employee của org.
  - `dataScope = BRANCH` (vd quản lý/nhân viên chi nhánh): chỉ trả employee thuộc `branchId` trong token.
  - Cả 2 scope đều **luôn loại chính người đang gọi** (không tự thấy mình) **và loại owner** (`orgRole = OWNER`) ra khỏi danh sách.
  - Mỗi item có thêm `fullName` (từ profile). Backend batch-fetch profile theo user (không N+1).
- Response 200:
```json
{
  "success": true,
  "data": [
    {
      "id": "emp-uuid",
      "username": "cashier01",
      "email": "cashier01@shop.com",
      "phone": "0900000000",
      "fullName": "Nguyen Van A",
      "branchId": "branch-uuid",
      "orgRoleName": "Cashier",
      "salary": 8000000,
      "status": "ACTIVE",
      "startDate": "2026-07-01"
    }
  ]
}
```
- Lỗi: `AUTHZ_UNAUTHORIZED` (403) nếu thiếu permission `EMPLOYEE_VIEW`.

## B1. Thêm employee (tạo user + employee) — permission `EMPLOYEE_ADD`
- **POST** `/api/v1/personal/branches/employees`
- Request body:
```json
{
  "username": "cashier01",
  "email": "cashier01@shop.com",
  "fullName": "Nguyen Van A",
  "phone": "0900000000",
  "branchId": "branch-uuid",
  "startDate": "2026-07-01",
  "salary": 8000000
}
```
  - `username`, `email`, `fullName`, `branchId`, `startDate` (bắt buộc). `phone`, `salary` (optional). Mật khẩu account do backend đặt mặc định.
  - **`orgRoleId` KHÔNG còn nhận ở API này** — gán role là hành động riêng, gọi B3 sau khi tạo. Gửi thừa field sẽ bị bỏ qua.
  - Backend insert 1 lúc 3 bảng (`users` + `user_profiles` + `employees`). `fullName` (2–255) → profile; `phone` (nếu gửi, định dạng `^\+?[0-9]{9,15}$`) → set **cả `user_profiles.phone` lẫn `employees.phone`**, **unique toàn hệ thống**.
- Response 201: `EmployeeResponse` với `status` = `"INACTIVE"`, `orgRoleName` = `null`, `fullName`/`phone` như gửi (`phone` = `null` nếu không gửi).
- Lỗi: `EMPLOYEE_USERNAME_REQUIRED`, `EMPLOYEE_EMAIL_REQUIRED`, `EMPLOYEE_EMAIL_INVALID`, `EMPLOYEE_FULL_NAME_REQUIRED`, `EMPLOYEE_FULL_NAME_INVALID`, `EMPLOYEE_PHONE_INVALID`, `EMPLOYEE_BRANCH_REQUIRED`, `EMPLOYEE_START_DATE_REQUIRED`, `USER_USERNAME_ALREADY_EXISTS`, `EMAIL_ALREADY_EXISTS`, `USER_PHONE_ALREADY_EXISTS`, `ORGANIZATION_BRANCH_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

## B2. Cập nhật thông tin employee (update-thường) — permission `EMPLOYEE_UPDATE`
- **PUT** `/api/v1/personal/branches/employees/{id}`
- Request body (**tất cả field optional**, patch-style — chỉ field gửi khác `null` mới cập nhật):
```json
{
  "fullName": "Nguyen Van A",
  "phone": "0911111111",
  "status": "ACTIVE",
  "startDate": "2026-07-01",
  "endDate": null
}
```
  - `fullName` → cập nhật vào **profile** (2–255 ký tự).
  - `phone` → đồng bộ **cả profile lẫn employee**, **duy nhất toàn hệ thống** (trùng → `USER_PHONE_ALREADY_EXISTS`); định dạng `^\+?[0-9]{9,15}$`.
  - `status` → enum `EmployeeStatus` (`ACTIVE|INACTIVE|TERMINATED`). **Đây là API duy nhất để kích hoạt / vô hiệu hoá employee.**
    - Đặt `ACTIVE` chỉ hợp lệ khi employee **đã có org_role** (gán qua B3); chưa có → `EMPLOYEE_ACTIVATE_REQUIRES_ORG_ROLE` (400).
    - `INACTIVE` / `TERMINATED` đặt được bất kỳ lúc nào.
  - `startDate`, `endDate` → employee (update tay). **Lưu ý:** gửi `endDate: null` KHÔNG xoá được endDate (patch-style — null = giữ nguyên).
  - **KHÔNG** đổi `email`/role/salary/branch/account ở API này (email đổi ở luồng khác nếu cần; role ở B3/B4; salary ở B5).
- Response 200: `EmployeeResponse`.
- Lỗi: `EMPLOYEE_NOT_FOUND`, `EMPLOYEE_FULL_NAME_INVALID`, `EMPLOYEE_PHONE_INVALID`, `USER_PHONE_ALREADY_EXISTS`, `EMPLOYEE_ACTIVATE_REQUIRES_ORG_ROLE`, `AUTHZ_UNAUTHORIZED`.

## B3. Gán org_role cho employee — permission `EMPLOYEE_ROLE_ASSIGN`
- **PUT** `/api/v1/personal/branches/employees/{id}/role`
- Request body:
```json
{ "orgRoleId": "role-uuid" }
```
- Dùng cho **cả** lần gán đầu (sau B1) lẫn đổi role về sau. Gán role **không** tự kích hoạt employee — muốn `ACTIVE` phải gọi thêm B2.
- Không tự đổi role của **chính mình** (bị chặn ở backend).
- Response 200: `EmployeeResponse`.
- Lỗi: `EMPLOYEE_NOT_FOUND`, `EMPLOYEE_ORG_ROLE_REQUIRED`, `EMPLOYEE_ORG_ROLE_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

## B4. Gỡ org_role của employee — permission `EMPLOYEE_ROLE_REVOKE`
- **DELETE** `/api/v1/personal/branches/employees/{id}/role`
- Request: (none)
- Idempotent: gỡ khi đã không có role vẫn trả 200.
- **Tự động vô hiệu hoá**: employee đang `ACTIVE` sẽ bị chuyển về `INACTIVE` trong cùng request (vì `ACTIVE` luôn phải đi kèm org_role). Employee đang `INACTIVE` giữ nguyên; đang `TERMINATED` cũng **giữ nguyên** (trạng thái cuối, không bị hạ về `INACTIVE`).
- Muốn dùng lại employee đó: gán role (B3) rồi activate (B2) — đúng bước 2–3 của luồng onboarding.
- Response 200: `EmployeeResponse` (`orgRoleName` = null, `status` = `INACTIVE` nếu trước đó đang `ACTIVE`).
- Lỗi: `EMPLOYEE_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

## B5. Cấu hình lương — permission `EMPLOYEE_UPDATE`
- **PUT** `/api/v1/personal/branches/employees/{id}/salary`
- Request body:
```json
{ "salary": 9000000 }
```
- Response 200: `EmployeeResponse`.
- Lỗi: `EMPLOYEE_NOT_FOUND`, `EMPLOYEE_SALARY_INVALID`, `AUTHZ_UNAUTHORIZED`.

---

## Ghi chú cho FE

- **Bất biến `ACTIVE` ⇒ có org_role**: backend giữ ràng buộc này ở cả hai chiều — không cho activate khi chưa có role (B2), và tự hạ về `INACTIVE` khi gỡ role (B4). FE không cần tự xử lý thứ tự, chỉ cần đọc lại `status` trong response sau mỗi lần gọi B3/B4.
- **Tạo employee ≠ gán role ≠ kích hoạt**: ba hành động tách rời, ba API riêng (B1 → B3 → B2), mỗi cái một permission (`EMPLOYEE_ADD` / `EMPLOYEE_ROLE_ASSIGN` / `EMPLOYEE_UPDATE`). Màn hình "Thêm nhân viên" nên dẫn thẳng người dùng qua đủ 3 bước, nếu không employee tạo ra sẽ nằm im ở `INACTIVE` và không đăng nhập vào org được.
- **List employee**: dùng `B0 GET /employees` (permission `EMPLOYEE_VIEW`), phạm vi tự động theo `dataScope` trong token — FE không cần truyền branch/org. Chưa có API get-1-employee riêng; nếu cần, báo BE.
- **`fullName`/`phone`** quản lý qua chính các API employee ở trên (B1 tạo, B2 sửa) — **KHÔNG** dùng API staff-update của module profile. `phone` là **duy nhất toàn hệ thống**; `fullName` sống trên profile, `phone` đồng bộ cả profile lẫn employee.
- Tất cả `id` là UUID string.
- `salary` là number (BigDecimal) — gửi/nhận number.
- Ngày (`startDate`, `endDate`) format `YYYY-MM-DD`.
- Không gửi `branchId`/`employeeId của người đang thao tác` từ client cho API cần context — backend lấy từ token.
