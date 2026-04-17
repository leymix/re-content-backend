package com.recontent.backend.watchlist.dto;

import com.recontent.backend.common.enums.WatchlistStatus;
import jakarta.validation.constraints.NotNull;

public record WatchlistUpdateRequest(@NotNull WatchlistStatus status) {
}
