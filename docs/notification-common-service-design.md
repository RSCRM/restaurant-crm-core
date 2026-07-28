# THIẾT KẾ: COMMON NOTIFICATION SERVICE
## Dự án: Restaurant CRM Core (`restaurant-crm-core`)

> Mục tiêu: một hạ tầng thông báo **dùng chung** (`common`), chịu tải cao, hỗ trợ 4 phạm vi gửi
> (toàn hệ thống / chi nhánh / nhóm / cá nhân), có **permission matrix** rõ ràng và **cách ly
> multi-tenant** nhiều lớp.
>
> Tuân thủ `docs/CODE_REGULATIONS.md`: modular monolith, tách `service/interfaces` + `service/impl`,
> `BaseEntity`, `ApiResponse<T>`, MapStruct, Constants class, và quy tắc DO #8 — **tái sử dụng hạ tầng
> `common.sse`**, không để module khác phụ thuộc chéo vào module notification.

---

## 0. HIỆN TRẠNG & KHOẢNG CÁCH (GAP ANALYSIS)

| Điểm | Hiện trạng (`modules/erp/notification`, `common/sse`) | Hệ quả khi tải cao / nhiều tenant |
|---|---|---|
| Tenant | `Notification` chỉ có `branchId`; controller nhận `branchId` từ **query param**, không đối chiếu JWT | **IDOR**: nhân viên org A subscribe/đọc lịch sử của branch org B |
| Trạng thái đọc | `status` nằm trên chính dòng notification | Broadcast: 1 người đọc → cả chi nhánh thành `READ` |
| Registry SSE | `Map<String, List<SseConnection>>` với `CopyOnWriteArrayList` | `remove` O(n) + copy toàn mảng; key rỗng không bao giờ bị xoá → rò rỉ bộ nhớ |
| Fan-out | `emitter.send()` **đồng bộ, trong vòng lặp**, trên thread của caller | 1 client chậm chặn toàn bộ broadcast (head-of-line blocking) |
| Đa node | Registry nằm trong RAM của 1 JVM | Scale ngang: client nối node 1 không nhận sự kiện phát từ node 2 |
| Backpressure | Không có hàng đợi, không giới hạn | Client chậm → thread bị treo, không có cơ chế bỏ bớt |
| Heartbeat | Không có | Proxy/LB cắt kết nối idle, server không biết socket đã chết |
| Phạm vi gửi | Chỉ có `recipientId = null` (branch) hoặc có giá trị (cá nhân) | Thiếu SYSTEM và GROUP |
| Phân quyền | `subscribe` gate bằng `ORDER_READ` | Sai ngữ nghĩa: mọi nhân viên đều cần nhận thông báo; lọc quyền phải theo **từng sự kiện** |
| Migration | `spring.flyway.enabled=true` nhưng **pom.xml không có dependency Flyway**, không có `db/migration` | Flyway không chạy; schema đang do `ddl-auto=update` sinh ra |

Thiết kế dưới đây giải quyết toàn bộ các mục trên.

---

## 1. TỔNG QUAN KIẾN TRÚC

```text
   [Business modules: order, table, booking, attendance, invoice, ...]
                    │  NotificationPublisher.publish(cmd)      (API nội bộ, không phụ thuộc chéo)
                    ▼
   ┌──────────────────────────────────────────────────────────────┐
   │  common.notification  (WRITE PATH)                           │
   │  1. NotificationAccessGuard  → kiểm tra quyền gửi + tenant   │
   │  2. NotificationRepository   → 1 INSERT duy nhất (dedupe_key)│
   │  3. @TransactionalEventListener(AFTER_COMMIT)                │
   └───────────────────────┬──────────────────────────────────────┘
                           ▼  publish envelope (nhỏ, ~200B)
   ┌──────────────────────────────────────────────────────────────┐
   │  Redis Pub/Sub   channel: notif:org:{orgId} | notif:system   │
   └───────┬──────────────────────────────┬───────────────────────┘
           ▼ node-1                       ▼ node-2
   ┌───────────────────────┐      ┌───────────────────────┐
   │ SseConnectionRegistry │      │ SseConnectionRegistry │   (common.sse)
   │  - index theo tenant  │      │                       │
   │  - index theo nhân sự │      │  mỗi connection:      │
   │  - NotificationAudience│     │  queue bounded + 1     │
   │    .matches(ctx)      │      │  virtual thread drain │
   └───────────────────────┘      └───────────────────────┘
           ▼ SSE                          ▼ SSE
        clients                        clients

   READ PATH (nguồn sự thật = PostgreSQL):
   GET /api/v1/notifications  → cursor pagination + LEFT JOIN receipts
   GET .../unread-count       → Redis counter (fallback: COUNT có điều kiện)
```

