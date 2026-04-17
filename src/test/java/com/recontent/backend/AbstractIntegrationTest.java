package com.recontent.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("recontent_test")
            .withUsername("recontent")
            .withPassword("recontent");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.security.jwt.secret", () -> "test-secret-that-is-long-enough-for-hs256-signing");
        registry.add("app.security.refresh-cookie.secure", () -> "false");
    }

    @AfterEach
    void keepDatabaseStateIsolatedByUniqueUsers() {
        // Test data uses unique users per test, preserving migration realism without truncating FK graphs.
    }

    protected RegisteredUser registerUser() throws Exception {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String username = "user_" + suffix;
        String email = username + "@example.com";
        String body = """
                {
                  "username": "%s",
                  "email": "%s",
                  "password": "StrongPass123!",
                  "firstName": "Test",
                  "lastName": "User"
                }
                """.formatted(username, email);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return new RegisteredUser(
                username,
                email,
                json.get("accessToken").asText(),
                result.getResponse().getHeader("Set-Cookie")
        );
    }

    protected record RegisteredUser(String username, String email, String accessToken, String refreshCookie) {
        String bearer() {
            return "Bearer " + accessToken;
        }
    }
}
