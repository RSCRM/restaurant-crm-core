package com.restaurant.crm.modules.erp.employee.service.interfaces;

import com.restaurant.crm.modules.erp.employee.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.employee.dto.response.EmployeeResponse;

public interface EmployeeService {
    EmployeeResponse addEmployee(CreateEmployeeRequest request);
}
