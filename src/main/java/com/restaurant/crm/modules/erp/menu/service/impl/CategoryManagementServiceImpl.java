package com.restaurant.crm.modules.erp.menu.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateCategoryRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateCategoryRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.CategoryResponse;
import com.restaurant.crm.modules.erp.menu.entity.Category;
import com.restaurant.crm.modules.erp.menu.mapper.MenuManagementMapper;
import com.restaurant.crm.modules.erp.menu.repository.CategoryRepository;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.menu.security.MenuBranchGuard;
import com.restaurant.crm.modules.erp.menu.service.interfaces.CategoryManagementService;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryManagementServiceImpl implements CategoryManagementService {

    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    OrganizationBranchRepository organizationBranchRepository;
    MenuBranchGuard branchGuard;
    MenuManagementMapper mapper;

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        branchGuard.validateBranchAccess(request.getBranchId());
        if (categoryRepository.existsByBranch_IdAndCategoryName(request.getBranchId(), request.getCategoryName())) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
        OrganizationBranch branch = organizationBranchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        Category category = Category.builder()
                .branch(branch)
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .build();
        return mapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(String id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        branchGuard.validateBranchAccess(category.getBranch().getId());
        if (!category.getCategoryName().equals(request.getCategoryName())
                && categoryRepository.existsByBranch_IdAndCategoryNameAndIdNot(category.getBranch().getId(), request.getCategoryName(), id)) {
            throw new AppException(ErrorCode.CATEGORY_NAME_EXISTS);
        }
        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setDisplayOrder(request.getDisplayOrder());
        return mapper.toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        branchGuard.validateBranchAccess(category.getBranch().getId());
        productRepository.detachCategory(id);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> listByBranch(String branchId) {
        branchGuard.validateBranchAccess(branchId);
        return categoryRepository.findByBranch_IdOrderByDisplayOrderAscCategoryNameAsc(branchId)
                .stream().map(mapper::toCategoryResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse get(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        branchGuard.validateBranchAccess(category.getBranch().getId());
        return mapper.toCategoryResponse(category);
    }
}
