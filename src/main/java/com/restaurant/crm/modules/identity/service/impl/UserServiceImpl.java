package com.restaurant.crm.modules.identity.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.dto.request.UserCreationRequest;
import com.restaurant.crm.modules.identity.dto.request.UserRolesUpdateRequest;
import com.restaurant.crm.modules.identity.dto.response.UserResponse;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.mapper.UserMapper;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.service.interfaces.UserService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository usersRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse create(UserCreationRequest request) {
        validateUsernameExisted(request.getUsername());
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        
        Role userRole = roleRepository.findByRoleName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoles(new HashSet<>(Set.of(userRole)));

        User userSaved = usersRepository.save(user);

        return userMapper.toUserResponse(userSaved);
    }

    @Override
    public PagingResponse<UserResponse> getUsers(PagingRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                request.getPageSize(),
                PagingUtil.createSort(request)
        );

        Page<User> userPage = usersRepository.findAll(pageable);

        return PagingResponse.<UserResponse>builder()
                .currentPage(request.getPage())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalElement(userPage.getTotalElements())
                .data(userPage.getContent().stream()
                        .map(userMapper::toUserResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public UserResponse updateRoles(String userId, UserRolesUpdateRequest request) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHENTICATED));

        List<Role> roles = roleRepository.findAllById(request.getRoleIds());
        user.setRoles(new HashSet<>(roles));

        User updated = usersRepository.save(user);
        return userMapper.toUserResponse(updated);
    }

    @Override
    public UserResponse getMyInfo() {
        String userId = AuthUtils.getCurrentUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }

        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHENTICATED));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void deleteById(String userId) {
        usersRepository.deleteById(userId);
    }


    private void validateUsernameExisted(String username) {
        if (usersRepository.existsByUsername(username)) {
            // TODO: bổ sung ErrorCode riêng cho user khi mở rộng ErrorCode
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }
    }
}
