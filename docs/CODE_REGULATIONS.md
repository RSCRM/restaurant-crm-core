# QUY ĐỊNH VÀ CHUẨN MỰC LẬP TRÌNH (CODE REGULATIONS)
## Dự án: Restaurant CRM Core (`restaurant-crm-core`)

---

## 1. GIỚI THIỆU & CÔNG NGHỆ CỐT LÕI (TECH STACK)

Tài liệu này quy định các chuẩn mực thiết kế, cấu trúc thư mục, quy tắc viết code, xử lý lỗi và bảo mật áp dụng thống nhất cho toàn bộ hệ thống backend **Restaurant CRM Core**. Tất cả lập trình viên khi tham gia phát triển dự án bắt buộc phải tuân thủ nghiêm ngặt các quy định này.

### Công nghệ sử dụng trong dự án:
- **Ngôn ngữ**: Java 21 (Tận dụng Virtual Threads `spring.threads.virtual.enabled=true`)
- **Framework chính**: Spring Boot 4.1.0 (Spring WebMVC, Spring Data JPA, Spring Security OAuth2 Resource Server, Spring Validation, Spring Cache, Spring Actuator)
- **Cơ sở dữ liệu**: PostgreSQL (Hibernate Dialect, Migration bằng Flyway)
- **Mapping & Code Generation**: MapStruct 1.6.3 + Lombok 1.18.32 (`lombok-mapstruct-binding` 0.2.0)
- **Bảo mật**: Spring Security OAuth2 Resource Server (JWT Nimbus JOSE JWT, BCrypt Password Encoder)
- **Tài liệu API**: Springdoc OpenAPI 2.8.0 (Swagger UI)
- **Quản lý cấu hình**: `spring-dotenv` (Load file `.env`)

---

## 2. KIẾN TRÚC HỆ THỐNG & CẤU TRÚC THƯ MỤC (MODULAR MONOLITH)

Hệ thống được thiết kế theo mô hình **Modular Monolith (Monolith phân tách module)**. Mọi mã nguồn nằm dưới package gốc: `com.restaurant.crm`.

```text
com.restaurant.crm
├── CrmApplication.java                 # Class khởi chạy Spring Boot Application
├── common                              # Chứa mã nguồn dùng chung cho toàn bộ hệ thống
│   ├── config                          # Cấu hình Spring (Security, CORS, OpenAPI, Jwt)
│   ├── constant                        # Hằng số toàn cục (ApiConstant, PaginationConstant, InitializerOrder, ...)
│   ├── dto                             # DTO chung (ApiResponse, PagingResponse, ErrorMessage, PagingRequest)
│   ├── entity                          # Entity cơ sở (BaseEntity)
│   ├── enums                           # Enum lỗi & trạng thái dùng chung (ErrorCode)
│   ├── exception                       # Xử lý ngoại lệ toàn cục (AppException, GlobalExceptionHandler)
│   ├── properties                      # Configuration Properties (@ConfigurationProperties)
│   └── utils                           # Utility dùng chung (PagingUtil)
└── modules                             # Các module nghiệp vụ tách biệt
    ├── identity                        # Module Quản lý Định danh (User, Role, Permission, Auth)
    ├── crm                             # Module Khách hàng & CRM (Customer Account, Feedback, Loyalty, Wallet)
    └── erp                             # Module Tích hợp ERP
```

### Cấu trúc chuẩn bên trong một Module nghiệp vụ (`com.restaurant.crm.modules.<module_name>`):
Mỗi module nghiệp vụ phải tuân thủ phân tầng package chuẩn như sau:

```text
com.restaurant.crm.modules.<module_name>
├── constants                           # Hằng số riêng của module
│   ├── <entity>/                       # Ví dụ: user/UserConstants.java, user/UserControllerConstants.java
│   └── ...
├── controller                          # REST Controllers
├── dto                                 # Request và Response DTOs
│   ├── request                         # DTO nhận từ Client
│   └── response                        # DTO trả về Client
├── entity                              # JPA Entities (Inherit BaseEntity)
├── enums                               # Enum nội bộ của module
├── initializer                         # Components khởi tạo dữ liệu ban đầu (ApplicationRunner)
├── mapper                              # MapStruct Mappers
├── repository                          # Spring Data JPA Repositories
├── service                             # Tách biệt Interface và Implementation
│   ├── interfaces                      # Service Interfaces
│   └── impl                            # Service Implementations
└── utils                               # Utility nội bộ module
```

