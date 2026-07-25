package com.restaurant.crm.modules.erp.employee.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.employee.constants.EmployeeAccountConstants;
import com.restaurant.crm.modules.erp.employee.constants.permission.OrgPermissionConstants;
import com.restaurant.crm.modules.erp.employee.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.employee.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.employee.mapper.EmployeeMapper;
import com.restaurant.crm.modules.erp.employee.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.employee.service.interfaces.EmployeeAuthorizationService;
import com.restaurant.crm.modules.erp.employee.service.interfaces.EmployeeService;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    EmployeeAuthorizationService employeeAuthorizationService;
    UserRepository userRepository;
    RoleRepository roleRepository;
    EmployeeRepository employeeRepository;
    OrganizationBranchRepository organizationBranchRepository;
    OrgRoleRepository orgRoleRepository;
    PasswordEncoder passwordEncoder;
    EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        employeeAuthorizationService.authorize(request.getBranchId(), OrgPermissionConstants.EMPLOYEE_ADD);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        OrganizationBranch branch = organizationBranchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        OrgRole orgRole = null;
        if (request.getOrgRoleId() != null && !request.getOrgRoleId().isBlank()) {
            orgRole = orgRoleRepository.findById(request.getOrgRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ORG_ROLE_NOT_FOUND));
        }

        Role userRole = roleRepository.findByRoleName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(EmployeeAccountConstants.DEFAULT_PASSWORD))
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        user = userRepository.save(user);

        Employee employee = Employee.builder()
                .user(user)
                .branch(branch)
                .orgRole(orgRole)
                .email(request.getEmail())
                .phone(request.getPhone())
                .startDate(request.getStartDate())
                .salary(request.getSalary())
                .status(EmployeeStatus.ACTIVE)
                .build();
        employee = employeeRepository.save(employee);

        return employeeMapper.toEmployeeResponse(employee);
    }
}
