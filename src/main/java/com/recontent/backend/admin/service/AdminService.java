package com.recontent.backend.admin.service;

import com.recontent.backend.admin.dto.AdminUserResponse;
import com.recontent.backend.admin.dto.HealthSummaryResponse;
import com.recontent.backend.audit.repository.AuditLogRepository;
import com.recontent.backend.favorite.repository.FavoriteRepository;
import com.recontent.backend.rating.repository.RatingRepository;
import com.recontent.backend.review.repository.ReviewRepository;
import com.recontent.backend.user.entity.RoleEntity;
import com.recontent.backend.user.repository.UserRepository;
import com.recontent.backend.watchlist.repository.WatchlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final WatchlistRepository watchlistRepository;
    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminService(UserRepository userRepository, FavoriteRepository favoriteRepository, WatchlistRepository watchlistRepository,
                        RatingRepository ratingRepository, ReviewRepository reviewRepository, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.watchlistRepository = watchlistRepository;
        this.ratingRepository = ratingRepository;
        this.reviewRepository = reviewRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> users() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getStatus(),
                        user.getRoles().stream().map(RoleEntity::getName).map(Enum::name).sorted(Comparator.naturalOrder()).collect(Collectors.toCollection(LinkedHashSet::new)),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public HealthSummaryResponse healthSummary() {
        return new HealthSummaryResponse(
                "UP",
                userRepository.count(),
                favoriteRepository.count(),
                watchlistRepository.count(),
                ratingRepository.count(),
                reviewRepository.count(),
                auditLogRepository.count()
        );
    }
}
