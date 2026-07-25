package com.restaurant.crm.modules.erp.employee.service.interfaces;

public interface EmployeeAuthorizationService {
    void authorize(String targetBranchId, String requiredOrgPermission);
}
