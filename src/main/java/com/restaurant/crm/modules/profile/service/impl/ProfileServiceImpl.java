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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElse(null);

        return userProfileMapper.toUserProfileResponse(user, profile);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<UserProfileResponse> getAll(PagingRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                request.getPageSize(),
                PagingUtil.createSort(request)
        );

        Page<UserProfile> profilePage = userProfileRepository.findAll(pageable);

        return PagingResponse.<UserProfileResponse>builder()
                .currentPage(request.getPage())
                .pageSize(profilePage.getSize())
                .totalPages(profilePage.getTotalPages())
                .totalElement(profilePage.getTotalElements())
                .data(profilePage.getContent().stream()
                        .map(profile -> userProfileMapper.toUserProfileResponse(profile.getUser(), profile))
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getById(String profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return userProfileMapper.toUserProfileResponse(profile.getUser(), profile);
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

        return userProfileMapper.toUserProfileResponse(user, userProfileRepository.save(profile));
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
        return userProfileMapper.toUserProfileResponse(user, userProfileRepository.save(profile));
    }

    private void validateStaffScope(Employee employee) {
        String actorEmployeeId = AuthUtils.getEmployeeId();
        if (actorEmployeeId == null) {
            if (employee.getBranch() == null
                    || employee.getBranch().getOrganization() == null
                    || !AuthUtils.getCurrentUserId().equals(
                            employee.getBranch().getOrganization().getOwner().getId())) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
        } else if (employee.getBranch() == null
                || !AuthUtils.getBranchId().equals(employee.getBranch().getId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }
}