**Nguyên tắc nền:** PostgreSQL là **nguồn sự thật**, SSE chỉ là kênh đẩy **best-effort**. Mất một
sự kiện realtime không bao giờ được phép làm mất dữ liệu — client reconnect luôn đồng bộ lại qua REST.
Nguyên tắc này cho phép write path không cần transaction phân tán, không cần message broker bền vững.

---

## 2. MÔ HÌNH PHẠM VI GỬI (4 SCOPES)

```java
public enum NotificationScope {
    SYSTEM,   // Toàn hệ thống — vượt tenant (bảo trì, thay đổi chính sách license)
    BRANCH,   // Toàn bộ nhân sự 1 chi nhánh
    GROUP,    // 1 nhóm trong chi nhánh (theo orgRole hoặc theo permission)
    DIRECT    // 1 nhân viên cụ thể
}
```

| Scope | `organization_id` | `branch_id` | `target_key` | `recipient_id` | Ví dụ |
|---|---|---|---|---|---|
| `SYSTEM` | NULL | NULL | NULL | NULL | "Hệ thống bảo trì 02:00–03:00" |
| `BRANCH` | bắt buộc | bắt buộc | NULL | NULL | "Chi nhánh đóng cửa sớm hôm nay" |
| `GROUP` | bắt buộc | bắt buộc | bắt buộc | NULL | `READY_TO_SERVE` → nhóm phục vụ |
| `DIRECT` | bắt buộc | bắt buộc | NULL | bắt buộc | "Lịch làm việc của bạn đã đổi" |

`GROUP` có 2 kiểu định nghĩa nhóm, phân biệt bằng `group_type`:

```java
public enum NotificationGroupType {
    BY_ROLE,        // target_key = OrgRole.roleName   (ví dụ: "WAITER", "CHEF")
    BY_PERMISSION   // target_key = OrgPermission name  (ví dụ: "ORDER_READ")
}
```

`BY_PERMISSION` là kiểu nên dùng mặc định cho thông báo hệ thống sinh ra: nó bám theo **năng lực**
chứ không bám theo tên vai trò, nên không vỡ khi khách hàng tự định nghĩa lại vai trò trong tổ chức
của họ (`OrgRole` là dữ liệu người dùng tạo, không phải hằng số hệ thống).

> **Lưu ý về `SYSTEM`**: đây là scope **duy nhất** được phép vượt biên tenant. Vì vậy nó phải bị
> khoá chặt nhất (xem §4) và mọi bản ghi `SYSTEM` phải có `sender_type = SYSTEM` hoặc do
> super-admin ở tầng `identity` phát hành, không bao giờ do context token của một tổ chức phát ra.

---

## 3. MÔ HÌNH DỮ LIỆU

### 3.1. Quyết định then chốt: fan-out-on-read cho lưu trữ

Có 2 lựa chọn kinh điển:

| | Fan-out-on-write (mỗi người nhận 1 dòng) | **Fan-out-on-read (1 dòng/thông báo)** ✅ |
|---|---|---|
| Ghi 1 broadcast cho chi nhánh 50 người | 50 INSERT | **1 INSERT** |
| Dung lượng | O(người nhận) | O(thông báo) |
| Đọc feed | SELECT đơn giản theo `recipient_id` | SELECT + predicate scope + LEFT JOIN receipts |
| Đếm chưa đọc | COUNT đơn giản | Cần counter cache |

Nghiệp vụ nhà hàng **thiên về broadcast** (món xong, bàn mở, ca làm việc) và số người nhận mỗi chi
nhánh nhỏ (chục người), nên **fan-out-on-read** thắng rõ rệt: write path O(1), không có "write
amplification" khi bếp bắn 200 sự kiện/giờ. Chi phí đọc được xử lý bằng index phù hợp + cursor
pagination + Redis counter (§6).

Trạng thái đọc tách sang bảng `notification_receipts` **thưa** — chỉ sinh dòng khi người dùng thực sự
đọc/ẩn. Đây cũng là thứ sửa lỗi "một người đọc, cả chi nhánh thành đã đọc" của thiết kế hiện tại.

### 3.2. Entity `Notification` (kế thừa `BaseEntity`)

