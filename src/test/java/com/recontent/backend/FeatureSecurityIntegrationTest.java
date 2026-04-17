package com.recontent.backend;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FeatureSecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    void favoritesAreIdempotentAndUseFrontendAliases() throws Exception {
        RegisteredUser user = registerUser();
        String favoriteBody = """
                {
                  "mediaType": "movie",
                  "id": 550,
                  "title": "Fight Club",
                  "poster_path": "/poster.jpg",
                  "backdrop_path": "/backdrop.jpg",
                  "overview": "A test favorite",
                  "release_date": "1999-10-15",
                  "vote_average": 8.4
                }
                """;

        mockMvc.perform(post("/api/v1/users/me/favorites")
                        .header("Authorization", user.bearer())
                        .contentType("application/json")
                        .content(favoriteBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaType").value("movie"))
                .andExpect(jsonPath("$.mediaId").value(550));

        mockMvc.perform(post("/api/v1/users/me/favorites")
                        .header("Authorization", user.bearer())
                        .contentType("application/json")
                        .content(favoriteBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/users/me/favorites")
                        .header("Authorization", user.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(delete("/api/v1/users/me/favorites/movie/550")
                        .header("Authorization", user.bearer()))
                .andExpect(status().isNoContent());
    }

    @Test
    void ratingValidationAndAdminProtectionWork() throws Exception {
        RegisteredUser user = registerUser();

        mockMvc.perform(post("/api/v1/users/me/ratings")
                        .header("Authorization", user.bearer())
                        .contentType("application/json")
                        .content("""
                                {
                                  "mediaType": "tv",
                                  "mediaId": 1399,
                                  "score": 12
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", user.bearer()))
                .andExpect(status().isForbidden());
    }

    @Test
    void reviewUpdateRequiresOwnerOrAdmin() throws Exception {
        RegisteredUser owner = registerUser();
        RegisteredUser other = registerUser();

        MvcResult createResult = mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", owner.bearer())
                        .contentType("application/json")
                        .content("""
                                {
                                  "mediaType": "movie",
                                  "mediaId": 603,
                                  "content": "A thoughtful review.",
                                  "spoilerFlag": false
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String reviewId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/v1/reviews/movie/603"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(patch("/api/v1/reviews/" + reviewId)
                        .header("Authorization", other.bearer())
                        .contentType("application/json")
                        .content("""
                                {
                                  "content": "Trying to edit someone else's review.",
                                  "spoilerFlag": false
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
