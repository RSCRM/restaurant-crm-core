package com.restaurant.crm.modules.identity.service.impl;

import com.nimbusds.jwt.SignedJWT;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.redis.RedisBlacklistRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationRepository;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                authenticationService, "generateOwnerContextToken", "owner-1", "org-1", "branch-1");
        SignedJWT jwt = SignedJWT.parse(token);

        assertEquals("branch-1", jwt.getJWTClaimsSet()
                .getStringClaim(JwtClaimSetConstant.CLAIM_BRANCH_ID));
        assertEquals(
                Set.of("PROFILE_VIEW"),
                Set.copyOf(jwt.getJWTClaimsSet()
                        .getStringListClaim(JwtClaimSetConstant.CLAIM_PERMISSION)));
    }
}