```java
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = NotificationConstants.TABLE_NOTIFICATION)
public class Notification extends BaseEntity {

    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_SCOPE, nullable = false, length = 20)
    NotificationScope scope;

    /** NULL chỉ khi scope = SYSTEM. Cột dẫn đầu mọi index & mọi truy vấn. */
    @Column(name = NotificationConstants.COL_ORGANIZATION_ID)
    String organizationId;

    /** NULL khi scope = SYSTEM. */
    @Column(name = NotificationConstants.COL_BRANCH_ID)
    String branchId;

    /** Chỉ dùng cho scope = GROUP: roleName hoặc permission name. */
    @Column(name = NotificationConstants.COL_TARGET_KEY, length = 64)
    String targetKey;

    @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_GROUP_TYPE, length = 20)
    NotificationGroupType groupType;

    /** Chỉ dùng cho scope = DIRECT. */
    @Column(name = NotificationConstants.COL_RECIPIENT_ID)
    String recipientId;

    /** employeeId người gửi, hoặc NULL nếu sender_type = SYSTEM. */
    @Column(name = NotificationConstants.COL_SENDER_ID)
    String senderId;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_SENDER_TYPE, nullable = false, length = 20)
    NotificationSenderType senderType;

    @NotBlank @Size(max = NotificationConstants.MAX_CHARS_TITLE)
    @Column(name = NotificationConstants.COL_TITLE, nullable = false)
    String title;

    @NotBlank @Size(max = NotificationConstants.MAX_CHARS_CONTENT)
    @Column(name = NotificationConstants.COL_CONTENT, nullable = false)
    String content;

    @NotNull @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_TYPE, nullable = false, length = 40)
    NotificationType type;

    /**
     * Quyền tối thiểu để NHÌN THẤY thông báo này. NULL = mọi nhân sự trong phạm vi đều thấy.
     * Được chốt cứng tại thời điểm ghi (snapshot) để đọc và đẩy luôn nhất quán.
     */
    @Column(name = NotificationConstants.COL_REQUIRED_PERMISSION, length = 64)
    String requiredPermission;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_PRIORITY, nullable = false, length = 20)
    NotificationPriority priority = NotificationPriority.NORMAL;

    /** Deep-link / tham chiếu entity (orderId, tableId...) dạng JSON. */
    @Column(name = NotificationConstants.COL_PAYLOAD, columnDefinition = "jsonb")
    String payload;

    /** Hết hạn → không hiển thị, và là mốc cho job dọn dẹp. NULL = không hết hạn. */
    @Column(name = NotificationConstants.COL_EXPIRES_AT)
    Instant expiresAt;

    /** Khoá chống trùng (idempotency) cho nguồn phát at-least-once. */
    @Column(name = NotificationConstants.COL_DEDUPE_KEY, length = 128)
    String dedupeKey;
}
```

> Bỏ hẳn trường `status` khỏi `Notification` — trạng thái đọc là **thuộc tính của cặp
> (thông báo, người nhận)**, không phải của thông báo.

### 3.3. Entity `NotificationReceipt` (bảng thưa)

```java
@Entity
@Table(name = NotificationConstants.TABLE_NOTIFICATION_RECEIPT)
@IdClass(NotificationReceiptId.class)   // (notificationId, recipientId)
public class NotificationReceipt {
    @Id String notificationId;
    @Id String recipientId;
    @Enumerated(EnumType.STRING) ReceiptStatus status;  // READ | DISMISSED
    Instant readAt;
}
```

Không kế thừa `BaseEntity` — đây là bảng liên kết khối lượng lớn, không cần `version`/UUID/audit;
khoá chính tổ hợp là đủ và tiết kiệm hơn nhiều. (Đây là ngoại lệ có chủ đích so với quy tắc §6.1
của `CODE_REGULATIONS.md`; cần ghi chú lại trong PR để reviewer không hiểu nhầm là sai chuẩn.)

### 3.4. Index (bắt buộc — đây là thứ quyết định hiệu năng đọc)

```sql
-- Feed chi nhánh + nhóm (truy vấn nóng nhất)
CREATE INDEX idx_notif_branch_feed
    ON notifications (organization_id, branch_id, created_at DESC)
    WHERE scope IN ('BRANCH', 'GROUP');

-- Feed cá nhân
CREATE INDEX idx_notif_direct_feed
    ON notifications (organization_id, recipient_id, created_at DESC)
    WHERE recipient_id IS NOT NULL;

-- Thông báo toàn hệ thống (số lượng rất nhỏ)
CREATE INDEX idx_notif_system_feed
    ON notifications (created_at DESC)
    WHERE scope = 'SYSTEM';

-- Chống trùng khi nguồn phát gửi lại
CREATE UNIQUE INDEX uq_notif_dedupe
    ON notifications (organization_id, dedupe_key)
    WHERE dedupe_key IS NOT NULL;

-- Tra trạng thái đọc theo người dùng
CREATE INDEX idx_receipt_recipient
    ON notification_receipts (recipient_id, notification_id);
```

