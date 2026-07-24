package com.restaurant.crm.modules.erp.inventory.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateIngredientCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateIngredientCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.IngredientCategoryResponse;
import com.restaurant.crm.modules.erp.inventory.entity.IngredientCategory;
import com.restaurant.crm.modules.erp.inventory.mapper.IngredientCategoryMapper;
import com.restaurant.crm.modules.erp.inventory.repository.IngredientCategoryRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.IngredientCategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IngredientCategoryServiceImpl implements IngredientCategoryService {

    IngredientCategoryRepository ingredientCategoryRepository;

    OrganizationBranchRepository organizationBranchRepository;

    IngredientCategoryMapper ingredientCategoryMapper;

    @Override
    @Transactional
    public IngredientCategoryResponse createIngredientCategory(
            CreateIngredientCategoryRequest request
    ) {

        OrganizationBranch branch = organizationBranchRepository.findById(request.getBranchId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        if (ingredientCategoryRepository.existsByBranchIdAndCategoryName(
                request.getBranchId(),
                request.getCategoryName())) {

            throw new AppException(ErrorCode.INGREDIENT_CATEGORY_EXISTS);
        }

        IngredientCategory ingredientCategory =
                ingredientCategoryMapper.toIngredientCategory(request);

        ingredientCategory.setBranch(branch);

        ingredientCategory = ingredientCategoryRepository.save(ingredientCategory);

        return ingredientCategoryMapper.toIngredientCategoryResponse(
                ingredientCategory
        );
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientCategoryResponse getIngredientCategoryById(
            String id
    ) {

        IngredientCategory ingredientCategory =
                ingredientCategoryRepository.findById(id)
                        .orElseThrow(() ->
                                new AppException(ErrorCode.INGREDIENT_CATEGORY_NOT_FOUND));

        return ingredientCategoryMapper.toIngredientCategoryResponse(
                ingredientCategory
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<IngredientCategoryResponse> getIngredientCategories(
            String branchId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
        );

        Page<IngredientCategory> ingredientCategoryPage =
                ingredientCategoryRepository.findByBranchId(
                        branchId,
                        pageable
                );

        return PagingResponse.<IngredientCategoryResponse>builder()
                .currentPage(page)
                .pageSize(ingredientCategoryPage.getSize())
                .totalPages(ingredientCategoryPage.getTotalPages())
                .totalElement(ingredientCategoryPage.getTotalElements())
                .data(
                        ingredientCategoryPage.getContent()
                                .stream()
                                .map(ingredientCategoryMapper::toIngredientCategoryResponse)
                                .toList()
                )
                .build();
    }

    @Override
    @Transactional
    public IngredientCategoryResponse updateIngredientCategory(
            String id,
            UpdateIngredientCategoryRequest request
    ) {

        IngredientCategory ingredientCategory =
                ingredientCategoryRepository.findById(id)
                        .orElseThrow(() ->
                                new AppException(ErrorCode.INGREDIENT_CATEGORY_NOT_FOUND));

        if (request.getCategoryName() != null
                && !request.getCategoryName().equals(ingredientCategory.getCategoryName())
                && ingredientCategoryRepository.existsByBranchIdAndCategoryName(
                ingredientCategory.getBranch().getId(),
                request.getCategoryName())) {

            throw new AppException(ErrorCode.INGREDIENT_CATEGORY_EXISTS);
        }

        ingredientCategoryMapper.updateIngredientCategory(
                request,
                ingredientCategory
        );

        ingredientCategory = ingredientCategoryRepository.save(
                ingredientCategory
        );

        return ingredientCategoryMapper.toIngredientCategoryResponse(
                ingredientCategory
        );
    }

    @Override
    @Transactional
    public void deleteIngredientCategory(
            String id
    ) {

        IngredientCategory ingredientCategory =
                ingredientCategoryRepository.findById(id)
                        .orElseThrow(() ->
                                new AppException(ErrorCode.INGREDIENT_CATEGORY_NOT_FOUND));

        ingredientCategoryRepository.delete(ingredientCategory);
    }
}