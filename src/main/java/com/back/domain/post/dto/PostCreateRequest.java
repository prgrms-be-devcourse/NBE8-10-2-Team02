package com.back.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(

        @NotBlank
        @Size(max = 20)
        String title,

        @NotBlank
        @Size(max = 5000)
        String content,
        List<String> tags
){}