Partial index (`WHERE ...`) giữ index nhỏ và loại luôn các dòng không liên quan khỏi cây B-tree —
quan trọng khi bảng lớn dần. Khi vượt ~50–100 triệu dòng: **partition theo tháng trên `created_at`**,
kèm job `DROP PARTITION` thay cho `DELETE` (mục §9).

> ⚠️ Migration: `application.properties` bật Flyway nhưng `pom.xml` **chưa có** `flyway-core` +
> `flyway-database-postgresql`, và không có thư mục `db/migration` → schema hiện đang do
> `ddl-auto=update` sinh. Các index partial ở trên **không thể** sinh bằng `ddl-auto`. Nên bổ sung
> dependency Flyway và đặt file `V{n}__notification_core.sql`; nếu chưa muốn, phải tạo index thủ công.

---

## 4. PERMISSION MATRIX

Tách bạch **hai** ma trận khác nhau — nhầm lẫn giữa chúng là nguồn gốc phổ biến nhất của lỗ hổng.

### 4.1. Ma trận QUYỀN GỬI (ai được phát tới scope nào)

Áp dụng **chỉ cho luồng do người dùng khởi tạo** (REST `POST /api/v1/notifications`).
Thông báo do hệ thống sinh (ví dụ `READY_TO_SERVE` từ module order) đi bằng API nội bộ
`NotificationPublisher` và **không** qua ma trận này — nhưng vẫn bắt buộc mang tenant context.

| Scope | Authority yêu cầu | Ràng buộc bổ sung (bắt buộc, kiểm tra trong `NotificationAccessGuard`) |
|---|---|---|
| `SYSTEM` | `ROLE_SUPER_ADMIN` (identity token, claim `scope`) | Token **không được** có `organizationId`; ghi audit log riêng |
| `BRANCH` | `NOTIFICATION_BROADCAST` **hoặc** `BRANCH_MANAGE` | `target.orgId == token.orgId` **và** người gửi có quyền trên chính branch đó |
| `GROUP` | `NOTIFICATION_BROADCAST` **hoặc** `STAFF_MANAGE` | Như `BRANCH`; `targetKey` phải tồn tại trong org (role) hoặc là permission hợp lệ |
| `DIRECT` | `NOTIFICATION_SEND` | Người nhận phải cùng `organizationId`; nếu khác branch → cần `ORG_MANAGE` |

Permission mới cần bổ sung vào `StartDefinedOrgPermission` + `OrgPermissionInitializer`:

```java
// Notification Permissions
public static final String NOTIFICATION_SEND      = "NOTIFICATION_SEND";      // gửi 1-1
public static final String NOTIFICATION_BROADCAST = "NOTIFICATION_BROADCAST"; // gửi branch/group
```

### 4.2. Ma trận QUYỀN NHẬN (ai nhìn thấy thông báo nào)

Một vị từ (predicate) **duy nhất**, áp dụng ở **cả hai** nơi: khi đẩy SSE và khi truy vấn lịch sử.

```
visible(u, n) :=
      tenantMatch(u, n)                                  // §5, lớp 1
  AND scopeMatch(u, n)
  AND (n.requiredPermission == null || u.permissions.contains(n.requiredPermission))
  AND (n.expiresAt == null || n.expiresAt > now)

scopeMatch(u, n) :=
  n.scope == SYSTEM  → true
  n.scope == BRANCH  → u.branchId == n.branchId
  n.scope == GROUP   → n.groupType == BY_ROLE       ? u.orgRole == n.targetKey
                                                    : u.permissions.contains(n.targetKey)
  n.scope == DIRECT  → u.employeeId == n.recipientId
```

| Scope \ Người dùng | Cùng branch, đủ quyền | Cùng branch, thiếu `requiredPermission` | Khác branch, cùng org | Khác org | Customer session token |
|---|---|---|---|---|---|
| `SYSTEM` | ✅ | ✅ (nếu `requiredPermission` = NULL) | ✅ | ✅ | ❌ |
| `BRANCH` | ✅ | ❌ | ❌ | ❌ | ❌ |
| `GROUP` | ✅ nếu khớp nhóm | ❌ | ❌ | ❌ | ❌ |
| `DIRECT` | ✅ nếu là chính chủ | ✅ (chính chủ luôn thấy) | ✅ (chính chủ) | ❌ | ❌ |

> `DIRECT` gửi cho chính chủ **bỏ qua** `requiredPermission`: nếu ai đó đã chủ đích gửi riêng cho bạn
> thì quyền chức năng không nên chặn bạn đọc nó.
>
> `ROLE_CUSTOMER_SESSION` (token QR khách hàng, `SecurityConfig`) **không bao giờ** vào được kênh
> nhân sự. Thông báo cho khách hàng dùng kênh SSE riêng theo `sessionId`, không dùng service này.

