package com.recontent.backend.watchlist.controller;

import com.recontent.backend.watchlist.dto.WatchlistRequest;
import com.recontent.backend.watchlist.dto.WatchlistResponse;
import com.recontent.backend.watchlist.dto.WatchlistUpdateRequest;
import com.recontent.backend.watchlist.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Watchlist")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users/me/watchlist")
public class WatchlistController {
    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @Operation(summary = "List current user's watchlist")
    @GetMapping
    public List<WatchlistResponse> listMine() {
        return watchlistService.listMine();
    }

    @Operation(summary = "Add or update a watchlist item by media reference")
    @PostMapping
    public ResponseEntity<WatchlistResponse> addMine(@Valid @RequestBody WatchlistRequest request) {
        return ResponseEntity.status(201).body(watchlistService.addMine(request));
    }

    @Operation(summary = "Update watchlist item status")
    @PatchMapping("/{id}")
    public WatchlistResponse updateMine(@PathVariable UUID id, @Valid @RequestBody WatchlistUpdateRequest request) {
        return watchlistService.updateMine(id, request);
    }

    @Operation(summary = "Delete a watchlist item")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMine(@PathVariable UUID id) {
        watchlistService.deleteMine(id);
        return ResponseEntity.noContent().build();
    }
}
