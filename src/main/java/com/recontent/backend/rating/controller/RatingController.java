package com.recontent.backend.rating.controller;

import com.recontent.backend.rating.dto.RatingRequest;
import com.recontent.backend.rating.dto.RatingResponse;
import com.recontent.backend.rating.dto.RatingUpdateRequest;
import com.recontent.backend.rating.service.RatingService;
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

@Tag(name = "Ratings")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users/me/ratings")
public class RatingController {
    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Operation(summary = "List current user's ratings")
    @GetMapping
    public List<RatingResponse> listMine() {
        return ratingService.listMine();
    }

    @Operation(summary = "Create or update a rating by media reference")
    @PostMapping
    public ResponseEntity<RatingResponse> addMine(@Valid @RequestBody RatingRequest request) {
        return ResponseEntity.status(201).body(ratingService.addMine(request));
    }

    @Operation(summary = "Update a rating")
    @PatchMapping("/{id}")
    public RatingResponse updateMine(@PathVariable UUID id, @Valid @RequestBody RatingUpdateRequest request) {
        return ratingService.updateMine(id, request);
    }

    @Operation(summary = "Delete a rating")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMine(@PathVariable UUID id) {
        ratingService.deleteMine(id);
        return ResponseEntity.noContent().build();
    }
}
