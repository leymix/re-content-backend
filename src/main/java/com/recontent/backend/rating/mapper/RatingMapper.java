package com.recontent.backend.rating.mapper;

import com.recontent.backend.rating.dto.RatingResponse;
import com.recontent.backend.rating.entity.RatingEntity;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {
    public RatingResponse toResponse(RatingEntity rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getMediaType(),
                rating.getMediaId(),
                rating.getScore(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}
