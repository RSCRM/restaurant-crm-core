package com.restaurant.crm.modules.identity.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.identity.constants.user.UserProfileConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = UserProfileConstants.TABLE_USER_PROFILE,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_profiles_user_id",
                        columnNames = UserProfileConstants.COL_USER_ID),
                @UniqueConstraint(name = "uk_user_profiles_phone",
                        columnNames = UserProfileConstants.COL_PHONE)
        })
public class UserProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = UserProfileConstants.COL_USER_ID, nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    User user;

    @Column(name = UserProfileConstants.COL_FULL_NAME,
            columnDefinition = UserProfileConstants.FULL_NAME_DEFINITION)
    String fullName;

    @Column(name = UserProfileConstants.COL_PHONE,
            columnDefinition = UserProfileConstants.PHONE_DEFINITION)
    String phone;
}
