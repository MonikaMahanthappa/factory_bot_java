package com.example.blog.web.dto;

import com.example.blog.domain.Author;

public record AuthorResponse(Long id, String name, String email) {

    public static AuthorResponse from(Author author) {
        return new AuthorResponse(author.getId(), author.getName(), author.getEmail());
    }
}
