package com.restaurant.crm.modules.erp.menu.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
@Table(name = "combo_items", uniqueConstraints = {
        @UniqueConstraint(name = "uk_combo_items_combo_product", columnNames = {"combo_id", "product_id"})
})
@AttributeOverride(name = "id", column = @Column(name = "combo_item_id", columnDefinition = "VARCHAR(36)"))
public class ComboItem extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    Combo combo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @Builder.Default
    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    Integer quantity = 1;
}
