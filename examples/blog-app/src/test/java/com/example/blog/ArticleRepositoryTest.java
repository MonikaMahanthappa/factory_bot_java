package com.example.blog;

import static com.example.blog.domain.ArticleStatus.PUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.blog.domain.Article;
import com.example.blog.domain.Author;
import com.example.blog.factories.ArticleFactory;
import com.example.blog.factories.AuthorFactory;
import com.example.blog.repository.ArticleRepository;
import com.example.blog.support.AbstractPostgresTest;
import org.factorybot.core.Attributes;
import org.factorybot.core.FactoryBot;
import org.factorybot.springdata.FactoryBotJpaAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Persistence-slice test. Uses factory_bot_java's {@code create} to set up rows (with a trait and an
 * association) and asserts the repository's derived queries. {@code @AutoConfigureTestDatabase(NONE)} keeps
 * the Testcontainers Postgres instead of an embedded DB.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(FactoryBotJpaAutoConfiguration.class)
class ArticleRepositoryTest extends AbstractPostgresTest {

    @Autowired
    private ArticleRepository articles;

    @Test
    void create_persistsPublishedArticleWithAuthor() {
        Article article = FactoryBot.create(ArticleFactory.class, "published");

        assertThat(article.getId()).isNotNull();
        assertThat(article.getAuthor().getId()).isNotNull(); // association was created + persisted
        assertThat(article.getSlug()).isNotBlank();
        assertThat(articles.findByStatus(PUBLISHED))
                .extracting(Article::getId).contains(article.getId());
    }

    @Test
    void findByAuthorIdAndStatus_filtersToPublished() {
        Author author = FactoryBot.create(AuthorFactory.class);

        FactoryBot.createList(ArticleFactory.class, 2, "published", Attributes.set(Article::setAuthor, author));
        FactoryBot.create(ArticleFactory.class, Attributes.set(Article::setAuthor, author)); // a draft

        assertThat(articles.findByAuthorIdAndStatus(author.getId(), PUBLISHED)).hasSize(2);
    }
}
