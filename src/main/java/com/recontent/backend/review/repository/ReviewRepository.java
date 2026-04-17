package com.recontent.backend.review.repository;

import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.review.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    List<ReviewEntity> findAllByMediaTypeAndMediaIdOrderByCreatedAtDesc(MediaType mediaType, Long mediaId);
}
