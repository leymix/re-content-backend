package com.recontent.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WatchlistStatus {
    PLANNED("planned"),
    WATCHING("watching"),
    COMPLETED("completed"),
    DROPPED("dropped");

    private final String value;

    WatchlistStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static WatchlistStatus from(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String normalized = rawValue.trim().toLowerCase();
        for (WatchlistStatus status : values()) {
            if (status.value.equals(normalized) || status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported watchlist status: " + rawValue);
    }

    @JsonValue
    public String value() {
        return value;
    }
}
