package com.restaurant.crm.modules.erp.menu.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import jakarta.persistence.*;
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

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "combos", uniqueConstraints = {
        @UniqueConstraint(name = "uk_combos_branch_combo_name", columnNames = {"branch_id", "combo_name"})
})
@AttributeOverride(name = "id", column = @Column(name = "combo_id", columnDefinition = "VARCHAR(36)"))
public class Combo extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    OrganizationBranch branch;

    @NotBlank
    @NotNull
    @Size(max = 150)
    @Column(name = "combo_name", nullable = false, length = 150)
    String comboName;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Builder.Default
    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    BigDecimal price = BigDecimal.ZERO;

    @Size(max = 255)
    @Column(name = "image_url", length = 255)
    String imageUrl;

    @Builder.Default
    @NotBlank
    @NotNull
    @Size(max = 20)
    @Column(name = "status", nullable = false, length = 20)
    String status = "AVAILABLE";
}