### 4.3. Chống lệch giữa hai nơi kiểm tra

Vị từ trên phải tồn tại **một lần** trong code, dưới hai hình thái sinh đôi:

```java
public final class NotificationAudience {

    /** Dùng cho đẩy SSE — chạy trên envelope trong RAM, không chạm DB. */
    public static boolean matches(NotificationEnvelope n, RecipientContext u) { ... }

    /** Dùng cho truy vấn lịch sử — sinh cùng một logic dưới dạng JPA Specification. */
    public static Specification<Notification> specFor(RecipientContext u) { ... }
}
```

Bắt buộc có **contract test** sinh ma trận tổ hợp (scope × groupType × role × permission × tenant),
chạy song song `matches()` và `specFor()` trên cùng tập dữ liệu và **assert kết quả trùng khít**.
Đây là bài test chống rò rỉ dữ liệu quan trọng nhất của toàn bộ thiết kế — thiếu nó, hai nhánh code
sẽ lệch nhau sau vài lần sửa và lỗ hổng sẽ chỉ xuất hiện ở một trong hai đường.

### 4.4. Endpoint `subscribe` **không** gate bằng permission nghiệp vụ

Hiện tại `@PreAuthorize(... ORDER_READ)` trên `/subscribe` là sai ngữ nghĩa: mọi nhân sự đều cần
kênh thông báo. Việc lọc đã nằm ở **từng sự kiện** qua `requiredPermission`. Chỉ cần
`isAuthenticated()` + token type là `CONTEXT`.

---

## 5. CÁCH LY MULTI-TENANT — 5 LỚP PHÒNG THỦ

Mỗi lớp độc lập; một lớp thủng vẫn còn 4 lớp chặn.

**Lớp 1 — Tenant lấy từ token, không lấy từ request.**
Đây là lớp sửa lỗ hổng hiện tại. `organizationId` / `branchId` / `employeeId` **luôn** đọc từ JWT
claim qua `AuthUtils`. Endpoint không nhận `branchId` làm query param nữa.

```java
public record RecipientContext(
        String organizationId, String branchId, String employeeId,
        String orgRole, Set<String> permissions) {

    public static RecipientContext fromSecurityContext() { /* AuthUtils + authorities */ }
}
```

Trường hợp quản lý đa chi nhánh cần xem chi nhánh khác: cho phép truyền `branchId`, nhưng **phải**
đi qua guard xác thực quyền sở hữu (theo mẫu `EmployeeAccessChecker` đã có sẵn trong module
organization), và branch đó phải thuộc `token.organizationId`.

**Lớp 2 — Dữ liệu.** `organization_id` là cột dẫn đầu của mọi index và mọi mệnh đề WHERE. Không có
truy vấn nào chỉ lọc theo `branch_id`.

**Lớp 3 — Repository.** Mọi method đọc **bắt buộc** nhận `organizationId`. Cấm dùng `findById` trần
trong service; đọc một thông báo cụ thể phải qua
`findByIdAndOrganizationId(...)`, và khi không khớp thì trả **404 (NOT_FOUND)**, không phải 403 —
403 vô tình xác nhận "tài nguyên này có tồn tại ở tenant khác".

**Lớp 4 — Transport.** Redis channel tách theo tổ chức: `notif:org:{organizationId}`, riêng
`notif:system` cho scope SYSTEM. Một sự kiện của org A **không đi vào** đường truyền mà node đang
lắng nghe cho org B — cách ly ở tầng vật lý, không phụ thuộc vào việc code lọc đúng hay sai.

**Lớp 5 — Registry.** Khoá của registry là `tenantKey = organizationId + ':' + branchId`. Kể cả khi
`branchId` bị trùng hoặc bị đoán đúng giữa hai tổ chức, tra cứu vẫn không thể va nhau.

**Bổ sung:** scope `SYSTEM` bị từ chối nếu token mang `organizationId`; mọi lần phát `SYSTEM` hoặc
`BRANCH` ghi audit (`sender_id`, `created_by` đã có sẵn trong `BaseEntity`).

---

## 6. HIỆU NĂNG & ĐỒNG THỜI CAO

### 6.1. Registry SSE viết lại (trái tim của phần chịu tải)

```java
public final class SseConnectionRegistry {

    // tenantKey -> connId -> connection   (nested CHM: remove O(1), không copy mảng)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, SseConnection>> byTenant
            = new ConcurrentHashMap<>();

    // employeeKey (org:employeeId) -> connId -> connection  (DIRECT: O(1), không quét cả chi nhánh)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, SseConnection>> byEmployee
            = new ConcurrentHashMap<>();
}
```

Hai thay đổi cấu trúc quan trọng so với bản hiện tại:

