package com.back.domain.member.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank String email,
        @NotBlank String password
) {}
