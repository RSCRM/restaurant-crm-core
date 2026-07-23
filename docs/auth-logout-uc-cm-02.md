# Specification - Redis Token Blacklist & Logout

**Feature:** Logout với Redis Token Blacklist

**Module:** `modules/identity`

**Version:** 1.0

**Status:** Draft

---

# 1. Mục tiêu

Triển khai cơ chế Logout sử dụng Redis để thu hồi (revoke) JWT Access Token.

Sau khi người dùng logout:

- JWT sẽ được lưu vào Redis Blacklist.
- Mọi request sử dụng JWT đã logout sẽ bị từ chối.
- Token blacklist sẽ tự động bị xóa khi JWT hết hạn (TTL của Redis).

Mục tiêu:

- Không lưu session trên server.
- Không cần cron job dọn dữ liệu.
- Không thay đổi cấu trúc JWT hiện tại.
- Dễ mở rộng cho Refresh Token sau này.

---

# 2. Kiến trúc

```
Client
    │
    │ Authorization: Bearer xxx
    ▼
Spring Security Filter Chain
    │
    ▼
JwtAuthenticationFilter
    │
    ├── Parse JWT
    ├── Verify Signature
    ├── Check Redis Blacklist
    │      │
    │      ├── Token tồn tại
    │      │      ↓
    │      │   401 Unauthorized
    │      │
    │      └── Không tồn tại
    │
    ▼
Controller
    ▼
Service
```

Logout

```
Client
    │
POST /api/v1/auth/logout
    │
    ▼
IdentityController
    ▼
IdentityService.logout()
    ▼
Redis
(jwt:blacklist:{hash})
```

---

# 3. Package Structure

```
src/main/java
│
├── infrastructure
│   └── redis
│       ├── RedisConfig.java
│       ├── RedisConstants.java
│       ├── RedisKeyGenerator.java
│       └── RedisBlacklistRepository.java
│
├── modules
│   └── identity
│       ├── controller
│       ├── dto
│       ├── service
│       ├── repository
│       ├── mapper
│       └── ...
│
└── common
```

---

# 4. Dependencies

```
spring-boot-starter-data-redis
```

---

# 5. application.yml

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 3000
```

Nếu deploy production:

```yaml
spring:
  data:
    redis:
      host: redis
      port: 6379
      password: ********
      ssl: false
```

---

# 6. Redis Configuration

## RedisConfig

Trách nhiệm:

- Khởi tạo RedisConnectionFactory
- Khởi tạo StringRedisTemplate
- Không chứa business logic

Bean cần có:

```
StringRedisTemplate
```

Serializer

Key

```
StringRedisSerializer
```

Value

```
StringRedisSerializer
```

Không dùng Java Serialization.

---

# 7. Redis Key Convention

Không lưu trực tiếp JWT.

JWT

```
eyJhbGciOiJIUzI1NiJ9...
```

↓

SHA-256

↓

```
9F6A1A42B6D...
```

Redis Key

```
jwt:blacklist:9F6A1A42B6D...
```

Value

```
1
```

TTL

```
Remaining JWT Expiration
```

Ví dụ

```
jwt:blacklist:7A991D...

value = 1

ttl = 12m
```

Sau 12 phút Redis tự xóa.

---

# 8. RedisConstants

Tạo class chứa constant.

Ví dụ

```
BLACKLIST_PREFIX = "jwt:blacklist:"
```

Không hard-code prefix ở nhiều nơi.

---

# 9. RedisKeyGenerator

Trách nhiệm

Sinh Redis Key.

Input

```
JWT
```

Output

```
jwt:blacklist:{sha256(jwt)}
```

Không nơi nào khác tự nối string.

---

# 10. RedisBlacklistRepository

Trách nhiệm

Toàn bộ thao tác với Redis.

Các method

```
save(tokenHash, ttl)

exists(tokenHash)

delete(tokenHash)
```

Lưu ý

Không xử lý JWT.

Không verify JWT.

Không tính TTL.

Chỉ thao tác Redis.

---

# 11. JwtAuthenticationFilter

Thêm bước kiểm tra Blacklist.

Flow

```
Resolve JWT