- **`ConcurrentHashMap` lồng nhau thay cho `CopyOnWriteArrayList`.** COW copy toàn bộ mảng mỗi lần
  thêm/xoá. Ở chi nhánh 50 kết nối, giờ cao điểm nhân viên vào/ra liên tục → O(n) copy mỗi thao tác.
  CHM lồng nhau cho add/remove O(1).
- **Index phụ theo `employeeId`.** Thiết kế hiện tại muốn gửi `DIRECT` phải quét toàn bộ danh sách
  chi nhánh. Với index phụ, gửi 1-1 là tra khoá trực tiếp.

Dọn khoá rỗng bằng `compute` (nếu inner map rỗng sau khi xoá thì trả `null` để CHM gỡ luôn entry) —
tránh rò rỉ khoá tenant tích tụ theo thời gian.

### 6.2. Backpressure: hàng đợi có giới hạn + 1 virtual thread mỗi kết nối

Đây là thay đổi quan trọng nhất về mặt chịu tải. Không bao giờ gọi `emitter.send()` trên thread của
người phát.

```java
final class SseConnection {
    private final String connId, organizationId, branchId, employeeId, orgRole;
    private final Set<String> permissions;          // immutable snapshot
    private final SseEmitter emitter;
    private final ArrayBlockingQueue<NotificationEnvelope> queue;  // mặc định 32
    private final AtomicBoolean stale = new AtomicBoolean(false);
    private final Instant connectedAt;

    void offer(NotificationEnvelope e) {
        if (!queue.offer(e)) {                 // client chậm → hàng đợi đầy
            queue.poll();                      // bỏ bản tin cũ nhất
            queue.offer(e);
            stale.set(true);                   // đánh dấu: client đã mất sự kiện
        }
    }
    // 1 virtual thread duy nhất drain queue → đảm bảo send() được tuần tự hoá cho mỗi kết nối
    // (SseEmitter KHÔNG thread-safe khi nhiều thread cùng send).
}
```

Khi `stale == true`, thay vì cố đẩy bù, server gửi một sự kiện `RESYNC`; client gọi lại REST để lấy
feed chuẩn. Lý do: DB là nguồn sự thật, nên "bảo client đồng bộ lại" luôn rẻ và đúng hơn là giữ
buffer lớn trong RAM.

**Tính dung lượng bộ nhớ** (con số để chọn tham số, không phải phỏng đoán):

```
200 chi nhánh × 20 nhân sự = 4.000 kết nối đồng thời
envelope ~200 B (chỉ id + metadata, không chứa content dài)
queue 32 phần tử  → 4.000 × 32 × 200 B ≈ 25 MB  (worst case, tất cả queue đầy)
queue 256 phần tử → 4.000 × 256 × 200 B ≈ 205 MB  ❌ quá tốn
```

→ Chọn **32** làm mặc định, đưa vào `@ConfigurationProperties` để chỉnh theo môi trường.

`spring.threads.virtual.enabled=true` đã bật sẵn: 4.000 virtual thread drain là chi phí không đáng
kể (khác hoàn toàn platform thread — 4.000 × 1MB stack là bất khả thi). Đây chính là lý do thiết kế
"1 thread mỗi kết nối" khả thi trên Java 21.

### 6.3. Heartbeat & vòng đời kết nối

- Heartbeat mỗi 15s: gửi SSE comment (`:ping`) — gần như miễn phí, giúp phát hiện socket chết và
  giữ kết nối qua LB/proxy (thường timeout 30–60s).
- `SSE_TIMEOUT` 30 phút giữ nguyên; client tự reconnect.
- **Reconnect phải có jitter.** Khi một node restart, toàn bộ client mất kết nối cùng lúc; nếu tất
  cả reconnect sau đúng 1s thì tạo thundering herd. Yêu cầu client: backoff luỹ thừa + jitter ngẫu nhiên.
- Hỗ trợ `Last-Event-ID`: khi reconnect, server phát lại tối đa N (ví dụ 50) thông báo mới hơn id đó,
  lấy từ DB qua đúng predicate `NotificationAudience`.
- **Thu hồi quyền:** `permissions` là snapshot lúc kết nối, nên sẽ cũ sau khi đổi vai trò. Xử lý bằng
  sự kiện `REVOKE` phát trên `notif:org:{orgId}` kèm `employeeId` → registry đóng emitter của người
  đó, buộc reconnect với token mới. (Kết hợp với `JwtBlacklistFilter` đã có.)

### 6.4. Write path

