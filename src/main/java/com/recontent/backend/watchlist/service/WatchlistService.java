package com.recontent.backend.watchlist.service;

import com.recontent.backend.audit.service.AuditLogService;
import com.recontent.backend.exception.ApiException;
import com.recontent.backend.security.CurrentUserService;
import com.recontent.backend.user.entity.UserEntity;
import com.recontent.backend.user.repository.UserRepository;
import com.recontent.backend.watchlist.dto.WatchlistRequest;
import com.recontent.backend.watchlist.dto.WatchlistResponse;
import com.recontent.backend.watchlist.dto.WatchlistUpdateRequest;
import com.recontent.backend.watchlist.entity.WatchlistItemEntity;
import com.recontent.backend.watchlist.mapper.WatchlistMapper;
import com.recontent.backend.watchlist.repository.WatchlistRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WatchlistService {
    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final WatchlistMapper watchlistMapper;
    private final AuditLogService auditLogService;

    public WatchlistService(WatchlistRepository watchlistRepository, UserRepository userRepository, CurrentUserService currentUserService,
                            WatchlistMapper watchlistMapper, AuditLogService auditLogService) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.watchlistMapper = watchlistMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<WatchlistResponse> listMine() {
        UUID userId = currentUserService.requireCurrentUserId();
        return watchlistRepository.findAllByUser_IdOrderByUpdatedAtDesc(userId).stream().map(watchlistMapper::toResponse).toList();
    }

    @Transactional
    public WatchlistResponse addMine(WatchlistRequest request) {
        UUID userId = currentUserService.requireCurrentUserId();
        WatchlistItemEntity item = watchlistRepository.findByUser_IdAndMediaTypeAndMediaId(userId, request.mediaType(), request.mediaId())
                .orElseGet(() -> {
                    WatchlistItemEntity created = new WatchlistItemEntity();
                    created.setUser(currentUser(userId));
                    return created;
                });
        watchlistMapper.apply(request, item);
        WatchlistItemEntity saved = watchlistRepository.save(item);
        auditLogService.record(userId, "WATCHLIST_UPSERTED", "WATCHLIST_ITEM", saved.getId().toString(), Map.of("mediaId", request.mediaId()));
        return watchlistMapper.toResponse(saved);
    }

    @Transactional
    public WatchlistResponse updateMine(UUID id, WatchlistUpdateRequest request) {
        UUID userId = currentUserService.requireCurrentUserId();
        WatchlistItemEntity item = ownedItem(id, userId);
        item.setStatus(request.status());
        auditLogService.record(userId, "WATCHLIST_STATUS_UPDATED", "WATCHLIST_ITEM", id.toString(), Map.of("status", request.status().value()));
        return watchlistMapper.toResponse(watchlistRepository.save(item));
    }

    @Transactional
    public void deleteMine(UUID id) {
        UUID userId = currentUserService.requireCurrentUserId();
        WatchlistItemEntity item = ownedItem(id, userId);
        watchlistRepository.delete(item);
        auditLogService.record(userId, "WATCHLIST_DELETED", "WATCHLIST_ITEM", id.toString(), Map.of());
    }

    private WatchlistItemEntity ownedItem(UUID id, UUID userId) {
        WatchlistItemEntity item = watchlistRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WATCHLIST_ITEM_NOT_FOUND", "Watchlist item was not found"));
        if (!item.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "WATCHLIST_ITEM_FORBIDDEN", "You do not own this watchlist item");
        }
        return item;
    }

    private UserEntity currentUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "Current user was not found"));
    }
}
