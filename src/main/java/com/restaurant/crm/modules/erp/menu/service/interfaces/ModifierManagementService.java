package com.restaurant.crm.modules.erp.menu.service.interfaces;

import com.restaurant.crm.modules.erp.menu.dto.request.CreateModifierGroupRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateModifierOptionRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateModifierGroupRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateModifierOptionRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.ModifierGroupResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ModifierOptionResponse;

import java.util.List;

public interface ModifierManagementService {
    ModifierGroupResponse createGroup(String productId, CreateModifierGroupRequest request);
    ModifierGroupResponse updateGroup(String groupId, UpdateModifierGroupRequest request);
    void deleteGroup(String groupId);
    List<ModifierGroupResponse> listGroupsByProduct(String productId);
    ModifierOptionResponse createOption(String groupId, CreateModifierOptionRequest request);
    ModifierOptionResponse updateOption(String optionId, UpdateModifierOptionRequest request);
    void deleteOption(String optionId);
}
