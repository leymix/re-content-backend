package com.recontent.backend.favorite.repository;

import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.favorite.entity.FavoriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<FavoriteEntity, UUID> {
    List<FavoriteEntity> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<FavoriteEntity> findByUser_IdAndMediaTypeAndMediaId(UUID userId, MediaType mediaType, Long mediaId);
}