```java
@Transactional
public String publish(NotificationCommand cmd) {
    accessGuard.checkSendable(cmd, ctx);                 // chỉ với luồng do user khởi tạo
    Notification saved = repository.save(mapper.toEntity(cmd));   // 1 INSERT
    events.publishEvent(new NotificationCreatedEvent(Envelope.of(saved)));
    return saved.getId();
}

@TransactionalEventListener(phase = AFTER_COMMIT)       // KHÔNG BAO GIỜ đẩy trước khi commit
public void onCreated(NotificationCreatedEvent e) {
    brokerExecutor.execute(() -> broker.publish(e.envelope()));   // executor riêng, nhỏ
}
```

- `AFTER_COMMIT` là bắt buộc: đẩy trước commit → client nhận realtime rồi gọi REST lại không thấy gì.
- Publish chạy trên executor riêng để Redis chậm/treo không kéo theo thread request.
- Redis lỗi → log + tăng metric, **không** ném exception làm hỏng nghiệp vụ đã commit. Client vẫn
  nhận đủ dữ liệu ở lần poll/reconnect kế tiếp.
- Batch: `publishAll(List<NotificationCommand>)` dùng `saveAll` (đã có
  `hibernate.jdbc.batch_size=5000` + `order_inserts=true`) và một lần PUBLISH duy nhất.
- Idempotency: `dedupe_key` + unique index → nguồn phát at-least-once gửi lại không tạo bản trùng.
- **Rate limit** người gửi broadcast (token bucket trên Redis, theo `employeeId`) để chặn bão thông báo.

### 6.5. Read path

- **Cursor pagination** theo `(created_at, id)` thay cho OFFSET. Bản hiện tại dùng
  `PageRequest.of(page, size)` — trang sâu buộc PostgreSQL quét và bỏ qua toàn bộ dòng phía trước.
  Giữ endpoint offset cho tương thích, khuyến nghị client chuyển sang cursor.
- **Đếm chưa đọc** là truy vấn nóng nhất (badge poll liên tục): cache tại
  `notif:unread:{orgId}:{branchId}:{employeeId}`, `INCR` khi đẩy, `DECR` khi đánh dấu đọc,
  TTL 1 giờ, miss thì tính lại từ DB. Tránh `COUNT(*)` + LEFT JOIN mỗi vài giây trên mỗi người dùng.
- Đánh dấu đã đọc: `INSERT ... ON CONFLICT DO NOTHING` vào `notification_receipts` — không đụng tới
  bảng `notifications`, nên không có contention giữa người đọc và người ghi.

---

## 7. CẤU TRÚC PACKAGE

Engine đặt ở `common` (theo quy tắc DO #8: hạ tầng realtime dùng chung, module nghiệp vụ không được
phụ thuộc chéo vào module notification):

```text
com.restaurant.crm.common
├── sse
│   ├── SseConnection, SseConnectionRegistry, SseProperties
│   └── service/{interfaces,impl}/SseEmitterService
└── notification
    ├── constants/     NotificationConstants, NotificationChannelConstants
    ├── dto/
    │   ├── NotificationCommand, NotificationEnvelope, RecipientContext
    │   └── response/NotificationResponse, UnreadCountResponse
    ├── entity/        Notification, NotificationReceipt
    ├── enums/         NotificationScope, NotificationGroupType, NotificationType,
    │                  NotificationPriority, NotificationSenderType, ReceiptStatus
    ├── mapper/        NotificationMapper (MapStruct)
    ├── repository/    NotificationRepository, NotificationReceiptRepository
    ├── security/      NotificationAudience, NotificationAccessGuard
    ├── transport/     NotificationBroker (interface), RedisNotificationBroker,
    │                  NotificationRedisListenerConfig
    └── service/
        ├── interfaces/ NotificationPublisher, NotificationQueryService
        └── impl/       NotificationPublisherImpl, NotificationQueryServiceImpl

com.restaurant.crm.modules.erp.notification
└── controller/NotificationController      // REST + SSE endpoint, uỷ quyền xuống common
```

### 7.1. Registry kiểu thông báo (tách nơi phát khỏi quyết định phân quyền)

```java
public enum NotificationType {
    READY_TO_SERVE (GROUP,  BY_PERMISSION, StartDefinedOrgPermission.ORDER_READ,  HIGH),
    TABLE_ASSIGNED (DIRECT, null,          null,                                  NORMAL),
    SHIFT_CHANGED  (DIRECT, null,          null,                                  NORMAL),
    LOW_STOCK      (GROUP,  BY_PERMISSION, StartDefinedOrgPermission.MENU_MANAGE,  HIGH),
    SYSTEM_MAINTENANCE (SYSTEM, null,      null,                                  CRITICAL);
    // defaultScope, groupType, requiredPermission, priority
}
```

Module nghiệp vụ chỉ cần gọi `publisher.publish(NotificationCommand.of(READY_TO_SERVE, orderId, ...))`
— không tự quyết định ai được xem. Mọi chính sách hiển thị nằm gọn một chỗ, sửa một chỗ.

