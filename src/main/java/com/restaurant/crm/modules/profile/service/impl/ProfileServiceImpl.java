package com.restaurant.crm.modules.profile.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.profile.dto.response.UserProfileResponse;
import com.restaurant.crm.modules.profile.dto.request.ProfileUpdateRequest;
import com.restaurant.crm.modules.profile.dto.request.StaffProfileUpdateRequest;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.profile.entity.UserProfile;
import com.restaurant.crm.modules.profile.mapper.UserProfileMapper;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
import com.restaurant.crm.modules.profile.service.interfaces.ProfileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceImpl implements ProfileService {

    UserRepository userRepository;
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;
    EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyInfo() {
        String userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElse(null);

        return toResponse(user, profile, AuthUtils.getEmployeeId());
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<UserProfileResponse> getAll(PagingRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                request.getPageSize(),
                PagingUtil.createSort(request)
        );

        Page<UserProfile> profilePage;
        if (isAdmin()) {
            profilePage = userProfileRepository.findAll(pageable);
        } else {
            profilePage = switch (AuthUtils.getDataScope()) {
                case ORGANIZATION -> userProfileRepository.findByOrganizationId(
                        AuthUtils.getOrganizationId(), pageable);
                case BRANCH -> userProfileRepository.findByBranchId(
                        AuthUtils.getBranchId(), pageable);
                case SELF -> userProfileRepository.findByUser_Id(
                        AuthUtils.getCurrentUserId(), pageable);
            };
        }

        Map<String, String> employeeIds = resolveEmployeeIds(profilePage.getContent());
        return PagingResponse.<UserProfileResponse>builder()
                .currentPage(request.getPage())
                .pageSize(profilePage.getSize())
                .totalPages(profilePage.getTotalPages())
                .totalElement(profilePage.getTotalElements())
                .data(profilePage.getContent().stream()
                        .map(profile -> toResponse(
                                profile.getUser(),
                                profile,
                                employeeIds.get(profile.getUser().getId())))
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getById(String profileId) {
        Optional<UserProfile> result;
        if (isAdmin()) {
            result = userProfileRepository.findById(profileId);
        } else {
            result = switch (AuthUtils.getDataScope()) {
                case ORGANIZATION -> userProfileRepository.findByIdAndOrganizationId(
                        profileId, AuthUtils.getOrganizationId());
                case BRANCH -> userProfileRepository.findByIdAndBranchId(
                        profileId, AuthUtils.getBranchId());
                case SELF -> userProfileRepository.findByUser_Id(AuthUtils.getCurrentUserId())
                        .filter(profile -> profile.getId().equals(profileId));
            };
        }
        UserProfile profile = result
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return toResponse(profile.getUser(), profile, resolveEmployeeId(profile.getUser()));
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyInfo(ProfileUpdateRequest request) {
        String userId = AuthUtils.getCurrentUserId();
        String employeeId = AuthUtils.getEmployeeId();
        if (employeeId != null && !employeeRepository.findByIdAndUserId(employeeId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND))
                .isProfileUpdateEnabled()) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseGet(() -> UserProfile.builder().user(user).build());

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }

        return toResponse(user, userProfileRepository.save(profile), AuthUtils.getEmployeeId());
    }

    @Override
    @Transactional
    public UserProfileResponse updateStaffInfo(
            String employeeId, StaffProfileUpdateRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        validateStaffScope(employee);

        User user = employee.getUser();
        UserProfile profile = userProfileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> UserProfile.builder().user(user).build());

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            if (!request.getPhone().equals(profile.getPhone())
                    && userProfileRepository.existsByPhone(request.getPhone())) {
                throw new AppException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
            }
            profile.setPhone(request.getPhone());
            employee.setPhone(request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(request.getEmail());
            employee.setEmail(request.getEmail());
            userRepository.save(user);
        }

        employeeRepository.save(employee);
        return toResponse(user, userProfileRepository.save(profile), employee.getId());
    }

    private void validateStaffScope(Employee employee) {
        if (isAdmin()) {
            return;
        }
        boolean allowed = employee.getBranch() != null && switch (AuthUtils.getDataScope()) {
            case ORGANIZATION -> employee.getBranch().getOrganization() != null
                    && AuthUtils.getOrganizationId().equals(
                            employee.getBranch().getOrganization().getId());
            case BRANCH -> AuthUtils.getBranchId().equals(employee.getBranch().getId());
            case SELF -> AuthUtils.getEmployeeId().equals(employee.getId());
        };
        if (!allowed) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }

    private boolean isAdmin() {
        return AuthUtils.hasRole("ADMIN");
    }

    private String resolveEmployeeId(User user) {
        if (isAdmin()) {
            return employeeRepository.findFirstByUser_Id(user.getId()).map(Employee::getId).orElse(null);
        }
        if (AuthUtils.getBranchId() != null) {
            return employeeRepository.findFirstByUser_IdAndBranch_Id(
                    user.getId(), AuthUtils.getBranchId()).map(Employee::getId).orElse(null);
        }
        return employeeRepository.findFirstByUser_IdAndBranch_Organization_Id(
                user.getId(), AuthUtils.getOrganizationId())
                .map(Employee::getId).orElse(null);
    }

    private Map<String, String> resolveEmployeeIds(List<UserProfile> profiles) {
        List<String> userIds = profiles.stream().map(profile -> profile.getUser().getId()).toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<Employee> employees;
        if (isAdmin()) {
            employees = employeeRepository.findByUser_IdIn(userIds);
        } else {
            employees = switch (AuthUtils.getDataScope()) {
                case ORGANIZATION -> employeeRepository
                        .findByUser_IdInAndBranch_Organization_Id(
                                userIds, AuthUtils.getOrganizationId());
                case BRANCH -> employeeRepository.findByUser_IdInAndBranch_Id(
                        userIds, AuthUtils.getBranchId());
                case SELF -> employeeRepository.findByUser_IdIn(List.of(
                        AuthUtils.getCurrentUserId()));
            };
        }
        return employees.stream().collect(Collectors.toMap(
                employee -> employee.getUser().getId(),
                Employee::getId,
                (first, ignored) -> first));
    }

    private UserProfileResponse toResponse(User user, UserProfile profile, String employeeId) {
        UserProfileResponse response = userProfileMapper.toUserProfileResponse(user, profile);
        response.setEmployeeId(employeeId);
        return response;
    }
}
