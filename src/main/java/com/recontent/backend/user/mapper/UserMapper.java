package com.recontent.backend.user.mapper;

import com.recontent.backend.user.dto.UserResponse;
import com.recontent.backend.user.entity.RoleEntity;
import com.recontent.backend.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponse toResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAvatarUrl(),
                user.getStatus(),
                user.getRoles().stream()
                        .map(RoleEntity::getName)
                        .map(Enum::name)
                        .sorted(Comparator.naturalOrder())
                        .collect(Collectors.toCollection(LinkedHashSet::new)),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