### 7.2. API

| Method | Path | Mô tả | Quyền |
|---|---|---|---|
| `GET` | `/api/v1/notifications/subscribe` | Mở SSE stream (tenant lấy từ token) | `isAuthenticated()`, token `CONTEXT` |
| `GET` | `/api/v1/notifications` | Feed, cursor pagination | `isAuthenticated()` |
| `GET` | `/api/v1/notifications/unread-count` | Badge | `isAuthenticated()` |
| `PUT` | `/api/v1/notifications/{id}/read` | Đánh dấu đã đọc | chính chủ (qua predicate) |
| `PUT` | `/api/v1/notifications/read-all` | Đánh dấu đọc hết | chính chủ |
| `POST` | `/api/v1/notifications` | Gửi thủ công (§4.1) | theo scope |

Tất cả trả `ResponseEntity<ApiResponse<T>>` đúng chuẩn §5.2 của `CODE_REGULATIONS.md`.

---

## 8. XỬ LÝ SỰ CỐ

| Sự cố | Hành vi hệ thống |
|---|---|
| Redis chết | Fan-out nội bộ node vẫn chạy; publish lỗi → log + metric; client đồng bộ lại qua REST khi reconnect. Nghiệp vụ đã commit **không** bị ảnh hưởng |
| Node restart | Emitter chết hàng loạt → client reconnect có jitter + `Last-Event-ID` để phát lại |
| Client chậm | Queue đầy → bỏ bản tin cũ nhất, đặt `stale`, gửi `RESYNC` |
| Đổi vai trò / nghỉ việc | Sự kiện `REVOKE` → đóng emitter, buộc lấy token mới |
| Bão thông báo | Rate limit theo người gửi; `priority` cho phép hạ cấp/bỏ bớt loại `LOW` khi quá tải |
| Thông báo trùng | `dedupe_key` + unique index |

**Metrics (Actuator, đã có `spring-boot-starter-actuator`):** số kết nối đang mở (gauge, tách theo
tenant), độ sâu queue (histogram), số envelope bị bỏ (counter), độ trễ publish→gửi (timer), số lần
`RESYNC`. Không có các chỉ số này thì không thể biết hệ thống đang âm thầm mất thông báo hay không.

---

## 9. LỘ TRÌNH TRIỂN KHAI

| Giai đoạn | Nội dung | Giá trị |
|---|---|---|
| **P0** ✅ | Sửa IDOR: `branchId` lấy từ token, không lấy từ query param | Bịt lỗ hổng dữ liệu — làm ngay, độc lập với phần còn lại |
| **P1** ✅ | Entity mới + `notification_receipts` + enum scope + Flyway (bổ sung dependency) + index | Đúng ngữ nghĩa 4 scope; sửa lỗi trạng thái đọc dùng chung |
| **P2** | `NotificationAudience` (predicate sinh đôi) + `NotificationAccessGuard` + contract test | Chốt permission matrix, chống lệch hai đường kiểm tra |
| **P3** | Viết lại registry: CHM lồng nhau, index theo employee, bounded queue, virtual thread, heartbeat | Chịu tải cao, không còn head-of-line blocking |
| **P4** | `RedisNotificationBroker` + channel theo org | Scale ngang nhiều node + cách ly ở tầng transport |
| **P5** | Redis unread counter, cursor pagination, rate limit, metrics | Tối ưu đường đọc & vận hành |
| **P6** | Partition theo tháng, job dọn theo `expires_at` | Khi dữ liệu lớn dần |

P0 nên tách PR riêng và merge trước — nó là lỗi bảo mật đang tồn tại, không nên chờ toàn bộ thiết kế
này hoàn tất.

---

## 10. KIỂM THỬ

1. **Contract test predicate** (§4.3) — quan trọng nhất: `matches()` ≡ `specFor()` trên toàn bộ ma
   trận tổ hợp scope × groupType × role × permission × tenant.
2. **Tenant isolation test** — với mỗi endpoint: token org A + tài nguyên org B ⇒ **404**, không rò rỉ
   sự tồn tại.
3. **Concurrency test** — 1.000 emitter giả lập, 10.000 sự kiện, trong đó có client cố tình chậm:
   assert không giao hàng chéo tenant, không deadlock, bộ nhớ có trần, client chậm không làm chậm
   client nhanh.
4. **Leak test** — mở/đóng 10.000 kết nối, assert `byTenant` và `byEmployee` rỗng hoàn toàn sau đó.
5. **Idempotency test** — publish cùng `dedupe_key` 100 lần đồng thời ⇒ đúng 1 dòng.
