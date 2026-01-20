package com.back.domain.member.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthSignupRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 4, max = 50) String password,
        @NotBlank @Size(min = 2, max = 30) String nickname
) {}
