package com.recontent.backend.rating.service;

import com.recontent.backend.audit.service.AuditLogService;
import com.recontent.backend.exception.ApiException;
import com.recontent.backend.rating.dto.RatingRequest;
import com.recontent.backend.rating.dto.RatingResponse;
import com.recontent.backend.rating.dto.RatingUpdateRequest;
import com.recontent.backend.rating.entity.RatingEntity;
import com.recontent.backend.rating.mapper.RatingMapper;
import com.recontent.backend.rating.repository.RatingRepository;
import com.recontent.backend.security.CurrentUserService;
import com.recontent.backend.user.entity.UserEntity;
import com.recontent.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final RatingMapper ratingMapper;
    private final AuditLogService auditLogService;

    public RatingService(RatingRepository ratingRepository, UserRepository userRepository, CurrentUserService currentUserService,
                         RatingMapper ratingMapper, AuditLogService auditLogService) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.ratingMapper = ratingMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> listMine() {
        UUID userId = currentUserService.requireCurrentUserId();
        return ratingRepository.findAllByUser_IdOrderByUpdatedAtDesc(userId).stream().map(ratingMapper::toResponse).toList();
    }

    @Transactional
    public RatingResponse addMine(RatingRequest request) {
        UUID userId = currentUserService.requireCurrentUserId();
        RatingEntity rating = ratingRepository.findByUser_IdAndMediaTypeAndMediaId(userId, request.mediaType(), request.mediaId())
                .orElseGet(() -> {
                    RatingEntity created = new RatingEntity();
                    created.setUser(currentUser(userId));
                    created.setMediaType(request.mediaType());
                    created.setMediaId(request.mediaId());
                    return created;
                });
        rating.setScore(request.score());
        RatingEntity saved = ratingRepository.save(rating);
        auditLogService.record(userId, "RATING_UPSERTED", "RATING", saved.getId().toString(), Map.of("score", request.score()));
        return ratingMapper.toResponse(saved);
    }

    @Transactional
    public RatingResponse updateMine(UUID id, RatingUpdateRequest request) {
        UUID userId = currentUserService.requireCurrentUserId();
        RatingEntity rating = ownedRating(id, userId);
        rating.setScore(request.score());
        auditLogService.record(userId, "RATING_UPDATED", "RATING", id.toString(), Map.of("score", request.score()));
        return ratingMapper.toResponse(ratingRepository.save(rating));
    }

    @Transactional
    public void deleteMine(UUID id) {
        UUID userId = currentUserService.requireCurrentUserId();
        RatingEntity rating = ownedRating(id, userId);
        ratingRepository.delete(rating);
        auditLogService.record(userId, "RATING_DELETED", "RATING", id.toString(), Map.of());
    }

    private RatingEntity ownedRating(UUID id, UUID userId) {
        RatingEntity rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "RATING_NOT_FOUND", "Rating was not found"));
        if (!rating.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "RATING_FORBIDDEN", "You do not own this rating");
        }
        return rating;
    }

    private UserEntity currentUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Current user was not found"));
    }
}
