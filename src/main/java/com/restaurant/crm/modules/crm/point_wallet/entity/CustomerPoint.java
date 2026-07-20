package com.restaurant.crm.modules.crm.point_wallet.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.crm.customer_account.entity.Customer;
import com.restaurant.crm.modules.crm.point_wallet.constants.CustomerPointConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = CustomerPointConstants.TABLE_CUSTOMER_POINT,
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customer_restaurant", columnNames = {CustomerPointConstants.COL_CUSTOMER_ID, CustomerPointConstants.COL_RESTAURANT_ID})
        }
)
public class CustomerPoint extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = CustomerPointConstants.COL_CUSTOMER_ID, nullable = false)
    Customer customer;

    @NotNull
    @Column(name = CustomerPointConstants.COL_RESTAURANT_ID, nullable = false)
    String restaurantId;

    @Builder.Default
    @Column(name = CustomerPointConstants.COL_CURRENT_POINTS, nullable = false)
    Integer currentPoints = 0;

    @Builder.Default
    @Column(name = CustomerPointConstants.COL_LIFETIME_POINTS, nullable = false)
    Integer lifetimePoints = 0;
}
