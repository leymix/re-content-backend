package com.recontent.backend.review.mapper;

import com.recontent.backend.review.dto.ReviewResponse;
import com.recontent.backend.review.entity.ReviewEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    public ReviewResponse toResponse(ReviewEntity review) {
        return new ReviewResponse(
                review.getId(),
                review.getMediaType(),
                review.getMediaId(),
                review.getContent(),
                review.isSpoilerFlag(),
                new ReviewResponse.Author(review.getUser().getId(), review.getUser().getUsername(), review.getUser().getAvatarUrl()),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
