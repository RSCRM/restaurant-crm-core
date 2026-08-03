package com.restaurant.crm.modules.erp.organization;

import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.mapper.EmployeeMapper;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmployeeMapperTests {

    private final EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

    @Test
    public void toEmployeeBranchAssignmentResponse_MapsBranchAndManagerDetails() {
        OrganizationBranch branch = branch();
        branch.setManager(managerEmployee(branch));

        EmployeeBranchAssignmentResponse response =
                employeeMapper.toEmployeeBranchAssignmentResponse(branch);

        assertEquals("e0000000-0000-0000-0000-000000000005", response.getBranchId());
        assertEquals("BBQ Garden - Q3", response.getBranchName());
        assertEquals("123 Nguyen Dinh Chieu, Q3", response.getBranchAddress());
        assertEquals("028-1234-5678", response.getBranchPhone());
        assertEquals("ACTIVE", response.getBranchStatus());
        assertEquals("f0000000-0000-0000-0000-000000000003", response.getEmployeeId());
        assertEquals("f0000000-0000-0000-0000-000000000003", response.getManagerId());
        assertEquals("manager", response.getManagerName());
        assertEquals("0904000001", response.getPhone());
        assertEquals(EmployeeStatus.ACTIVE, response.getStatus());
        assertTrue(response.isEnabled());
    }

    private OrganizationBranch branch() {
        Organization organization = Organization.builder()
                .id("organization-id")
                .owner(User.builder().id("owner-id").build())
                .organizationName("Restaurant Group")
                .build();

        return OrganizationBranch.builder()
                .id("e0000000-0000-0000-0000-000000000005")
                .organization(organization)
                .branchName("BBQ Garden - Q3")
                .address("123 Nguyen Dinh Chieu, Q3")
                .phone("028-1234-5678")
                .status(OrganizationBranchStatus.ACTIVE)
                .build();
    }

    private Employee managerEmployee(OrganizationBranch branch) {
        return Employee.builder()
                .id("f0000000-0000-0000-0000-000000000003")
                .user(User.builder()
                        .id("c0000000-0000-0000-0000-000000000004")
                        .username("manager")
                        .email("manager@restaurant.com")
                        .enabled(true)
                        .status(UserStatus.ACTIVE)
                        .build())
                .orgRole(OrgRole.builder()
                        .id("r0000000-0000-0000-0000-000000000002")
                        .roleName(EmployeeConstants.MANAGER_ROLE_NAME)
                        .build())
                .branch(branch)
                .status(EmployeeStatus.ACTIVE)
                .email("manager@restaurant.com")
                .phone("0904000001")
                .startDate(LocalDate.of(2024, 6, 1))
                .build();
    }
}
