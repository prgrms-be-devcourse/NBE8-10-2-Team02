package com.back.domain.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewModifyRequest (
        @NotBlank
        String title,
        @NotBlank
        String content,
        @NotNull @DecimalMin("0") @DecimalMax("5")
        double rating
){
}