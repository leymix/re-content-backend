package com.recontent.backend.watchlist.mapper;

import com.recontent.backend.common.enums.WatchlistStatus;
import com.recontent.backend.watchlist.dto.WatchlistRequest;
import com.recontent.backend.watchlist.dto.WatchlistResponse;
import com.recontent.backend.watchlist.entity.WatchlistItemEntity;
import org.springframework.stereotype.Component;

@Component
public class WatchlistMapper {
    public WatchlistResponse toResponse(WatchlistItemEntity item) {
        return new WatchlistResponse(
                item.getId(),
                item.getMediaType(),
                item.getMediaId(),
                item.getTitle(),
                item.getPosterPath(),
                item.getBackdropPath(),
                item.getOverview(),
                item.getReleaseDate(),
                item.getVoteAverage(),
                item.getStatus(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    public void apply(WatchlistRequest request, WatchlistItemEntity item) {
        item.setMediaType(request.mediaType());
        item.setMediaId(request.mediaId());
        item.setTitle(request.title());
        item.setPosterPath(request.posterPath());
        item.setBackdropPath(request.backdropPath());
        item.setOverview(request.overview());
        item.setReleaseDate(request.releaseDate());
        item.setVoteAverage(request.voteAverage());
        item.setStatus(request.status() == null ? WatchlistStatus.PLANNED : request.status());
    }
}
