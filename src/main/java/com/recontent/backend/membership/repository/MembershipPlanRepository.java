package com.recontent.backend.membership.repository;

import com.recontent.backend.membership.entity.MembershipPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlanEntity, UUID> {
    List<MembershipPlanEntity> findAllByActiveTrueOrderByPriceAsc();
}
