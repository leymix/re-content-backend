package com.recontent.backend.favorite.mapper;

import com.recontent.backend.favorite.dto.FavoriteRequest;
import com.recontent.backend.favorite.dto.FavoriteResponse;
import com.recontent.backend.favorite.entity.FavoriteEntity;
import org.springframework.stereotype.Component;

@Component
public class FavoriteMapper {
    public FavoriteResponse toResponse(FavoriteEntity favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                favorite.getMediaType(),
                favorite.getMediaId(),
                favorite.getTitle(),
                favorite.getPosterPath(),
                favorite.getBackdropPath(),
                favorite.getOverview(),
                favorite.getReleaseDate(),
                favorite.getVoteAverage(),
                favorite.getCreatedAt()
        );
    }

    public void apply(FavoriteRequest request, FavoriteEntity favorite) {
        favorite.setMediaType(request.mediaType());
        favorite.setMediaId(request.mediaId());
        favorite.setTitle(request.title());
        favorite.setPosterPath(request.posterPath());
        favorite.setBackdropPath(request.backdropPath());
        favorite.setOverview(request.overview());
        favorite.setReleaseDate(request.releaseDate());
        favorite.setVoteAverage(request.voteAverage());
    }
}
