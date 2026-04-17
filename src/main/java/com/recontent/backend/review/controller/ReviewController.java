package com.recontent.backend.review.controller;

import com.recontent.backend.common.enums.MediaType;
import com.recontent.backend.review.dto.ReviewRequest;
import com.recontent.backend.review.dto.ReviewResponse;
import com.recontent.backend.review.dto.ReviewUpdateRequest;
import com.recontent.backend.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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

@Validated
@Tag(name = "Reviews")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "List reviews for a movie or TV series")
    @GetMapping("/{mediaType}/{mediaId}")
    public List<ReviewResponse> listForMedia(@PathVariable MediaType mediaType, @PathVariable @Positive Long mediaId) {
        return reviewService.listForMedia(mediaType, mediaId);
    }

    @Operation(summary = "Create a review")
    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(201).body(reviewService.create(request));
    }

    @Operation(summary = "Update a review")
    @PatchMapping("/{id}")
    public ReviewResponse update(@PathVariable UUID id, @Valid @RequestBody ReviewUpdateRequest request) {
        return reviewService.update(id, request);
    }

    @Operation(summary = "Delete a review")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
