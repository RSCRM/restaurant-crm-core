package com.restaurant.crm.modules.erp.menu.constants;

/**
 * Constants for the customer digital-menu browse feature (uc-c-04).
 */
public final class CustomerMenuConstants {

    /** The only product/combo/option status that counts as orderable. */
    public static final String STATUS_AVAILABLE = "AVAILABLE";

    /** Spring Cache name for the per-branch menu. */
    public static final String CACHE_NAME = "customerMenu";

    /**
     * Menu cache TTL in seconds. Kept short (60s) because no menu-management API exists yet to
     * {@code @CacheEvict} on change — a short TTL is the only way a just-sold-out item refreshes.
     * TODO(uc-c-04): switch to explicit @CacheEvict once uc-m-* ships menu edit endpoints.
     */
    public static final long CACHE_TTL_SECONDS = 60L;

    private CustomerMenuConstants() {}
}
