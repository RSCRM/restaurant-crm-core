package com.restaurant.crm.modules.identity.service.impl;

import com.nimbusds.jwt.SignedJWT;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.redis.RedisBlacklistRepository;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import com.restaurant.crm.modules.identity.dto.request.AuthenticationRequest;
import com.restaurant.crm.modules.identity.dto.response.AuthenticationResponse;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmployeeRepository employeeRepository;
    @Mock OrganizationRepository organizationRepository;
    @Mock OrganizationBranchRepository organizationBranchRepository;
    @Mock OrgRoleRepository orgRoleRepository;
    @Mock RoleRepository roleRepository;
    @Mock RedisBlacklistRepository redisBlacklistRepository;
    @InjectMocks AuthenticationServiceImpl authenticationService;

    @Test
    void authenticateReturnsEachOwnerContextOnce() {
        User user = User.builder()
                .id("user-1")
                .email("owner@example.com")
                .password("encoded")
                .roles(Set.of())
                .build();
        Organization organization = Organization.builder()
                .id("org-1")
                .organizationName("Phở Việt Chain")
                .build();
        Employee ownerEmployee = Employee.builder()
                .id("owner-employee-1")
                .user(user)
                .organization(organization)
                .orgRole(OrgRole.builder().roleName("OWNER").build())
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(employeeRepository.findByUserIdAndStatus("user-1", EmployeeStatus.ACTIVE))
                .thenReturn(List.of(ownerEmployee));
        lenient().when(employeeRepository.findByUser_IdAndOrgRole_RoleName("user-1", "OWNER"))
                .thenReturn(List.of(ownerEmployee));
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY",
                "0123456789012345678901234567890123456789012345678901234567890123");

        AuthenticationResponse response = authenticationService.authenticate(
                AuthenticationRequest.builder().email("owner@example.com").password("password").build());

        assertEquals(1, response.getContexts().size());
        verify(employeeRepository, never()).findByUser_IdAndOrgRole_RoleName("user-1", "OWNER");
    }

    @Test
    void ownerContextContainsRolePermissions() throws Exception {
        OrgPermission profileView = OrgPermission.builder()
                .permissionName("PROFILE_VIEW")
                .build();
        OrgRole owner = OrgRole.builder()
                .roleName("OWNER")
                .orgPermissions(Set.of(profileView))
                .build();
        Employee ownerEmployee = Employee.builder()
                .id("owner-1")
                .email("owner@example.com")
                .organization(Organization.builder().id("org-1").build())
                .orgRole(owner)
                .build();
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY",
                "0123456789012345678901234567890123456789012345678901234567890123");

        String token = ReflectionTestUtils.invokeMethod(
                authenticationService,
                "generateContextToken",
                "user-1",
                ownerEmployee,
                "OWNER",
                OrgDataScope.ORGANIZATION,
                Set.of("PROFILE_VIEW"));
        SignedJWT jwt = SignedJWT.parse(token);

        assertEquals("org-1", jwt.getJWTClaimsSet()
                .getStringClaim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID));
        assertNull(jwt.getJWTClaimsSet().getClaim(JwtClaimSetConstant.CLAIM_BRANCH_ID));
        assertEquals(
                Set.of("PROFILE_VIEW"),
                Set.copyOf(jwt.getJWTClaimsSet()
                        .getStringListClaim(JwtClaimSetConstant.CLAIM_PERMISSION)));
    }
}
