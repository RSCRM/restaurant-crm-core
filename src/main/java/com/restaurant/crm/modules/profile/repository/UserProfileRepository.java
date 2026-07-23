package com.restaurant.crm.modules.profile.repository;

import com.restaurant.crm.modules.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByUser_Id(String userId);
    boolean existsByPhone(String phone);
}
