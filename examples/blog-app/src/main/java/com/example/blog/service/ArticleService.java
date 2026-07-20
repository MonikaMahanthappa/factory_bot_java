package com.example.blog.service;

import com.example.blog.domain.Article;
import com.example.blog.domain.ArticleStatus;
import com.example.blog.domain.Author;
import com.example.blog.domain.Comment;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.repository.AuthorRepository;
import com.example.blog.repository.CommentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The real workflow the example demonstrates: authors write draft articles, publish them, and readers
 * comment — but only on published articles. These rules are what the factory_bot_java-backed tests verify.
 */
@Service
@Transactional
public class ArticleService {

    private final AuthorRepository authors;
    private final ArticleRepository articles;
    private final CommentRepository comments;

    public ArticleService(AuthorRepository authors, ArticleRepository articles, CommentRepository comments) {
        this.authors = authors;
        this.articles = articles;
        this.comments = comments;
    }

    public Author createAuthor(String name, String email) {
        return authors.save(new Author(name, email));
    }

    public Article createDraft(Long authorId, String title, String body) {
        Author author = authors.findById(authorId)
                .orElseThrow(() -> new ArticleNotFoundException("No author with id " + authorId));
        Article article = new Article();
        article.setTitle(title);
        article.setSlug(slugify(title));
        article.setBody(body);
        article.setStatus(ArticleStatus.DRAFT);
        article.setAuthor(author);
        return articles.save(article);
    }

    public Article publish(Long articleId) {
        Article article = requireArticle(articleId);
        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            throw new IllegalArticleStateException("Article " + articleId + " is already published");
        }
        article.publish(Instant.now());
        return article;
    }

    public Comment addComment(Long articleId, String authorName, String body) {
        Article article = requireArticle(articleId);
        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            throw new IllegalArticleStateException("Cannot comment on article " + articleId + " until it is published");
        }
        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthorName(authorName);
        comment.setBody(body);
        return comments.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Article> listPublishedByAuthor(Long authorId) {
        return articles.findByAuthorIdAndStatus(authorId, ArticleStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public List<Article> listByStatus(ArticleStatus status) {
        return articles.findByStatus(status);
    }

    private Article requireArticle(Long articleId) {
        return articles.findById(articleId)
                .orElseThrow(() -> new ArticleNotFoundException("No article with id " + articleId));
    }

    private static String slugify(String title) {
        String slug = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        return slug.isEmpty() ? "article" : slug;
    }
}
