package com.example.blog.service;

/** Raised when an operation is invalid for the article's current state (e.g. commenting on a draft). Mapped to HTTP 409. */
public class IllegalArticleStateException extends RuntimeException {

    public IllegalArticleStateException(String message) {
        super(message);
    }
}
