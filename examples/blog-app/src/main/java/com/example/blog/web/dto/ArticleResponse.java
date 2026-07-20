package com.example.blog.web.dto;

import com.example.blog.domain.Article;
import com.example.blog.domain.ArticleStatus;
import java.time.Instant;

public record ArticleResponse(Long id, String title, String slug, ArticleStatus status,
                              Long authorId, Instant publishedAt) {

    public static ArticleResponse from(Article article) {
        Long authorId = article.getAuthor() != null ? article.getAuthor().getId() : null;
        return new ArticleResponse(article.getId(), article.getTitle(), article.getSlug(),
                article.getStatus(), authorId, article.getPublishedAt());
    }
}
