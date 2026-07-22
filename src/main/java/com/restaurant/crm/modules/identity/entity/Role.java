package com.restaurant.crm.modules.identity.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.identity.constants.role.RoleConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
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

        @ManyToMany(fetch = FetchType.LAZY)
        @JoinTable(name = RoleConstants.TABLE_ROLE_PERMISSION,
                joinColumns = @JoinColumn(name = RoleConstants.COL_ROLE_ID),
                inverseJoinColumns = @JoinColumn(name = RoleConstants.COL_PERMISSION_ID))
        Set<Permission> permissions;
}
