package com.restaurant.crm.modules.profile.service;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import com.restaurant.crm.modules.profile.dto.response.UserProfileResponse;
import com.restaurant.crm.modules.profile.entity.UserProfile;
import com.restaurant.crm.modules.profile.mapper.UserProfileMapper;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileService {

    UserRepository userRepository;
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyInfo() {
        String userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElse(null);

        return userProfileMapper.toUserProfileResponse(user, profile);
    }
}
