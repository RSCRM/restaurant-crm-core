package com.restaurant.crm.modules.erp.menu.config;

import com.restaurant.crm.modules.erp.menu.constants.CustomerMenuConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableCaching
public class CustomerMenuCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CustomerMenuConstants.CACHE_NAME);
    }
}