↓

Verify Signature

↓

Verify Expiration

↓

Generate Token Hash

↓

Redis Exists ?

        │
        ├── YES
        │
        └── Throw Unauthorized
        │
        ▼
Set Authentication
```

Không truy cập Database.

Không gọi Controller.

---

# 12. Logout Flow

```
Client

↓

POST /logout

↓

Extract JWT

↓

Verify JWT

↓

Remaining Expiration

↓

SHA256(token)

↓

Redis Save(hash, ttl)

↓

200 OK
```

---

# 13. Logout Business Logic

Input

```
Bearer Token
```

Các bước

### Step 1

Lấy JWT.

---

### Step 2

Verify JWT.

Nếu invalid

```
401 Unauthorized
```

---

### Step 3

Tính thời gian còn sống.

Ví dụ

JWT

```
exp = 22:00
```

Current

```
21:40
```

TTL

```
20 phút
```

---

### Step 4

Hash JWT.

```
SHA-256
```

---

### Step 5

Lưu Redis.

```
key

jwt:blacklist:{hash}
```

value

```
1
```

ttl

```
20 phút
```

---

### Step 6

Return

```
200 OK
```

---

# 14. Authentication Flow

Mọi request cần Authentication.

```
Bearer Token

↓

Verify JWT

↓

SHA256(token)

↓

Redis Exists

↓

YES

↓

401 Unauthorized
```

Nếu

```
NO
```

↓

Authenticate.

---

# 15. Redis TTL Strategy

TTL luôn bằng

```
JWT Expiration
-
Current Time
```

Ví dụ

JWT

```
60 phút
```

Logout sau

```
15 phút
```

TTL

```
45 phút
```

Redis sẽ tự động xóa key sau 45 phút.

Không cần Scheduler.

Không cần Job.

---

# 16. HTTP API

## Logout

```
POST /api/v1/auth/logout
```

Authorization

```
Bearer <access-token>
```

Response

```
200 OK
```

Ví dụ

```json
{
    "message": "Logout successfully."
}
```

---

# 17. Error Handling

## Token Invalid

```
401 Unauthorized
```

Ví dụ

```json
{
    "code": "TOKEN_INVALID",
    "message": "Invalid access token."
}
```

---

## Token Expired

```
401 Unauthorized
```

Ví dụ

```json
{
    "code": "TOKEN_EXPIRED",
    "message": "Access token has expired."
}
```

---

## Token Revoked

```
401 Unauthorized
```

Ví dụ

```json
{
    "code": "TOKEN_REVOKED",
    "message": "Access token has been revoked."
}
```

---

# 18. Logging

Logout thành công

```
INFO

User 15 logout successfully.
```

Token revoked

```
WARN

Revoked token detected.
```

Không log JWT.

Không log Token Hash.

---

# 19. Security Considerations

- Không lưu nguyên JWT trong Redis.
- Luôn hash JWT trước khi lưu.
- Redis chỉ lưu hash.
- Redis sử dụng TTL để tự động cleanup.
- Không expose hash ra ngoài API.
- Không ghi JWT vào log.
- Redis chỉ đóng vai trò Blacklist Storage.

---

# 20. Testing Checklist

## Redis

- Redis kết nối thành công.
- Redis tự động reconnect.
- Có thể ghi key.
- Có thể đọc key.
- TTL hoạt động.

---

## Logout

- Logout thành công.
- Logout với token invalid trả 401.
- Logout với token expired trả 401.
- Logout nhiều lần với cùng token không gây lỗi (idempotent).

---

## Authentication

- Token hợp lệ → 200.
- Token revoked → 401.
- Token expired → 401.
- Token invalid → 401.

---

# 21. Future Enhancements

- Blacklist Refresh Token.
- Hỗ trợ Redis Cluster.
- Hỗ trợ Redis Sentinel.
- Thêm metric theo dõi số lượng token bị revoke.
- Thêm monitoring Redis thông qua Spring Boot Actuator và Micrometer.
- Hỗ trợ cơ chế token version nếu hệ thống cần revoke theo user hoặc theo organization.