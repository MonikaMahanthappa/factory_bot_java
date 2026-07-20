package com.example.blog.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateArticleRequest(@NotNull Long authorId, @NotBlank String title, @NotBlank String body) {
}
