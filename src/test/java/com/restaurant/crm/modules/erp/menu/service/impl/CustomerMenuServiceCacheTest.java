package com.restaurant.crm.modules.erp.menu.service.impl;

import com.restaurant.crm.modules.erp.menu.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.constants.CustomerMenuConstants;
import com.restaurant.crm.modules.erp.menu.entity.Category;
import com.restaurant.crm.modules.erp.menu.mapper.CustomerMenuMapper;
import com.restaurant.crm.modules.erp.menu.repository.ModifierGroupRepository;
import com.restaurant.crm.modules.erp.menu.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.entity.Product;
import com.restaurant.crm.modules.erp.menu.repository.ProductRepository;
import com.restaurant.crm.modules.erp.menu.service.interfaces.CustomerMenuService;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the per-branch menu cache (uc-c-04): a second call within the cache is served from the
 * cache and does not touch the repositories.
 */
@SpringJUnitConfig(CustomerMenuServiceCacheTest.CacheTestConfig.class)
class CustomerMenuServiceCacheTest {

    private static final String BRANCH = "branch-1";

    @Autowired CustomerMenuService service;
    @Autowired ProductRepository productRepository;
    @Autowired ComboRepository comboRepository;
    @Autowired ModifierGroupRepository modifierGroupRepository;

    @Test
    void secondCallIsServedFromCacheWithoutHittingRepositories() {
        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn(BRANCH);
            when(productRepository.findByBranchIdAndDeletedAtIsNullOrderByCategoryIdAscProductNameAsc(BRANCH))
                    .thenReturn(List.of(Product.builder()
                            .id("p1").branch(OrganizationBranch.builder().id(BRANCH).build())
                            .category(Category.builder().id("cat-1").build()).productName("Apple")
                            .price(new BigDecimal("10.00")).status("AVAILABLE").requiresPreparation(true).build()));
            when(comboRepository.findByBranchIdOrderByComboNameAsc(BRANCH)).thenReturn(List.of());
            when(modifierGroupRepository.findByProduct_Branch_IdOrderByGroupNameAsc(BRANCH)).thenReturn(List.of());

            service.getMenu();
            service.getMenu(); // second call → cache hit

            verify(productRepository, times(1))
                    .findByBranchIdAndDeletedAtIsNullOrderByCategoryIdAscProductNameAsc(BRANCH);
            verify(comboRepository, times(1)).findByBranchIdOrderByComboNameAsc(BRANCH);
            verify(modifierGroupRepository, times(1))
                    .findByProduct_Branch_IdOrderByGroupNameAsc(BRANCH);
        }
    }

    @EnableCaching
    @Configuration
    static class CacheTestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CustomerMenuConstants.CACHE_NAME);
        }

        @Bean
        ProductRepository productRepository() {
            return mock(ProductRepository.class);
        }

        @Bean
        ComboRepository comboRepository() {
            return mock(ComboRepository.class);
        }

        @Bean
        ModifierGroupRepository modifierGroupRepository() {
            return mock(ModifierGroupRepository.class);
        }

        @Bean
        ModifierOptionRepository modifierOptionRepository() {
            return mock(ModifierOptionRepository.class);
        }

        @Bean
        CustomerMenuMapper customerMenuMapper() {
            return Mappers.getMapper(CustomerMenuMapper.class);
        }

        @Bean
        CustomerMenuServiceImpl customerMenuService(ProductRepository productRepository,
                                                    ComboRepository comboRepository,
                                                    ModifierGroupRepository modifierGroupRepository,
                                                    ModifierOptionRepository modifierOptionRepository,
                                                    CustomerMenuMapper customerMenuMapper) {
            return new CustomerMenuServiceImpl(productRepository, comboRepository,
                    modifierGroupRepository, modifierOptionRepository, customerMenuMapper);
        }
    }
}
