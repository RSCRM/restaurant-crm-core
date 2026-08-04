package com.restaurant.crm.modules.erp.menu.service.impl;

import com.restaurant.crm.common.cloudinary.CloudinaryImageService;
import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.erp.menu.constants.MenuStorageConstants;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateProductRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.ProductSearchRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateProductRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.ProductResponse;
import com.restaurant.crm.modules.erp.menu.entity.Category;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.menu.mapper.MenuManagementMapper;
import com.restaurant.crm.modules.erp.menu.repository.CategoryRepository;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.menu.security.MenuBranchGuard;
import com.restaurant.crm.modules.erp.menu.service.interfaces.ProductManagementService;
import com.restaurant.crm.modules.erp.menu.specification.ProductSpecification;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductManagementServiceImpl implements ProductManagementService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    OrganizationBranchRepository organizationBranchRepository;
    CloudinaryImageService cloudinaryImageService;
    MenuBranchGuard branchGuard;
    MenuManagementMapper mapper;

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request, MultipartFile image) {
        branchGuard.validateBranchAccess(request.getBranchId());
        if (productRepository.existsByBranch_IdAndProductName(request.getBranchId(), request.getProductName())) {
            throw new AppException(ErrorCode.PRODUCT_NAME_EXISTS);
        }
        OrganizationBranch branch = organizationBranchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        Category category = resolveCategory(request.getCategoryId(), request.getBranchId());

        Product product = Product.builder()
                .branch(branch).category(category)
                .productName(request.getProductName()).description(request.getDescription())
                .price(request.getPrice())
                .status(request.getStatus() != null ? request.getStatus() : "AVAILABLE")
                .requiresPreparation(request.getRequiresPreparation() != null ? request.getRequiresPreparation() : Boolean.TRUE)
                .build();
        product = productRepository.save(product);

        if (image != null && !image.isEmpty()) {
            product.setImageUrl(storeImage(MenuStorageConstants.productImagePublicId(product.getId()), image));
            product = productRepository.save(product);
        }
        return mapper.toProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(String id, UpdateProductRequest request, MultipartFile image) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        branchGuard.validateBranchAccess(product.getBranch().getId());
        if (!product.getProductName().equals(request.getProductName())
                && productRepository.existsByBranch_IdAndProductNameAndIdNot(product.getBranch().getId(), request.getProductName(), id)) {
            throw new AppException(ErrorCode.PRODUCT_NAME_EXISTS);
        }
        product.setCategory(resolveCategory(request.getCategoryId(), product.getBranch().getId()));
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getStatus() != null) product.setStatus(request.getStatus());
        if (request.getRequiresPreparation() != null) product.setRequiresPreparation(request.getRequiresPreparation());
        if (image != null && !image.isEmpty()) {
            product.setImageUrl(storeImage(MenuStorageConstants.productImagePublicId(product.getId()), image));
        }
        return mapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(String id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        branchGuard.validateBranchAccess(product.getBranch().getId());
        product.setDeletedAt(Instant.now());
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> listByBranch(String branchId) {
        branchGuard.validateBranchAccess(branchId);
        return productRepository.findByBranchIdAndDeletedAtIsNullOrderByCategoryIdAscProductNameAsc(branchId)
                .stream().map(mapper::toProductResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse get(String id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        branchGuard.validateBranchAccess(product.getBranch().getId());
        return mapper.toProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<ProductResponse> searchProducts(ProductSearchRequest searchRequest, PagingRequest pagingRequest) {
        Pageable pageable = PageRequest.of(
                pagingRequest.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                pagingRequest.getPageSize(),
                PagingUtil.createSort(pagingRequest)
        );

        // Resolve data scope
        String dataScopeOrgId = null;
        String dataScopeBranchId = null;
        if (!AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)) {
            OrgDataScope dataScope = AuthUtils.getDataScope();
            switch (dataScope) {
                case BRANCH, SELF -> dataScopeBranchId = AuthUtils.getBranchId();
                case ORGANIZATION -> dataScopeOrgId = AuthUtils.getOrganizationId();
            }
        }

        Page<Product> productPage = productRepository.findAll(
                ProductSpecification.build(searchRequest, dataScopeOrgId, dataScopeBranchId), pageable);
        return PagingResponse.<ProductResponse>builder()
                .currentPage(pagingRequest.getPage())
                .pageSize(productPage.getSize())
                .totalPages(productPage.getTotalPages())
                .totalElement(productPage.getTotalElements())
                .data(productPage.getContent().stream()
                        .map(mapper::toProductResponse)
                        .toList())
                .build();
    }

    private Category resolveCategory(String categoryId, String branchId) {
        if (categoryId == null || categoryId.isBlank()) return null;
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        if (!category.getBranch().getId().equals(branchId)) {
            throw new AppException(ErrorCode.MENU_CATEGORY_BRANCH_MISMATCH);
        }
        return category;
    }

    private String storeImage(String publicId, MultipartFile image) {
        cloudinaryImageService.upload(publicId, image);
        return cloudinaryImageService.getUrl(publicId, null);
    }
}