> ⚠️ **LƯU Ý VỀ ĐẶT TÊN PACKAGE**: Package Java **TUYỆT ĐỐI KHÔNG** chứa dấu gạch ngang (`-`). Sử dụng snake_case hoặc camelCase nếu cần (Ví dụ: dùng `loyalty_voucher` thay vì `loyalty-voucher`).

---

## 3. QUY TẮC ĐẶT TÊN (NAMING CONVENTIONS)

### 3.1. Class và Interface
- **Controller**: `<Entity>Controller` (Ví dụ: `UserController`, `RoleController`, `AuthenticationController`).
- **Service Interface**: `<Entity>Service` (Ví dụ: `UserService`, `RoleService`).
- **Service Implementation**: `<Entity>ServiceImpl` (Ví dụ: `UserServiceImpl`, `RoleServiceImpl`).
- **Repository**: `<Entity>Repository` (Ví dụ: `UserRepository`, `RoleRepository`).
- **Request DTO**: `<Entity><Action>Request` (Ví dụ: `UserCreationRequest`, `UserRolesUpdateRequest`, `AuthenticationRequest`).
- **Response DTO**: `<Entity>Response` hoặc `<Action>Response` (Ví dụ: `UserResponse`, `RoleResponse`, `IntrospectResponse`).
- **JPA Entity**: Viết hoa chữ cái đầu (PascalCase), danh từ số ít (Ví dụ: `User`, `OrgRole`, `OrgPermission`).
- **Mapper**: `<Entity>Mapper` (Ví dụ: `UserMapper`, `RoleMapper`).
- **Initializer**: `<Domain>Initializer` (Ví dụ: `PermissionInitializer`, `RoleInitializer`, `AdminInitializer`).
- **Constant Class**: `<Domain>Constants`, `<Domain>ControllerConstants`, `<Domain>ErrorCodeConstants`. Phải khai báo `private` constructor để chống khởi tạo.

### 3.2. Database (Bảng & Cột)
- **Tên bảng**: Dùng danh từ số nhiều, chữ thường, phân cách bởi dấu gạch dưới `snake_case` (Ví dụ: `users`, `roles`, `orgPermissions`, `user_roles`).
- **Tên cột**: Chữ thường, `snake_case` (Ví dụ: `created_at`, `updated_at`, `created_by`, `updated_by`).

---

## 4. QUY TẮC SỬ DỤNG LOMBOK & DEPENDENCY INJECTION

### 4.1. Dependency Injection (Tiêm phụ thuộc)
- **KHÔNG SỬ DỤNG** `@Autowired` trên private fields (Field Injection).
- **BẮT BUỘC SỬ DỤNG** Constructor Injection kết hợp với Lombok:
  - Khai báo `@RequiredArgsConstructor` trên class.
  - Sử dụng `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)` để tự động chuyển mọi field thành `private final`.

```java
// ✅ ĐÚNG CHUẨN
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService; // Tự động final và được inject qua Constructor
}
```

### 4.2. Lombok Annotations chuẩn cho DTO và Entity
- **Cho DTO (Request/Response)**:
  ```java
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public class UserCreationRequest { ... }
  ```
- **Cho Entity**:
  ```java
  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @SuperBuilder
  @FieldDefaults(level = AccessLevel.PRIVATE)
  @Entity
  @Table(name = UserConstants.TABLE_USER)
  public class User extends BaseEntity { ... }
  ```

---

## 5. THIẾT KẾ RESTFUL API & CHUẨN HÓA RESPONSE

### 5.1. Base URL & Endpoints
- Định dạng API path: `/api/v1/<resource>` (Ví dụ: `/api/v1/users`, `/api/v1/roles`, `/api/v1/auth/token`).
- Sử dụng đúng HTTP Method:
  - `GET`: Lấy dữ liệu.
  - `POST`: Tạo mới tài nguyên hoặc thao tác đặc biệt (login, introspect).
  - `PUT`: Cập nhật tài nguyên (toàn bộ hoặc một phần).
  - `DELETE`: Xóa tài nguyên.

### 5.2. Format Phản hồi Chuẩn (`ApiResponse<T>`)
Tất cả các API **BẮT BUỘC** trả về `ResponseEntity<ApiResponse<T>>`. Dữ liệu phản hồi tuân theo cấu trúc:

