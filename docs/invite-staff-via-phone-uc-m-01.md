-- =========================================================
-- IDENTITY & ORGANIZATION SCHEMA
-- =========================================================

CREATE TABLE users (
    id           VARCHAR(36) PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL,
    password     VARCHAR(255) NOT NULL,
    email        VARCHAR(100),
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    status       VARCHAR(20),
    version      BIGINT       DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(36),
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(36),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email)
);

CREATE TABLE roles (
    id           VARCHAR(36) PRIMARY KEY,
    role_name    VARCHAR(50) NOT NULL,
    version      BIGINT      DEFAULT 0,
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(36),
    updated_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(36),
    CONSTRAINT uk_roles_name UNIQUE (role_name)
);

CREATE TABLE permissions (
    id              VARCHAR(36) PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL,
    version         BIGINT       DEFAULT 0,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(36),
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(36),
    CONSTRAINT uk_permissions_name UNIQUE (permission_name)
);

CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE roles_permissions (
    role_id        VARCHAR(36) NOT NULL,
    permissions_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (role_id, permissions_id),
    CONSTRAINT fk_rp_role       FOREIGN KEY (role_id)        REFERENCES roles (id),
    CONSTRAINT fk_rp_permission FOREIGN KEY (permissions_id) REFERENCES permissions (id)
);

-- =========================================================
-- ORGANIZATION-LEVEL RBAC
-- =========================================================

CREATE TABLE org_roles (
    id           VARCHAR(36) PRIMARY KEY,
    role_name    VARCHAR(50) NOT NULL,
    version      BIGINT      DEFAULT 0,
    created_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(36),
    updated_at   TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_by   VARCHAR(36)
);

CREATE TABLE org_permissions (
    id              VARCHAR(36) PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL,
    version         BIGINT       DEFAULT 0,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(36),
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(36)
);

CREATE TABLE org_roles_org_permissions (
    org_role_id        VARCHAR(36) NOT NULL,
    org_permissions_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (org_role_id, org_permissions_id),
    CONSTRAINT fk_orop_role       FOREIGN KEY (org_role_id)        REFERENCES org_roles (id),
    CONSTRAINT fk_orop_permission FOREIGN KEY (org_permissions_id) REFERENCES org_permissions (id)
);

-- =========================================================
-- ORGANIZATION & BRANCH
-- =========================================================

CREATE TABLE organizations (
    id                VARCHAR(36) PRIMARY KEY,
    organization_name VARCHAR(150) NOT NULL,
    owner_id          VARCHAR(36),
    tax_code          VARCHAR(50),
    address           VARCHAR(255),
    phone             VARCHAR(20),
    email             VARCHAR(100),
    status            VARCHAR(20),
    version           BIGINT    DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(36),
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by        VARCHAR(36)
);

CREATE TABLE organization_branches (
    id              VARCHAR(36) PRIMARY KEY,
    organization_id VARCHAR(36) NOT NULL,
    branch_name     VARCHAR(100) NOT NULL,
    manager_id      VARCHAR(36),
    address         VARCHAR(255),
    phone           VARCHAR(20),
    status          VARCHAR(20),
    version         BIGINT    DEFAULT 0,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(36),
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(36),
    CONSTRAINT fk_branches_organization
        FOREIGN KEY (organization_id) REFERENCES organizations (id)
);

-- =========================================================
-- EMPLOYEES
-- =========================================================

CREATE TABLE employees (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36),
    branch_id   VARCHAR(36),
    org_role_id VARCHAR(36),
    email       VARCHAR(100),
    phone       VARCHAR(20),
    start_date  TIMESTAMP,
    end_date    TIMESTAMP,
    status      VARCHAR(20),
    version     BIGINT    DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(36),
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by  VARCHAR(36),
    CONSTRAINT fk_employees_user     FOREIGN KEY (user_id)     REFERENCES users (id),
    CONSTRAINT fk_employees_branch   FOREIGN KEY (branch_id)   REFERENCES organization_branches (id),
    CONSTRAINT fk_employees_org_role FOREIGN KEY (org_role_id) REFERENCES org_roles (id)
);

-- Khoá ngoại vòng (khai báo sau khi các bảng đã tồn tại)
ALTER TABLE organization_branches
    ADD CONSTRAINT fk_branches_manager
    FOREIGN KEY (manager_id) REFERENCES employees (id);

