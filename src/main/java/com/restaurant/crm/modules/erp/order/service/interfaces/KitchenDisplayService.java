package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.response.KitchenOrderItemResponse;

import java.util.List;

public interface KitchenDisplayService {

    /**
     * FIFO waiting list for the kitchen display of the caller's branch (uc-scf-01).
     * Returns PENDING and IN_PROGRESS items only, ordered per BR-RES-ORD-04.
     * The branch is taken from the authenticated user, never from the client (NFR-07).
     */
    List<KitchenOrderItemResponse> getKitchenQueue();
}
