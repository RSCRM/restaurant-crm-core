package com.restaurant.crm.modules.erp.menu.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "modifier_groups", uniqueConstraints = {
        @UniqueConstraint(name = "uk_modifier_groups_product_group_name", columnNames = {"product_id", "group_name"})
})
@AttributeOverride(name = "id", column = @Column(name = "modifier_group_id", columnDefinition = "VARCHAR(36)"))
public class ModifierGroup extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Product product;

    @NotBlank
    @NotNull
    @Size(max = 100)
    @Column(name = "group_name", nullable = false, length = 100)
    String groupName;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    String description;

    @Builder.Default
    @NotNull
    @Min(0)
    @Column(name = "min_selection", nullable = false)
    Integer minSelection = 0;

    @Builder.Default
    @NotNull
    @Min(0)
    @Column(name = "max_selection", nullable = false)
    Integer maxSelection = 1;
}
