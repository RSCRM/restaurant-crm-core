# API Contract — Org Role Management & Org Employee Management

Dành cho FE coding agent. Backend: Restaurant CRM Core.

## Quy ước chung

- **Base URL**: `/` (ví dụ `http://localhost:8080`).
- **Auth**: mọi API cần header `Authorization: Bearer <accessToken>` (Context Token sau khi chọn org context).
- **Ai gọi được**:
  - Org Role APIs: **owner** của org (token owner-context, không có `employeeId`). Backend kiểm tra owner sở hữu đúng `organizationId`.
  - Employee write APIs: **owner** HOẶC employee có permission tương ứng (đọc từ permission claim trong token).
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

Quản lý org_role (vai trò cấp tổ chức) + gán permission. `dataScope` ∈ `ORGANIZATION | BRANCH | SELF`.

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

## A2. List org_role theo organization
- **GET** `/api/v1/erp/org-roles?organizationId={organizationId}`
- Query: `organizationId` (bắt buộc) — org mà owner sở hữu.
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
  "organizationId": "org-uuid",
  "roleName": "Cashier",
  "dataScope": "BRANCH",
  "permissionIds": ["perm-uuid-1", "perm-uuid-2"]
}
```
  - `organizationId` (bắt buộc), `roleName` (bắt buộc, 1–50 ký tự, **unique trong org**), `dataScope` (bắt buộc), `permissionIds` (mảng, có thể rỗng `[]`).
- Response 201: object `OrgRole` (như A2).
- Lỗi: `ORG_ROLE_ORGANIZATION_REQUIRED`, `ORG_ROLE_NAME_REQUIRED`, `ORG_ROLE_DATA_SCOPE_REQUIRED`, `ORG_ROLE_NAME_EXISTS`, `ORG_PERMISSION_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

## A5. Sửa org_role (đổi tên/scope + remap permission)
- **PUT** `/api/v1/erp/org-roles/{id}`
- Request body (không đổi organization):
```json
{
  "roleName": "Senior Cashier",
  "dataScope": "BRANCH",
  "permissionIds": ["perm-uuid-1", "perm-uuid-3"]
}
```
- Response 200: object `OrgRole`.
- Lỗi: `ORG_ROLE_NOT_FOUND`, `ORG_ROLE_NAME_EXISTS`, `ORG_PERMISSION_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

> **Delete org_role**: chưa hỗ trợ (backend hoãn tới khi có soft-delete).

---

# B. ORG EMPLOYEE MANAGEMENT

Base path: `/api/v1/personal/branches`. Employee = 1 user account (1-1) + thông tin nhân sự. Authz: owner hoặc employee có permission ghi trong ngoặc.

### `EmployeeResponse` (trả ở mọi API employee bên dưới)
```json
{
  "id": "emp-uuid",
  "username": "cashier01",
  "email": "cashier01@shop.com",
  "phone": "0900000000",
  "branchId": "branch-uuid",
  "orgRoleName": "Cashier",
  "salary": 8000000,
  "status": "ACTIVE",
  "startDate": "2026-07-01",
  "profileUpdateEnabled": false
}
```
`status` ∈ `ACTIVE | INACTIVE | TERMINATED`.

## B1. Thêm employee (tạo user + employee) — permission `EMPLOYEE_ADD`
- **POST** `/api/v1/personal/branches/employees`
- Request body:
```json
{
  "username": "cashier01",
  "email": "cashier01@shop.com",
  "phone": "0900000000",
  "branchId": "branch-uuid",
  "orgRoleId": "role-uuid",
  "startDate": "2026-07-01",
  "salary": 8000000
}
```
  - `username`, `email`, `phone`, `branchId`, `startDate` (bắt buộc). `orgRoleId`, `salary` (optional). Mật khẩu account do backend đặt mặc định.
- Response 201: `EmployeeResponse`.
- Lỗi: `EMPLOYEE_USERNAME_REQUIRED`, `EMPLOYEE_EMAIL_REQUIRED`, `EMPLOYEE_EMAIL_INVALID`, `EMPLOYEE_PHONE_REQUIRED`, `EMPLOYEE_BRANCH_REQUIRED`, `EMPLOYEE_START_DATE_REQUIRED`, `USER_USERNAME_ALREADY_EXISTS`, `EMAIL_ALREADY_EXISTS`, `EMPLOYEE_ORG_ROLE_NOT_FOUND`, `ORGANIZATION_BRANCH_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

## B2. Cập nhật thông tin employee — permission `EMPLOYEE_UPDATE` (MỚI)
- **PUT** `/api/v1/personal/branches/employees/{id}`
- Request body (chỉ thông tin nhân sự; KHÔNG đổi role/salary/branch/account ở đây):
```json
{
  "email": "cashier01@shop.com",
  "phone": "0911111111",
  "status": "ACTIVE",
  "startDate": "2026-07-01",
  "endDate": null
}
```
  - `email`, `startDate` (bắt buộc). `phone`, `status`, `endDate` (optional).
- Response 200: `EmployeeResponse`.
- Lỗi: `EMPLOYEE_NOT_FOUND`, `EMPLOYEE_EMAIL_INVALID`, `AUTHZ_UNAUTHORIZED`.

## B3. Gán org_role cho employee — permission `EMPLOYEE_ROLE_ASSIGN`
- **PUT** `/api/v1/personal/branches/employees/{id}/role`
- Request body:
```json
{ "orgRoleId": "role-uuid" }
```
- Response 200: `EmployeeResponse`.
- Lỗi: `EMPLOYEE_NOT_FOUND`, `EMPLOYEE_ORG_ROLE_REQUIRED`, `EMPLOYEE_ORG_ROLE_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

## B4. Gỡ org_role của employee — permission `EMPLOYEE_ROLE_REVOKE`
- **DELETE** `/api/v1/personal/branches/employees/{id}/role`
- Request: (none)
- Response 200: `EmployeeResponse` (`orgRoleName` = null).
- Lỗi: `EMPLOYEE_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

## B5. Cấu hình lương — permission `EMPLOYEE_UPDATE`
- **PUT** `/api/v1/personal/branches/employees/{id}/salary`
- Request body:
```json
{ "salary": 9000000 }
```
- Response 200: `EmployeeResponse`.
- Lỗi: `EMPLOYEE_NOT_FOUND`, `EMPLOYEE_SALARY_INVALID`, `AUTHZ_UNAUTHORIZED`.

## B6. Bật/tắt quyền employee tự sửa profile — permission `EMPLOYEE_UPDATE`
- **PUT** `/api/v1/personal/branches/employees/{id}/profile-update-access`
- Request body:
```json
{ "enabled": true }
```
- Response 200: `EmployeeResponse` (`profileUpdateEnabled` cập nhật).
- Lỗi: `EMPLOYEE_NOT_FOUND`, `AUTHZ_UNAUTHORIZED`.

---

## Ghi chú cho FE

- **Chưa có API list/get employee** ở backend (chỉ có các API ghi ở trên + assign/revoke role). Nếu UI cần bảng danh sách employee, cần backend bổ sung `GET /employees?branchId=` — báo BE. Tạm thời FE thao tác theo `employeeId` đã biết.
- Tất cả `id` là UUID string.
- `salary` là number (BigDecimal) — gửi/nhận number.
- Ngày (`startDate`, `endDate`) format `YYYY-MM-DD`.
- Không gửi `branchId`/`employeeId của người đang thao tác` từ client cho API cần context — backend lấy từ token.
