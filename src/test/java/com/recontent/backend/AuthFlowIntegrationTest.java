package com.recontent.backend;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerMeRefreshAndLogoutFlowWorks() throws Exception {
        RegisteredUser user = registerUser();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", user.bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.username").value(user.username()))
                .andExpect(jsonPath("$.user.roles[0]").value("USER"));

        String rotatedCookie = mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Cookie", user.refreshCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(header().string("Set-Cookie", containsString("refresh_token=")))
                .andReturn()
                .getResponse()
                .getHeader("Set-Cookie");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Cookie", user.refreshCookie()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", user.bearer())
                        .header("Cookie", rotatedCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("refresh_token", 0));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .header("Cookie", rotatedCookie))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validationErrorsUseStandardShape() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "x",
                                  "email": "not-an-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.email").exists());
    }
}
