package com.restaurant.crm.modules.erp.menu.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateModifierGroupRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateModifierOptionRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateModifierGroupRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateModifierOptionRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.ModifierGroupResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ModifierOptionResponse;
import com.restaurant.crm.modules.erp.menu.entity.ModifierGroup;
import com.restaurant.crm.modules.erp.menu.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.menu.mapper.MenuManagementMapper;
import com.restaurant.crm.modules.erp.menu.repository.ModifierGroupRepository;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.menu.security.MenuBranchGuard;
import com.restaurant.crm.modules.erp.menu.service.interfaces.ModifierManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ModifierManagementServiceImpl implements ModifierManagementService {

    ProductRepository productRepository;
    ModifierGroupRepository modifierGroupRepository;
    ModifierOptionRepository modifierOptionRepository;
    MenuBranchGuard branchGuard;
    MenuManagementMapper mapper;

    @Override
    @Transactional
    public ModifierGroupResponse createGroup(String productId, CreateModifierGroupRequest request) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        branchGuard.validateBranchAccess(product.getBranch().getId());
        if (modifierGroupRepository.existsByProduct_IdAndGroupName(productId, request.getGroupName())) {
            throw new AppException(ErrorCode.MODIFIER_GROUP_NAME_EXISTS);
        }
        ModifierGroup group = ModifierGroup.builder()
                .product(product).groupName(request.getGroupName()).description(request.getDescription())
                .minSelection(request.getMinSelection()).maxSelection(request.getMaxSelection()).build();
        return mapper.toModifierGroupResponse(modifierGroupRepository.save(group));
    }

    @Override
    @Transactional
    public ModifierGroupResponse updateGroup(String groupId, UpdateModifierGroupRequest request) {
        ModifierGroup group = loadGroup(groupId);
        branchGuard.validateBranchAccess(group.getProduct().getBranch().getId());
        if (!group.getGroupName().equals(request.getGroupName())
                && modifierGroupRepository.existsByProduct_IdAndGroupNameAndIdNot(group.getProduct().getId(), request.getGroupName(), groupId)) {
            throw new AppException(ErrorCode.MODIFIER_GROUP_NAME_EXISTS);
        }
        group.setGroupName(request.getGroupName());
        group.setDescription(request.getDescription());
        group.setMinSelection(request.getMinSelection());
        group.setMaxSelection(request.getMaxSelection());
        return mapper.toModifierGroupResponse(modifierGroupRepository.save(group));
    }

    @Override
    @Transactional
    public void deleteGroup(String groupId) {
        ModifierGroup group = loadGroup(groupId);
        branchGuard.validateBranchAccess(group.getProduct().getBranch().getId());
        modifierOptionRepository.deleteByModifierGroup_Id(groupId);
        modifierGroupRepository.delete(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModifierGroupResponse> listGroupsByProduct(String productId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        branchGuard.validateBranchAccess(product.getBranch().getId());
        return modifierGroupRepository.findByProduct_IdOrderByGroupNameAsc(productId)
                .stream().map(mapper::toModifierGroupResponse).toList();
    }

    @Override
    @Transactional
    public ModifierOptionResponse createOption(String groupId, CreateModifierOptionRequest request) {
        ModifierGroup group = loadGroup(groupId);
        branchGuard.validateBranchAccess(group.getProduct().getBranch().getId());
        ModifierOption option = ModifierOption.builder()
                .modifierGroup(group).optionName(request.getOptionName())
                .additionalPrice(request.getAdditionalPrice())
                .status(request.getStatus() != null ? request.getStatus() : "AVAILABLE").build();
        return mapper.toModifierOptionResponse(modifierOptionRepository.save(option));
    }

    @Override
    @Transactional
    public ModifierOptionResponse updateOption(String optionId, UpdateModifierOptionRequest request) {
        ModifierOption option = modifierOptionRepository.findById(optionId)
                .orElseThrow(() -> new AppException(ErrorCode.MODIFIER_OPTION_NOT_FOUND));
        branchGuard.validateBranchAccess(option.getModifierGroup().getProduct().getBranch().getId());
        option.setOptionName(request.getOptionName());
        option.setAdditionalPrice(request.getAdditionalPrice());
        if (request.getStatus() != null) option.setStatus(request.getStatus());
        return mapper.toModifierOptionResponse(modifierOptionRepository.save(option));
    }

    @Override
    @Transactional
    public void deleteOption(String optionId) {
        ModifierOption option = modifierOptionRepository.findById(optionId)
                .orElseThrow(() -> new AppException(ErrorCode.MODIFIER_OPTION_NOT_FOUND));
        branchGuard.validateBranchAccess(option.getModifierGroup().getProduct().getBranch().getId());
        modifierOptionRepository.delete(option);
    }

    private ModifierGroup loadGroup(String groupId) {
        return modifierGroupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.MODIFIER_GROUP_NOT_FOUND));
    }
}
