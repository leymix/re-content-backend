package com.recontent.backend.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MediaType {
    MOVIE("movie"),
    TV("tv");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MediaType from(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String normalized = rawValue.trim().toLowerCase();
        if ("series".equals(normalized)) {
            normalized = "tv";
        }
        for (MediaType mediaType : values()) {
            if (mediaType.value.equals(normalized) || mediaType.name().equalsIgnoreCase(normalized)) {
                return mediaType;
            }
        }
        throw new IllegalArgumentException("Unsupported media type: " + rawValue);
    }

    @JsonValue
    public String value() {
        return value;
    }
}
