package com.recontent.backend.user.repository;

import com.recontent.backend.common.enums.RoleName;
import com.recontent.backend.user.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(RoleName name);
}
