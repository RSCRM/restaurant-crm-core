package com.restaurant.crm.modules.erp.menu.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.dto.request.ComboItemRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateComboRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateComboRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.ComboItemResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ComboResponse;
import com.restaurant.crm.modules.erp.menu.entity.Combo;
import com.restaurant.crm.modules.erp.menu.entity.ComboItem;
import com.restaurant.crm.modules.erp.menu.entity.ComboItemModifierOption;
import com.restaurant.crm.modules.erp.menu.entity.ModifierGroup;
import com.restaurant.crm.modules.erp.menu.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.menu.mapper.MenuManagementMapper;
import com.restaurant.crm.modules.erp.menu.repository.ComboItemModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.repository.ComboItemRepository;
import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.repository.ModifierGroupRepository;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.menu.security.MenuBranchGuard;
import com.restaurant.crm.modules.erp.menu.service.interfaces.ComboManagementService;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComboManagementServiceImpl implements ComboManagementService {

    ComboRepository comboRepository;
    ComboItemRepository comboItemRepository;
    ComboItemModifierOptionRepository comboItemModifierOptionRepository;
    ProductRepository productRepository;
    ModifierGroupRepository modifierGroupRepository;
    ModifierOptionRepository modifierOptionRepository;
    OrganizationBranchRepository organizationBranchRepository;
    MenuBranchGuard branchGuard;
    MenuManagementMapper mapper;

    @Override
    @Transactional
    public ComboResponse create(CreateComboRequest request) {
        branchGuard.validateBranchAccess(request.getBranchId());
        if (comboRepository.existsByBranch_IdAndComboName(request.getBranchId(), request.getComboName())) {
            throw new AppException(ErrorCode.COMBO_NAME_EXISTS);
        }
        OrganizationBranch branch = organizationBranchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        Combo combo = Combo.builder()
                .branch(branch).comboName(request.getComboName()).description(request.getDescription())
                .price(request.getPrice()).status(request.getStatus() != null ? request.getStatus() : "AVAILABLE").build();
        return toComboResponse(comboRepository.save(combo));
    }

    @Override
    @Transactional
    public ComboResponse update(String id, UpdateComboRequest request) {
        Combo combo = loadCombo(id);
        branchGuard.validateBranchAccess(combo.getBranch().getId());
        if (!combo.getComboName().equals(request.getComboName())
                && comboRepository.existsByBranch_IdAndComboNameAndIdNot(combo.getBranch().getId(), request.getComboName(), id)) {
            throw new AppException(ErrorCode.COMBO_NAME_EXISTS);
        }
        combo.setComboName(request.getComboName());
        combo.setDescription(request.getDescription());
        combo.setPrice(request.getPrice());
        if (request.getStatus() != null) combo.setStatus(request.getStatus());
        return toComboResponse(comboRepository.save(combo));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Combo combo = loadCombo(id);
        branchGuard.validateBranchAccess(combo.getBranch().getId());
        List<ComboItem> items = comboItemRepository.findByCombo_Id(id);
        for (ComboItem item : items) {
            comboItemModifierOptionRepository.deleteByComboItem_Id(item.getId());
        }
        items.forEach(comboItemRepository::delete);
        comboRepository.delete(combo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComboResponse> listByBranch(String branchId) {
        branchGuard.validateBranchAccess(branchId);
        return comboRepository.findByBranchIdOrderByComboNameAsc(branchId).stream().map(this::toComboResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ComboResponse get(String id) {
        Combo combo = loadCombo(id);
        branchGuard.validateBranchAccess(combo.getBranch().getId());
        return toComboResponse(combo);
    }

    @Override
    @Transactional
    public ComboItemResponse addItem(String comboId, ComboItemRequest request) {
        Combo combo = loadCombo(comboId);
        branchGuard.validateBranchAccess(combo.getBranch().getId());
        if (comboItemRepository.existsByCombo_IdAndProduct_Id(comboId, request.getProductId())) {
            throw new AppException(ErrorCode.COMBO_ITEM_PRODUCT_EXISTS);
        }
        Product product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getBranch().getId().equals(combo.getBranch().getId())) {
            throw new AppException(ErrorCode.COMBO_ITEM_PRODUCT_BRANCH_MISMATCH);
        }
        List<ModifierOption> options = validateAndLoadOptions(product, request.getModifierOptionIds());

        ComboItem item = comboItemRepository.save(ComboItem.builder()
                .combo(combo).product(product).quantity(request.getQuantity()).build());
        for (ModifierOption option : options) {
            comboItemModifierOptionRepository.save(ComboItemModifierOption.builder()
                    .comboItem(item).modifierOption(option).build());
        }
        return toComboItemResponse(item);
    }

    @Override
    @Transactional
    public ComboItemResponse updateItem(String itemId, ComboItemRequest request) {
        ComboItem item = comboItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.COMBO_ITEM_NOT_FOUND));
        branchGuard.validateBranchAccess(item.getCombo().getBranch().getId());
        Product product = item.getProduct();
        List<ModifierOption> options = validateAndLoadOptions(product, request.getModifierOptionIds());

        item.setQuantity(request.getQuantity());
        comboItemRepository.save(item);
        comboItemModifierOptionRepository.deleteByComboItem_Id(itemId);
        for (ModifierOption option : options) {
            comboItemModifierOptionRepository.save(ComboItemModifierOption.builder()
                    .comboItem(item).modifierOption(option).build());
        }
        return toComboItemResponse(item);
    }

    @Override
    @Transactional
    public void deleteItem(String itemId) {
        ComboItem item = comboItemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.COMBO_ITEM_NOT_FOUND));
        branchGuard.validateBranchAccess(item.getCombo().getBranch().getId());
        comboItemModifierOptionRepository.deleteByComboItem_Id(itemId);
        comboItemRepository.delete(item);
    }

    /** Luat: moi group cua product dung 1 option; so option = so group; option phai thuoc group cua product. */
    private List<ModifierOption> validateAndLoadOptions(Product product, List<String> optionIds) {
        List<String> productGroupIds = modifierGroupRepository.findByProduct_IdOrderByGroupNameAsc(product.getId())
                .stream().map(ModifierGroup::getId).toList();
        List<String> ids = optionIds == null ? List.of() : optionIds;
        if (ids.size() != productGroupIds.size()) {
            throw new AppException(ErrorCode.COMBO_ITEM_OPTIONS_MISMATCH);
        }
        List<ModifierOption> options = new ArrayList<>();
        Set<String> usedGroups = new HashSet<>();
        for (String optionId : ids) {
            ModifierOption option = modifierOptionRepository.findById(optionId)
                    .orElseThrow(() -> new AppException(ErrorCode.MODIFIER_OPTION_NOT_FOUND));
            String gid = option.getModifierGroup().getId();
            if (!productGroupIds.contains(gid) || !usedGroups.add(gid)) {
                throw new AppException(ErrorCode.COMBO_ITEM_OPTIONS_MISMATCH);
            }
            options.add(option);
        }
        return options;
    }

    private Combo loadCombo(String id) {
        return comboRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.COMBO_NOT_FOUND));
    }

    private ComboResponse toComboResponse(Combo combo) {
        ComboResponse res = mapper.toComboResponse(combo);
        res.setItems(comboItemRepository.findByCombo_Id(combo.getId()).stream().map(this::toComboItemResponse).toList());
        return res;
    }

    private ComboItemResponse toComboItemResponse(ComboItem item) {
        ComboItemResponse res = mapper.toComboItemResponse(item);
        res.setModifierOptionIds(comboItemModifierOptionRepository.findByComboItem_Id(item.getId())
                .stream().map(cimo -> cimo.getModifierOption().getId()).toList());
        return res;
    }
}
