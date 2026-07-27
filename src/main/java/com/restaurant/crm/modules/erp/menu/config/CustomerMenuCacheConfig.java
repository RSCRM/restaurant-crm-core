package com.restaurant.crm.modules.erp.menu.config;

import com.restaurant.crm.modules.erp.menu.constants.CustomerMenuConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enables caching for the customer menu (uc-c-04) with an explicit in-memory manager.
 * <p>
 * The explicit {@link ConcurrentMapCacheManager} is deliberate: {@code spring-boot-starter-data-redis}
 * is on the classpath, so without this the default cache type would be Redis and require a running
 * server even for tests.
 * <p>
 * TODO(uc-c-04): {@link ConcurrentMapCacheManager} does NOT honor
 * {@link CustomerMenuConstants#CACHE_TTL_SECONDS} — entries never expire on their own. Until either
 * (a) uc-m-* menu-edit endpoints add {@code @CacheEvict}, or (b) this moves to a TTL-capable manager
 * (Caffeine/Redis, needs a new dependency + NFR-03 multi-instance decision), a just-sold-out item can
 * stay cached until the app restarts. Short TTL is the intended mitigation once a real manager exists.
 */
@Configuration
@EnableCaching
public class CustomerMenuCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(CustomerMenuConstants.CACHE_NAME);
    }
}
