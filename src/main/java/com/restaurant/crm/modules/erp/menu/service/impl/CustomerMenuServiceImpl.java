package com.restaurant.crm.modules.erp.menu.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.dto.response.CustomerMenuResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuCategoryResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuComboResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuModifierGroupResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuModifierOptionResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuProductResponse;
import com.restaurant.crm.modules.erp.menu.mapper.CustomerMenuMapper;
import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierGroup;
import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.modifier.repository.ModifierGroupRepository;
import com.restaurant.crm.modules.erp.menu.modifier.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.menu.constants.CustomerMenuConstants;
import com.restaurant.crm.modules.erp.menu.service.interfaces.CustomerMenuService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Read-only per-branch menu assembly (uc-c-04). Runs a fixed set of queries (products, combos,
 * groups, options) — no N+1 — and groups the result into the customer menu tree.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerMenuServiceImpl implements CustomerMenuService {

    ProductRepository productRepository;
    ComboRepository comboRepository;
    ModifierGroupRepository modifierGroupRepository;
    ModifierOptionRepository modifierOptionRepository;
    CustomerMenuMapper customerMenuMapper;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CustomerMenuConstants.CACHE_NAME,
            key = "T(com.restaurant.crm.modules.identity.utils.AuthUtils).getBranchId()")
    public CustomerMenuResponse getMenu() {
        String branchId = requireBranchId();

        List<Product> products = productRepository
                .findByBranchIdOrderByCategoryIdAscProductNameAsc(branchId);
        List<Combo> combos = comboRepository.findByBranchIdOrderByComboNameAsc(branchId);
        if (products.isEmpty() && combos.isEmpty()) {
            throw new AppException(ErrorCode.MENU_EMPTY);
        }

        List<ModifierGroup> groups = modifierGroupRepository.findByBranchIdOrderByGroupNameAsc(branchId);
        List<String> groupIds = groups.stream().map(ModifierGroup::getId).toList();
        // One query for all options of all groups (no N+1); skip entirely when there are no groups.
        List<ModifierOption> options = groupIds.isEmpty()
                ? List.of()
                : modifierOptionRepository.findByModifierGroupIdInOrderByOptionNameAsc(groupIds);

        Map<String, List<MenuModifierOptionResponse>> optionsByGroup = options.stream()
                .collect(Collectors.groupingBy(
                        option -> option.getModifierGroup().getId(),
                        Collectors.mapping(customerMenuMapper::toModifierOptionResponse, Collectors.toList())));

        return CustomerMenuResponse.builder()
                .branchId(branchId)
                .categories(toCategories(products))
                .combos(customerMenuMapper.toComboResponses(combos))
                .modifierGroups(groups.stream()
                        .map(group -> customerMenuMapper.toModifierGroupResponse(
                                group, optionsByGroup.getOrDefault(group.getId(), List.of())))
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuProductResponse getProduct(String productId) {
        String branchId = requireBranchId();
        Product product = productRepository.findByIdAndBranchId(productId, branchId)
                .orElseThrow(() -> new AppException(ErrorCode.MENU_PRODUCT_NOT_FOUND));
        return customerMenuMapper.toProductResponse(product);
    }

    private String requireBranchId() {
        String branchId = AuthUtils.getBranchId();
        if (branchId == null || branchId.isBlank()) {
            throw new AppException(ErrorCode.MENU_BRANCH_CONTEXT_MISSING);
        }
        return branchId;
    }

    /**
     * Groups pre-sorted products (categoryId asc, productName asc) into categories, preserving
     * that stable order. {@code categoryName} stays null — no Category entity yet (TODO uc-c-04).
     */
    private List<MenuCategoryResponse> toCategories(List<Product> products) {
        Map<String, List<Product>> byCategory = products.stream()
                .collect(Collectors.groupingBy(Product::getCategoryId, LinkedHashMap::new, Collectors.toList()));
        return byCategory.entrySet().stream()
                .map(entry -> MenuCategoryResponse.builder()
                        .categoryId(entry.getKey())
                        .categoryName(null)
                        .products(customerMenuMapper.toProductResponses(entry.getValue()))
                        .build())
                .toList();
    }
}
