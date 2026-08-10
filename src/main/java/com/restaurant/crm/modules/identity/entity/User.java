package com.restaurant.crm.modules.identity.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.identity.constants.user.UserConstants;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Table(name = UserConstants.TABLE_USER)
public class User extends BaseEntity {

        @Column(name = UserConstants.COL_USERNAME,
                nullable = false,
                unique = true,
                columnDefinition = UserConstants.USERNAME_DEFINITION)
        @Size(min = UserConstants.MIN_CHARS_USERNAME,
                max = UserConstants.MAX_CHARS_USERNAME)
        String username;

        @Column(name = UserConstants.COL_PASSWORD,
                nullable = false,
                columnDefinition = UserConstants.PASSWORD_DEFINITION)
        @Size(min = UserConstants.MIN_CHARS_PASSWORD,
                max = UserConstants.MAX_CHARS_PASSWORD)
        String password;

        @Column(name = UserConstants.COL_EMAIL,
                nullable = false,
                unique = true,
                columnDefinition = UserConstants.EMAIL_DEFINITION)
        @Email
        String email;

        @Enumerated(EnumType.STRING)
        @Column(name = UserConstants.COL_STATUS, nullable = false)
        UserStatus status;

        @Builder.Default
        @Column(name = UserConstants.COL_ENABLED, nullable = false)
        boolean enabled = false;

        @ManyToMany()
        @JoinTable(name = "user_roles",
                joinColumns = @JoinColumn(name = "user_id"),
                inverseJoinColumns = @JoinColumn(name = "role_id"))
        Set<Role> roles;
}
