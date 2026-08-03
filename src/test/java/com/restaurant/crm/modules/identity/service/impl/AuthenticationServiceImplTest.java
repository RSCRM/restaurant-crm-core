package com.restaurant.crm.modules.identity.service.impl;

import com.nimbusds.jwt.SignedJWT;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.redis.RedisBlacklistRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmployeeRepository employeeRepository;
    @Mock OrganizationRepository organizationRepository;
    @Mock OrgRoleRepository orgRoleRepository;
    @Mock RoleRepository roleRepository;
    @Mock RedisBlacklistRepository redisBlacklistRepository;
    @InjectMocks AuthenticationServiceImpl authenticationService;

    @Test
    void ownerContextContainsRolePermissions() throws Exception {
        OrgPermission profileView = OrgPermission.builder()
                .permissionName("PROFILE_VIEW")
                .build();
        OrgRole owner = OrgRole.builder()
                .roleName("OWNER")
                .orgPermissions(Set.of(profileView))
                .build();
        when(orgRoleRepository.findByRoleName("OWNER")).thenReturn(Optional.of(owner));
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY",
                "0123456789012345678901234567890123456789012345678901234567890123");

        String token = ReflectionTestUtils.invokeMethod(
                authenticationService, "generateOwnerContextToken", "owner-1", "org-1");
        SignedJWT jwt = SignedJWT.parse(token);

        assertEquals(
                Set.of("PROFILE_VIEW"),
                Set.copyOf(jwt.getJWTClaimsSet()
                        .getStringListClaim(JwtClaimSetConstant.CLAIM_PERMISSION)));
    }

    @Test
    void authenticate_ownerWithMultipleOrganizationsReturnsAllOwnerContexts() {
        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY",
                "0123456789012345678901234567890123456789012345678901234567890123");

        User owner = User.builder()
                .id("owner-1")
                .email("owner@example.com")
                .password("encoded-password")
                .build();
        Organization orgA = Organization.builder()
                .id("org-a")
                .organizationName("Organization A")
                .owner(owner)
                .build();
        Organization orgB = Organization.builder()
                .id("org-b")
                .organizationName("Organization B")
                .owner(owner)
                .build();

        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
        when(employeeRepository.findByUserIdAndStatus("owner-1", EmployeeStatus.ACTIVE))
                .thenReturn(List.of());
        when(organizationRepository.findAllByOwnerId("owner-1"))
                .thenReturn(List.of(orgA, orgB));

        AuthenticationResponse response = authenticationService.authenticate(
                AuthenticationRequest.builder()
                        .email("owner@example.com")
                        .password("123456")
                        .build()
        );

        assertEquals(2, response.getContexts().size());
        assertEquals(Set.of("org-a", "org-b"), Set.copyOf(response.getContexts().stream()
                .map(context -> context.getOrganizationId())
                .toList()));
        verify(organizationRepository).findAllByOwnerId("owner-1");
    }
}