```json
// Phản hồi thành công
{
  "success": true,
  "data": {
    "id": "c0a80101-8c4d-1a2b-9e3f-4a5b6c7d8e9f",
    "username": "admin"
  }
}

// Phản hồi thất bại / Lỗi
{
  "success": false,
  "errorMessage": {
    "errorCode": "USER_1000",
    "message": "User not found with given username"
  }
}
```

### 5.3. Phân trang Phân chuẩn (`PagingRequest` & `PagingResponse<T>`)
- Đối với danh sách có phân trang, trả về `ApiResponse<PagingResponse<T>>`.
- Nhận tham số qua Query Request (Page 1-indexed trong Request Client, nhưng Service tự động trừ `GlobalVariableConstant.PAGE_SIZE_INDEX` khi gọi Spring Data JPA):

```java
@GetMapping
public ResponseEntity<ApiResponse<PagingResponse<UserResponse>>> getUsers(
        @RequestParam(value = "page", required = false, defaultValue = "1") int page,
        @RequestParam(value = "size", required = false, defaultValue = "10") int size,
        @RequestParam(required = false, defaultValue = PaginationConstant.DESC) String direction,
        @RequestParam(required = false, defaultValue = "createdAt") String field
) { ... }
```

---

## 6. QUY TẮC THIẾT KẾ ENTITY & BẢO TỒN DỮ LIỆU (JPA & PERSISTENCE)

### 6.1. Thừa kế `BaseEntity`
Tất cả các JPA Entities trong hệ thống **BẮT BUỘC** kế thừa từ `com.restaurant.crm.common.entity.BaseEntity`.

```java
@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseEntity {
    @Version
    Long version; // Kỹ thuật Optimistic Locking (Khóa lạc quan)

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id; // Primary Key định dạng UUID String

    @CreatedBy
    @Column(updatable = false)
    String createdBy;

    String updatedBy;

    @Column(updatable = false)
    Instant createdAt;

    Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
```

### 6.2. Hằng số cho Entity Column & Constraints
Mỗi Module phải có các lớp Constants định nghĩa tên bảng, tên cột và độ dài dữ liệu để tránh hardcode trong Entity và Request DTO (Ví dụ: `UserConstants.java`).

---

## 7. XỬ LÝ LỖI TOÀN CỤC & EXCEPTION HANDLING

### 7.1. Định nghĩa Mã lỗi (`ErrorCode`)
Mọi lỗi trong hệ thống phải được định nghĩa tập trung tại enum `com.restaurant.crm.common.enums.ErrorCode` với đầy đủ:
- `code` (String): Mã lỗi tiền tố theo domain (Ví dụ: `AUTH_1000`, `USER_1000`, `ROLE_1000`, `FILE_1000`).
- `message` (String): Thông điệp mô tả lỗi.
- `httpStatusCode` (HttpStatusCode): HTTP Status tương ứng (`HttpStatus.BAD_REQUEST`, `HttpStatus.NOT_FOUND`, `HttpStatus.UNAUTHORIZED`, v.v.).

### 7.2. Quăng lỗi Nghiệp vụ (`AppException`)
Khi xử lý logic nghiệp vụ trong Service, nếu phát hiện lỗi thì quăng ngoại lệ `AppException`:

```java
User user = usersRepository.findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
```

### 7.3. Validation lỗi Request DTO
Trong Request DTO, sử dụng Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Email`) và truyền tên Enum của `ErrorCode` vào thuộc tính `message`. `GlobalExceptionHandler` sẽ tự động chuyển đổi thông điệp này thành `ErrorCode` chuẩn:

```java
@Size(min = UserConstants.MIN_CHARS_USERNAME, 
      max = UserConstants.MAX_CHARS_USERNAME, 
      message = UserErrorCodeConstants.USER_USERNAME_INVALID)
String username;
```

---

## 8. MAPSTRUCT MAPPER

- Tất cả Mapper phải khai báo dưới dạng interface và có annotation `@Mapper(componentModel = "spring")`.
- Cấu hình Maven Compiler Plugin đã thiết lập tự động kết hợp MapStruct và Lombok (`lombok-mapstruct-binding`).
- Không thực hiện convert thủ công giữa Entity và DTO trong Service nếu đã có Mapper.

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
}
```

---

## 9. BẢO MẬT & PHÂN QUYỀN (SECURITY & JWT)

