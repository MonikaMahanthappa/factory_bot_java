package com.example.blog.factories;

import com.example.blog.domain.Article;
import com.example.blog.domain.ArticleStatus;
import java.time.Instant;
import java.util.Locale;
import org.factorybot.core.Definition;
import org.factorybot.core.Factory;

/**
 * Shows the factory_bot_java DSL end-to-end: a sequence, a dependent attribute, an association, and a
 * trait — all type-safe via method references.
 */
public class ArticleFactory extends Factory<Article> {

    public ArticleFactory() {
        super(Article.class);
    }

    @Override
    protected void define(Definition<Article> f) {
        f.sequence(Article::setTitle, n -> "Article Number " + n);

        // Dependent attribute: derive the slug from whatever the title resolved to.
        f.attr(Article::setSlug, ev ->
                ev.get(Article::getTitle).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-"));

        f.attr(Article::setBody, () -> faker().lorem().paragraph());
        f.attr(Article::setStatus, () -> ArticleStatus.DRAFT);

        // Association: the author is built with the parent's strategy (create→create, build→build).
        f.association(Article::setAuthor, AuthorFactory.class);

        // Trait: a ready-to-read, published article.
        f.trait("published", t -> t
                .attr(Article::setStatus, () -> ArticleStatus.PUBLISHED)
                .attr(Article::setPublishedAt, () -> Instant.now()));
    }
}
