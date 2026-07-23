# Software Requirements Specification (SRS)
# Authentication & Context Switching Module
Version: 1.0

---

# 1. Overview

## 1.1 Purpose

This document specifies the Authentication and Context Switching mechanism for the Restaurant Management Platform.

The platform supports multiple user types:

- System Administrator
- Restaurant Owner
- Branch Manager
- Cashier
- Waiter
- Kitchen Staff
- Inventory Manager
- ...

A single user account may own multiple roles across different branches or restaurants.

Instead of forcing the user to login multiple times, the platform provides:

- Single authentication
- Context discovery
- Context switching
- RBAC authorization based on the selected context

---

# 2. Goals

The solution must satisfy the following requirements:

- User authenticates only once.
- User may belong to multiple organizations.
- User may belong to multiple branches.
- User may own multiple organizational roles.
- Authorization must always execute under exactly one context.
- Every business request belongs to one selected context.
- Permission checking must be simple.
- FE can easily switch between branches.

---

# 3. Definitions

## Identity

Represents the authenticated user.

Example

```
john@gmail.com
```

Identity never changes until logout.

---

## Context

Represents where the user is currently working.

Example

```
Restaurant ABC

↓

Branch 01

↓

Manager
```

or

```
Restaurant ABC

↓

Branch 02

↓

Cashier
```

Context may change during one login session.

---

## Context Switching

The process of selecting another working context after authentication.

Example

```
Manager

↓

Branch A

↓

Switch

↓

Branch B

↓

Continue working
```

---

# 4. Business Rules

## BR-01

A user authenticates only once.

---

## BR-02

A user may belong to multiple restaurants.

---

## BR-03

A user may belong to multiple branches.

---

## BR-04

A user may own different roles in different branches.

Example

```
Branch A

Manager
```

```
Branch B

Cashier
```

---

## BR-05

Every API request executes under exactly one context.

---

## BR-06

Context determines:

- Restaurant
- Branch
- Role
- Permissions

---

## BR-07

Permission checking is always based on the current context.

---

# 5. Database Design

## User

```
User

id

email

password_hash

status
```

---

## Employee

```
Employee

id

user_id

restaurant_id

branch_id

org_role_id

status
```

A user may have multiple employee records.

Example

```
User

↓

Employee 1

↓

Branch A

↓

Manager
```

```
User

↓

Employee 2

↓

Branch B

↓

Cashier
```

---

## Org Role

```
Org_Role

id

name
```

Example

```
Manager

Cashier

Kitchen

Waiter
```

---

## Org Permission

```
Org_Permission

id

code

description
```

---

## Org Role Permission

```
Org_Role_Permission

role_id

permission_id
```

---

# 6. Authentication Flow

```
User

↓

Login

↓

Verify email/password

↓

Generate Identity Token

↓

Return available contexts

↓

User selects context

↓

Generate Context Token

↓

Access Business APIs
```

---

# 7. Login API

## Endpoint

```
POST /api/auth/login
```

---

## Request

```json
{
    "email":"owner@gmail.com",
    "password":"******"
}
```

---

## Processing

1. Validate credentials.
2. Load user.
3. Load System Roles.
4. Load Employee contexts.
5. Generate Identity Token.
6. Return contexts.

---

## Response

```json
{
    "accessToken":"IDENTITY_TOKEN",

    "refreshToken":"xxxxx",

    "contexts":[
        {
            "employeeId":1,
            "restaurantId":1,
            "restaurantName":"ABC Restaurant",
            "branchId":1,
            "branchName":"Branch 1",
            "role":"MANAGER"
        },
        {
            "employeeId":2,
            "restaurantId":1,
            "restaurantName":"ABC Restaurant",
            "branchId":2,
            "branchName":"Branch 2",
            "role":"CASHIER"
        }
    ],

    "systemRoles":[
        "SYSTEM_ADMIN"
    ]
}
```

---

# 8. Context Selection

## Endpoint

```
POST /api/auth/context
```

---

## Authorization

Identity Token

---

## Request

```json
{
    "role": "Manager",
    "organizationId": 123, 
    "employeeId":2
}
```
employeeId null nếu role là owner

---

## Processing

