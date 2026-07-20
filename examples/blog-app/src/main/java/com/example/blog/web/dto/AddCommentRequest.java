package com.example.blog.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddCommentRequest(@NotBlank String authorName, @NotBlank String body) {
}
