package com.example.blog.service;

/** Raised when an article (or author) referenced by id does not exist. Mapped to HTTP 404. */
public class ArticleNotFoundException extends RuntimeException {

    public ArticleNotFoundException(String message) {
        super(message);
    }
}