1. Verify Identity Token.
2. Verify employee belongs to user.
3. Load role.
4. Load permissions.
5. Generate Context Token.

---

## Response

```json
{
    "accessToken":"CONTEXT_TOKEN",

    "refreshToken":"xxxxx"
}
```

---

# 9. Identity Token

Purpose

Authentication only.

Not used for business APIs.

Example payload

```json
{
    "sub":"user-001",

    "email":"owner@gmail.com",

    "type":"IDENTITY"
}
```

---

# 10. Context Token

Purpose

Authentication + Authorization

Example payload

```json
{
    "sub":"user-001",

    "type":"CONTEXT",

    "restaurantId":1,

    "branchId":2,

    "employeeId":2,

    "orgRole":"CASHIER",

    "permissions":[
        "ORDER_READ",
        "ORDER_CREATE",
        "PAYMENT_READ"
    ]
}
```

---

# 11. Authorization Flow

```
Request

↓

JWT Filter

↓

Extract JWT

↓

Build Principal

↓

AOP

↓

Permission Check

↓

Business Logic
```

---

# 12. Principal Model

```java
public class JwtPrincipal {

    private UUID userId;

    private UUID employeeId;

    private UUID restaurantId;

    private UUID branchId;

    private String role;

    private Set<String> permissions;

}
```

---

# 13. Permission Annotation

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    String value();

}
```

---

Example

```java
@RequirePermission("ORDER_CREATE")
@PostMapping("/orders")
public ResponseEntity<?> createOrder() {

}
```

---

# 14. Authorization Aspect

```
Receive Request

↓

Read JWT

↓

Get Principal

↓

Read Annotation

↓

Compare Permission

↓

Allowed ?

↓

YES → Continue

NO → 403
```

---

# 15. Context Retrieval

Business services never trust FE.

Example

```
POST /orders
```

Request

```json
{
    "tableId":5,

    "items":[]
}
```

Restaurant ID and Branch ID are extracted from JWT.

Service

```
restaurantId = principal.restaurantId

branchId = principal.branchId
```

Repository

```
SELECT *

FROM orders

WHERE branch_id = currentBranch
```

---

# 16. Context Switching

User currently

```
Branch A

↓

Manager
```

Switch

↓

```
Branch B

↓

Cashier
```

Flow

```
POST /auth/context

↓

Generate new Context Token

↓

Replace Access Token

↓

Continue working
```

Logout is not required.

---

# 17. Frontend Flow

```
Login

↓

Store Identity Token

↓

Display Context List

↓

Select Context

↓

Receive Context Token

↓

Replace Token

↓

Redirect Dashboard
```

---

# 18. Security Rules

SR-01

Identity Token cannot access business APIs.

---

SR-02

Context Token is required for every protected endpoint.

---

SR-03

Every request must contain Authorization header.

---

SR-04

Branch ID must never be trusted from client requests.

---

SR-05

Restaurant ID must never be trusted from client requests.

---

SR-06

Permission must be verified by backend.

---

SR-07

Frontend menu visibility is only for UX.

Backend authorization is mandatory.

---

# 19. Error Codes

| Code | Description |
|------|-------------|
|401|Unauthenticated|
|403|Permission Denied|
|404|Context Not Found|
|409|Context Already Selected|
|422|Invalid Context|
|498|Identity Token Required|
|499|Context Token Required|

---

# 20. Sequence Diagram

```
User
    │
    │ Login
    ▼
Authentication Service
    │
    │ Verify Credentials
    ▼
Database
    │
    │ Load User
    │ Load Employee Contexts
    ▼
Authentication Service
    │
    │ Generate Identity Token
    ▼
Frontend
    │
    │ Display Context List
    ▼
User
    │
    │ Select Branch
    ▼
Authentication Service
    │
    │ Generate Context Token
    ▼
Frontend
    │
    │ Store Context Token
    ▼
Business APIs
```

---

# 21. Advantages

- Single Sign-On experience.
- Supports users with multiple branches.
- Supports users with multiple organizational roles.
- Simple permission checking.
- Clean RBAC implementation.
- Easy branch switching.
- Easy migration to microservices.
- JWT always represents exactly one execution context.
- Business APIs remain tenant-safe and branch-safe.