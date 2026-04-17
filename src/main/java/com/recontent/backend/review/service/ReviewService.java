package com.recontent.backend.review.service;

import com.recontent.backend.audit.service.AuditLogService;
import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.exception.ApiException;
import com.recontent.backend.review.dto.ReviewRequest;
import com.recontent.backend.review.dto.ReviewResponse;
import com.recontent.backend.review.dto.ReviewUpdateRequest;
import com.recontent.backend.review.entity.ReviewEntity;
import com.recontent.backend.review.mapper.ReviewMapper;
import com.recontent.backend.review.repository.ReviewRepository;
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
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final ReviewMapper reviewMapper;
    private final AuditLogService auditLogService;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, CurrentUserService currentUserService,
                         ReviewMapper reviewMapper, AuditLogService auditLogService) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.reviewMapper = reviewMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> listForMedia(MediaType mediaType, Long mediaId) {
        return reviewRepository.findAllByMediaTypeAndMediaIdOrderByCreatedAtDesc(mediaType, mediaId).stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse create(ReviewRequest request) {
        UUID userId = currentUserService.requireCurrentUserId();
        ReviewEntity review = new ReviewEntity();
        review.setUser(currentUser(userId));
        review.setMediaType(request.mediaType());
        review.setMediaId(request.mediaId());
        review.setContent(request.content());
        review.setSpoilerFlag(request.spoilerFlag());
        ReviewEntity saved = reviewRepository.save(review);
        auditLogService.record(userId, "REVIEW_CREATED", "REVIEW", saved.getId().toString(), Map.of("mediaId", request.mediaId()));
        return reviewMapper.toResponse(saved);
    }

    @Transactional
    public ReviewResponse update(UUID id, ReviewUpdateRequest request) {
        UUID userId = currentUserService.requireCurrentUserId();
        ReviewEntity review = ownedOrAdmin(id, userId);
        review.setContent(request.content());
        review.setSpoilerFlag(request.spoilerFlag());
        auditLogService.record(userId, "REVIEW_UPDATED", "REVIEW", id.toString(), Map.of());
        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = currentUserService.requireCurrentUserId();
        ReviewEntity review = ownedOrAdmin(id, userId);
        reviewRepository.delete(review);
        auditLogService.record(userId, "REVIEW_DELETED", "REVIEW", id.toString(), Map.of());
    }

    private ReviewEntity ownedOrAdmin(UUID id, UUID userId) {
        ReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "Review was not found"));
        if (!review.getUser().getId().equals(userId) && !currentUserService.hasRole("ADMIN")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "REVIEW_FORBIDDEN", "You do not have permission to modify this review");
        }
        return review;
    }

    private UserEntity currentUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Current user was not found"));
    }
}
