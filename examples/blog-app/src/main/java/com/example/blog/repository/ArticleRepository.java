package com.example.blog.repository;

import com.example.blog.domain.Article;
import com.example.blog.domain.ArticleStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByStatus(ArticleStatus status);

    List<Article> findByAuthorIdAndStatus(Long authorId, ArticleStatus status);

    Optional<Article> findBySlug(String slug);
}