ALTER TABLE organizations
    ADD CONSTRAINT fk_organizations_owner
    FOREIGN KEY (owner_id) REFERENCES users (id);


------------------------- tasks -------------------------
- tạo module employee thực hiện các task:
+ invite staff via phone (uc-m-01) : ĐÂY đơn giản là add user-employee vào branch của org
+ assign role rbac (uc-m-02): đây là assign role cho employee trong branch của org
+ revoke staff access (uc-m-03): đây là revoke role cho employee trong branch của org
+ salary config (uc-m-04): đây là config lương cho employee trong branch của org



------------------------- business requirements -------------------------
Role bao gồm các fix roles trong hệ thống là:  USER và ADMIN
+ ADMIN là người tạo các USER
+ USER có thể:
    + tạo org
    + tạo các org_role và gắn với các permission sẵn có của hệ thống (1 org_role có thể gắn nhiều permission)
    + tạo branch gắn với org 
    + tạo và gắn org_role vào employee (1 employee có thể gắn 1 org_role)

các permission sẵn có của hệ thống là:  EMPLOYEE_ADD, EMPLOYEE_UPDATE, EMPLOYEE_DELETE, EMPLOYEE_ROLE_ASSIGN, EMPLOYEE_ROLE_REVOKE

- employee có thể được thêm/config lương bởi:
    + ADMIN (là owner của org và các branch của org đó, đồng thời không map vào bất kì bảng employee nào)
    + employee (vẫn là user có role ADMIN nhưng được map vào ít nhất 1 employee) có quyền EMPLOYEE_MODIFY (duyệt đang có role có permission EMPLOYEE_ADD(này là cho hành động thêm)/EMPLOYEE_UPDATE(này là cho hành động cập nhật lương) là được + employee đó phải đang có branch id tương ứng với employee được thêm/config lương)
- employee có thể được assign/revoke roles bởi:
    + ADMIN (là owner của org và các branch của org đó, đồng thời không map vào bất kì bảng employee nào)
    + employee (vẫn là user có role ADMIN nhưng được map vào ít nhất 1 employee) có quyền EMPLOYEE_MODIFY (duyệt đang có role có permission EMPLOYEE_ROLE_ASSIGN/EMPLOYEE_ROLE_REVOKE là được + employee đó phải đang có branch id tương ứng với employee được assign/revoke role)

- role và permission của org được quản lí riêng và map từ ngoài vào các employee

remove cái managerid trong bảng employee

------------------------- coding requirements -------------------------
tạo module mới, không chạm với các module khác ngoại trừ phần common nếu có chạm phải bảo trước:
    + tận dụng các entity đã difined trong identity/organization module, không tạo thêm entity
    + tạo các constants như cách mà các module khác đang làm, bao gồm hiện tại chỉ có 1 là permission constants (EMPLOYEE_ADD, EMPLOYEE_UPDATE, EMPLOYEE_DELETE, EMPLOYEE_ROLE_ASSIGN, EMPLOYEE_ROLE_REVOKE), role constants (USER, ADMIN) không đọc role trong identity (vì cứ code trong module này trước, sau đó sync giữa các module sau)
    + các thứ khác phải được code theo đúng style của các module khác và nếu conflict thì follow theo CODE_REGULATIONS.md
    + tạo permission initializer để tạo các permission sẵn có của hệ thống vào DB như các identity module đang làm
    + tận dụng role constrants của identity để dùng (USER, ADMIN), còn permission thì tạo constants riêng trong module để dùng trong module và initializer để tạo vào DB
    + trước mắt khoan dùng @PreAuthorize để để kiểm tra author, cứ lấy user id về service và check trong service thôi (check role, check org_permission)(chủ org là khi không có employee nào gắn với user này, employee là có quyền tinh chỉnh staff là khi user id đó có map với ít nhất 1 employee và employee đó phải thuộc branch id tương đồng với branch id của employee đang cần được tách động(add/salary config/ assign role/ revoke role) và có role thuộc role cho phép để thực hiện api đó bằng cách nhìn vào org_permissons đang map vào org_role đó), sau này sẽ sync với identity module để dùng @PreAuthorize