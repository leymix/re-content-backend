package com.recontent.backend.watchlist.repository;

import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.watchlist.entity.WatchlistItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchlistRepository extends JpaRepository<WatchlistItemEntity, UUID> {
    List<WatchlistItemEntity> findAllByUser_IdOrderByUpdatedAtDesc(UUID userId);

    Optional<WatchlistItemEntity> findByUser_IdAndMediaTypeAndMediaId(UUID userId, MediaType mediaType, Long mediaId);
}
