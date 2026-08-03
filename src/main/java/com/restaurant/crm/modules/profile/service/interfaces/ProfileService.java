package com.restaurant.crm.modules.profile.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.profile.dto.response.UserProfileResponse;
import com.restaurant.crm.modules.profile.dto.request.ProfileUpdateRequest;
import com.restaurant.crm.modules.profile.dto.request.StaffProfileUpdateRequest;

public interface ProfileService {
    UserProfileResponse getMyInfo();

    PagingResponse<UserProfileResponse> getAll(PagingRequest request);

    UserProfileResponse getById(String profileId);

    UserProfileResponse updateMyInfo(ProfileUpdateRequest request);

    UserProfileResponse updateStaffInfo(String employeeId, StaffProfileUpdateRequest request);
}
