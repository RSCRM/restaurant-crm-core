package com.restaurant.crm.modules.erp.inventory.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateIngredientRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateIngredientRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.IngredientResponse;
import com.restaurant.crm.modules.erp.inventory.entity.Ingredient;
import com.restaurant.crm.modules.erp.inventory.mapper.IngredientMapper;
import com.restaurant.crm.modules.erp.inventory.repository.IngredientRepository;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.IngredientService;
import com.restaurant.crm.modules.erp.inventory.entity.IngredientCategory;
import com.restaurant.crm.modules.erp.inventory.repository.IngredientCategoryRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
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
public class IngredientServiceImpl implements IngredientService {

    IngredientRepository ingredientRepository;
    IngredientCategoryRepository ingredientCategoryRepository;
    OrganizationBranchRepository organizationBranchRepository;
    IngredientMapper ingredientMapper;

    @Override
    @Transactional
    public IngredientResponse createIngredient(
            CreateIngredientRequest request
    ) {

        OrganizationBranch branch =
            organizationBranchRepository.findById(request.getBranchId())
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND
                    )
                );

        IngredientCategory category =
            ingredientCategoryRepository.findById(
                request.getIngredientCategoryId()
                )
                .orElseThrow(() ->
                        new AppException(
                            ErrorCode.INGREDIENT_CATEGORY_NOT_FOUND
                        )
                );

        if (ingredientRepository.existsByBranchIdAndIngredientName(
                request.getBranchId(),
                request.getIngredientName()
        )) {
            throw new AppException(
                    ErrorCode.INGREDIENT_EXISTS
            );
        }

        Ingredient ingredient =
                ingredientMapper.toIngredient(request);

        ingredient.setBranch(branch);
        ingredient.setIngredientCategory(category);
        ingredient = ingredientRepository.save(ingredient);

        return ingredientMapper.toIngredientResponse(ingredient);
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientResponse getIngredientById(
            String id
    ) {

        Ingredient ingredient =
            ingredientRepository.findById(id)
                .orElseThrow(() ->
                    new AppException(
                        ErrorCode.INGREDIENT_NOT_FOUND
                    )
                );

        return ingredientMapper.toIngredientResponse(ingredient);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<IngredientResponse> getIngredientsByBranch(
            String branchId,
            int page,
            int size
    ) {

        Pageable pageable =
            PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
            );

        Page<Ingredient> ingredientPage =
            ingredientRepository.findByBranchId(
                branchId,
                pageable
            );


        return PagingResponse.<IngredientResponse>builder()
            .currentPage(page)
            .pageSize(ingredientPage.getSize())
            .totalPages(ingredientPage.getTotalPages())
            .totalElement(ingredientPage.getTotalElements())
            .data(
                ingredientPage.getContent()
                    .stream()
                    .map(ingredientMapper::toIngredientResponse)
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<IngredientResponse> searchIngredients(
        String branchId,
        String ingredientName,
        int page,
        int size
    ) {

        Pageable pageable =
            PageRequest.of(
                page - GlobalVariableConstant.PAGE_SIZE_INDEX,
                size
            );

        Page<Ingredient> ingredientPage =
            ingredientRepository
                .findByBranchIdAndIngredientNameContainingIgnoreCase(
                    branchId,
                    ingredientName,
                    pageable
                );

        return PagingResponse.<IngredientResponse>builder()
            .currentPage(page)
            .pageSize(ingredientPage.getSize())
            .totalPages(ingredientPage.getTotalPages())
            .totalElement(ingredientPage.getTotalElements())
            .data(
                ingredientPage.getContent()
                    .stream()
                    .map(ingredientMapper::toIngredientResponse)
                    .toList()
            )
            .build();
    }

    @Override
    @Transactional
    public IngredientResponse updateIngredient(
            String id,
            UpdateIngredientRequest request
    ) {

        Ingredient ingredient =
            ingredientRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(
                            ErrorCode.INGREDIENT_NOT_FOUND
                        )
                );

        if (request.getIngredientCategoryId() != null) {
            IngredientCategory category =
                ingredientCategoryRepository.findById(
                    request.getIngredientCategoryId()
                    )
                    .orElseThrow(() ->
                            new AppException(
                                ErrorCode.INGREDIENT_CATEGORY_NOT_FOUND
                            )
                    );
            ingredient.setIngredientCategory(category);
        }


        if (request.getIngredientName() != null
            && !request.getIngredientName()
            .equals(ingredient.getIngredientName())
            && ingredientRepository.existsByBranchIdAndIngredientName(
                ingredient.getBranch().getId(),
            request.getIngredientName()
        )) {

            throw new AppException(
                ErrorCode.INGREDIENT_EXISTS
            );
        }

        ingredientMapper.updateIngredient(
            request,
            ingredient
        );

        ingredient =
            ingredientRepository.save(ingredient);

        return ingredientMapper.toIngredientResponse(
            ingredient
        );
    }

    @Override
    @Transactional
    public void deleteIngredient(
            String id
    ) {

        Ingredient ingredient =
            ingredientRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(
                            ErrorCode.INGREDIENT_NOT_FOUND
                        )
                );

        ingredientRepository.delete(ingredient);
    }
}