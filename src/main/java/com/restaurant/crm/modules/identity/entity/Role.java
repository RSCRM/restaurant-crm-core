package com.restaurant.crm.modules.identity.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.identity.constants.role.RoleConstants;
import com.restaurant.crm.modules.identity.enums.SystemDataScope;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = RoleConstants.TABLE_ROLE)
public class Role extends BaseEntity {

        @Column(name = RoleConstants.COL_ROLE_NAME,
                nullable = false, unique = true,
                columnDefinition = RoleConstants.ROLE_NAME_DEFINITION)
        @Size(min = RoleConstants.MIN_CHARS_ROLE_NAME,
                max = RoleConstants.MAX_CHARS_ROLE_NAME)
        String roleName;

        @Enumerated(EnumType.STRING)
        @Column(name = RoleConstants.COL_DATA_SCOPE, nullable = false)
        SystemDataScope dataScope;

        @ManyToMany(fetch = FetchType.LAZY)
        Set<Permission> permissions;
}