### 9.1. Xác thực (Authentication)
- Sử dụng Spring Security với **OAuth2 Resource Server** dựa trên JSON Web Token (JWT).
- Thuật toán ký JWT: `HS512` với secret key lấy từ `security.jwt.signer-key`.
- Custom `JwtAuthenticationEntryPoint` trả về `ApiResponse` chuẩn định dạng HTTP 401 khi Client không gửi Token hoặc Token không hợp lệ.

### 9.2. Lấy thông tin User hiện tại (`AuthUtils`)
Sử dụng `com.restaurant.crm.modules.identity.utils.AuthUtils` để truy vấn thông tin User từ SecurityContext:
- `AuthUtils.getCurrentUserId()`: Lấy ID người dùng đang đăng nhập từ JWT claim (`userId`).
- `AuthUtils.getCurrentUserName()`: Lấy username từ Authentication context.

### 9.3. Phân quyền (Authorization)
- Sử dụng `@EnableMethodSecurity` cho phép phân quyền trực tiếp trên Method Service/Controller bằng `@PreAuthorize("hasAuthority('...')")` hoặc `@PreAuthorize("hasRole('...')")`.
- Các endpoint công khai (WhiteList) như Auth, Swagger UI được khai báo tập trung tại `SecurityConfig.java`.

---

## 10. KHỞI TẠO DỮ LIỆU BAN ĐẦU (DATA INITIALIZERS)

- Các dữ liệu mặc định của hệ thống (Permissions, Roles, Admin User) được khởi tạo tự động khi ứng dụng start bằng cách implement `ApplicationRunner`.
- **Thứ tự thực thi (Order Execution)**: Phải gắn annotation `@Order(InitializerOrder.<DOMAIN>)` để đảm bảo thứ tự phụ thuộc dữ liệu:
  1. `PermissionInitializer` (`Order(1)`): Khởi tạo danh sách quyền hệ thống.
  2. `RoleInitializer` (`Order(2)`): Khởi tạo vai trò (ADMIN, USER) và gán quyền tương ứng.
  3. `AdminInitializer` (`Order(3)`): Khởi tạo tài khoản Quản trị viên tối cao (Super Admin).

---

## 11. CẤU HÌNH NGHỆ MÔI TRƯỜNG (.ENV & APPLICATION.PROPERTIES)

- **Không bao giờ commit thông tin nhạy cảm** (DB Password, Secret Key, Cloudinary Credentials) lên Git.
- Mọi biến môi trường được khai báo mẫu trong `env.example`.
- Lập trình viên tạo file `.env` ở thư mục gốc của dự án để ứng dụng tự động nạp qua `spring-dotenv`.
- Trong `application.properties`, sử dụng cú pháp `${VAR_NAME:default_value}` để cung cấp giá trị mặc định khi chạy.

---

## 12. DANH SÁCH "NÊN LÀM" (DO'S) VÀ "KHÔNG NÊN LÀM" (DON'TS)

### ✅ NÊN LÀM (DO'S):
1. **Luôn Kế thừa `BaseEntity`** cho mọi JPA Entity mới.
2. **Luôn Dùng Interface-Implementation** cho tầng Service (`service/interfaces` và `service/impl`).
3. **Luôn Bọc API Output** bằng `ResponseEntity<ApiResponse<T>>`.
4. **Sử dụng MapStruct** cho mọi thao tác chuyển đổi Entity <-> DTO.
5. **Dùng Constant Classes** riêng cho từng Entity/Module để quản lý tên cột, tên bảng và thông báo lỗi.
6. **Khởi tạo dữ liệu hệ thống** qua `ApplicationRunner` có `@Order` rõ ràng.
7. **Sử dụng `@Transactional`** ở tầng Service đối với các phương thức ghi/sửa dữ liệu Database.

### ❌ KHÔNG NÊN LÀM (DON'TS):
1. **KHÔNG** sử dụng `@Autowired` trên private field.
2. **KHÔNG** gán dấu gạch ngang (`-`) trong tên package Java.
3. **KHÔNG** trả về Entity trực tiếp ra Controller/Client (Luôn chuyển đổi sang Response DTO).
4. **KHÔNG** hardcode chuỗi mã lỗi hoặc thông điệp lỗi rải rác trong code (Tất cả phải qua `ErrorCode`).
5. **KHÔNG** tự tạo format Response ngẫu nhiên ngoài chuẩn `ApiResponse<T>`.
6. **KHÔNG** lưu mật khẩu chưa mã hóa vào Database (Bắt buộc dùng `PasswordEncoder.encode()`).
