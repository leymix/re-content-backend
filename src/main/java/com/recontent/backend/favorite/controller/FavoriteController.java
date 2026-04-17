package com.recontent.backend.favorite.controller;

import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.favorite.dto.FavoriteRequest;
import com.recontent.backend.favorite.dto.FavoriteResponse;
import com.recontent.backend.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Tag(name = "Favorites")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users/me/favorites")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "List current user's favorites")
    @GetMapping
    public List<FavoriteResponse> listMine() {
        return favoriteService.listMine();
    }

    @Operation(summary = "Add or update a favorite by media reference")
    @PostMapping
    public ResponseEntity<FavoriteResponse> addMine(@Valid @RequestBody FavoriteRequest request) {
        return ResponseEntity.status(201).body(favoriteService.addMine(request));
    }

    @Operation(summary = "Remove a favorite by media type and TMDb id")
    @DeleteMapping("/{mediaType}/{mediaId}")
    public ResponseEntity<Void> deleteMine(@PathVariable MediaType mediaType, @PathVariable @Positive Long mediaId) {
        favoriteService.deleteMine(mediaType, mediaId);
        return ResponseEntity.noContent().build();
    }
}
