package com.recontent.backend.membership.repository;

import com.recontent.backend.common.enums.MembershipStatus;
import com.recontent.backend.membership.entity.UserMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserMembershipRepository extends JpaRepository<UserMembershipEntity, UUID> {
    Optional<UserMembershipEntity> findFirstByUser_IdAndStatusOrderByStartDateDesc(UUID userId, MembershipStatus status);
}
