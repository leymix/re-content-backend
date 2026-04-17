package com.recontent.backend.favorite.service;

import com.recontent.backend.audit.service.AuditLogService;
import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.exception.ApiException;
import com.recontent.backend.favorite.dto.FavoriteRequest;
import com.recontent.backend.favorite.dto.FavoriteResponse;
import com.recontent.backend.favorite.entity.FavoriteEntity;
import com.recontent.backend.favorite.mapper.FavoriteMapper;
import com.recontent.backend.favorite.repository.FavoriteRepository;
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
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final FavoriteMapper favoriteMapper;
    private final AuditLogService auditLogService;

    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository, CurrentUserService currentUserService,
                           FavoriteMapper favoriteMapper, AuditLogService auditLogService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.favoriteMapper = favoriteMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> listMine() {
        UUID userId = currentUserService.requireCurrentUserId();
        return favoriteRepository.findAllByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(favoriteMapper::toResponse)
                .toList();
    }

    @Transactional
    public FavoriteResponse addMine(FavoriteRequest request) {
        UUID userId = currentUserService.requireCurrentUserId();
        FavoriteEntity favorite = favoriteRepository.findByUser_IdAndMediaTypeAndMediaId(userId, request.mediaType(), request.mediaId())
                .orElseGet(() -> {
                    FavoriteEntity created = new FavoriteEntity();
                    created.setUser(currentUser(userId));
                    return created;
                });
        favoriteMapper.apply(request, favorite);
        FavoriteEntity saved = favoriteRepository.save(favorite);
        auditLogService.record(userId, "FAVORITE_UPSERTED", "FAVORITE", saved.getId().toString(), Map.of("mediaType", request.mediaType().value(), "mediaId", request.mediaId()));
        return favoriteMapper.toResponse(saved);
    }

    @Transactional
    public void deleteMine(MediaType mediaType, Long mediaId) {
        UUID userId = currentUserService.requireCurrentUserId();
        FavoriteEntity favorite = favoriteRepository.findByUser_IdAndMediaTypeAndMediaId(userId, mediaType, mediaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "FAVORITE_NOT_FOUND", "Favorite was not found"));
        favoriteRepository.delete(favorite);
        auditLogService.record(userId, "FAVORITE_DELETED", "FAVORITE", favorite.getId().toString(), Map.of("mediaType", mediaType.value(), "mediaId", mediaId));
    }

    private UserEntity currentUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Current user was not found"));
    }
}
