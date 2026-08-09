package com.restaurant.crm.modules.profile.repository;

import com.restaurant.crm.modules.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    Optional<UserProfile> findByUser_Id(String userId);
    Page<UserProfile> findByUser_Id(String userId, Pageable pageable);
    boolean existsByPhone(String phone);
    List<UserProfile> findByUser_IdIn(Collection<String> userIds);

    @Query("""
            select distinct p from UserProfile p
            where p.user.id in (
                select e.user.id from Employee e where e.branch.organization.id = :organizationId
            ) or p.user.id in (
                select e.user.id from Employee e where e.organization.id = :organizationId and e.orgRole.roleName = 'OWNER'
            )
            """)
    Page<UserProfile> findByOrganizationId(String organizationId, Pageable pageable);

    @Query("""
            select p from UserProfile p
            where p.user.id in (
                select e.user.id from Employee e where e.branch.id = :branchId
            )
            """)
    Page<UserProfile> findByBranchId(String branchId, Pageable pageable);

    @Query("""
            select distinct p from UserProfile p
            where p.id = :profileId and (
                p.user.id in (
                    select e.user.id from Employee e where e.branch.organization.id = :organizationId
                ) or p.user.id in (
                    select e.user.id from Employee e where e.organization.id = :organizationId and e.orgRole.roleName = 'OWNER'
                )
            )
            """)
    Optional<UserProfile> findByIdAndOrganizationId(String profileId, String organizationId);

    @Query("""
            select p from UserProfile p
            where p.id = :profileId and p.user.id in (
                select e.user.id from Employee e where e.branch.id = :branchId
            )
            """)
    Optional<UserProfile> findByIdAndBranchId(String profileId, String branchId);
}
