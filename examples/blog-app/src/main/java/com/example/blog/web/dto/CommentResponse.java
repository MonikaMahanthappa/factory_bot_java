package com.example.blog.web.dto;

import com.example.blog.domain.Comment;

public record CommentResponse(Long id, Long articleId, String authorName, String body) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getArticle().getId(),
                comment.getAuthorName(), comment.getBody());
    }
}
