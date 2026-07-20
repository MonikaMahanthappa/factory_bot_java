package com.example.blog.web;

import com.example.blog.domain.ArticleStatus;
import com.example.blog.service.ArticleService;
import com.example.blog.web.dto.AddCommentRequest;
import com.example.blog.web.dto.ArticleResponse;
import com.example.blog.web.dto.AuthorResponse;
import com.example.blog.web.dto.CommentResponse;
import com.example.blog.web.dto.CreateArticleRequest;
import com.example.blog.web.dto.CreateAuthorRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticleController {

    private final ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorResponse createAuthor(@Valid @RequestBody CreateAuthorRequest request) {
        return AuthorResponse.from(service.createAuthor(request.name(), request.email()));
    }

    @PostMapping("/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse createDraft(@Valid @RequestBody CreateArticleRequest request) {
        return ArticleResponse.from(service.createDraft(request.authorId(), request.title(), request.body()));
    }

    @PostMapping("/articles/{id}/publish")
    public ArticleResponse publish(@PathVariable Long id) {
        return ArticleResponse.from(service.publish(id));
    }

    @PostMapping("/articles/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse addComment(@PathVariable Long id, @Valid @RequestBody AddCommentRequest request) {
        return CommentResponse.from(service.addComment(id, request.authorName(), request.body()));
    }

    @GetMapping("/articles")
    public List<ArticleResponse> listByStatus(@RequestParam(defaultValue = "PUBLISHED") ArticleStatus status) {
        return service.listByStatus(status).stream().map(ArticleResponse::from).toList();
    }

    @GetMapping("/authors/{id}/articles")
    public List<ArticleResponse> listPublishedByAuthor(@PathVariable Long id) {
        return service.listPublishedByAuthor(id).stream().map(ArticleResponse::from).toList();
    }
}
