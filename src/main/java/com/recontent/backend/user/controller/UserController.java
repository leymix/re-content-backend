package com.recontent.backend.user.controller;

import com.recontent.backend.user.dto.UpdateUserRequest;
import com.recontent.backend.user.dto.UserResponse;
import com.recontent.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public UserResponse me() {
        return userService.me();
    }

    @Operation(summary = "Update current user profile")
    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateUserRequest request) {
        return userService.updateMe(request);
    }
}
