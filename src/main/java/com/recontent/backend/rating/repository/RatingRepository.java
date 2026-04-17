package com.recontent.backend.rating.repository;

import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.rating.entity.RatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<RatingEntity, UUID> {
    List<RatingEntity> findAllByUser_IdOrderByUpdatedAtDesc(UUID userId);

    Optional<RatingEntity> findByUser_IdAndMediaTypeAndMediaId(UUID userId, MediaType mediaType, Long mediaId);
}
