package com.recontent.backend.membership.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubscribeRequest(@NotNull UUID planId) {
}
