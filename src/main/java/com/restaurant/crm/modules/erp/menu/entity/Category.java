package com.restaurant.crm.modules.erp.menu.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_categories_branch_category_name", columnNames = {"branch_id", "category_name"})
})
@AttributeOverride(name = "id", column = @Column(name = "category_id", columnDefinition = "VARCHAR(36)"))
public class Category extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    OrganizationBranch branch;

    @NotNull
    @Size(max = 100)
    @Column(name = "category_name", nullable = false, length = 100)
    String categoryName;

    @Size(max = 255)
    @Column(name = "description", length = 255)
    String description;

    @Column(name = "display_order")
    Integer displayOrder;
}
