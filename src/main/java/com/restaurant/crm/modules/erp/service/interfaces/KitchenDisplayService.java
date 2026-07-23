package com.restaurant.crm.modules.erp.service.interfaces;

import com.restaurant.crm.modules.erp.dto.response.KitchenOrderItemResponse;

import java.util.List;

public interface KitchenDisplayService {

    /**
     * FIFO waiting list for the kitchen display of a branch (uc-scf-01).
     * Returns PENDING and IN_PROGRESS items only, ordered per BR-RES-ORD-04.
     *
     * @param branchId branch whose queue to load (NFR-07 isolation)
     */
    List<KitchenOrderItemResponse> getKitchenQueue(String branchId);
}
