package com.example.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.blog.domain.Article;
import com.example.blog.domain.ArticleStatus;
import com.example.blog.domain.Comment;
import com.example.blog.factories.ArticleFactory;
import com.example.blog.service.ArticleService;
import com.example.blog.service.IllegalArticleStateException;
import com.example.blog.support.AbstractPostgresTest;
import org.factorybot.core.FactoryBot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service-layer test. Factories arrange the exact state each business rule needs — a draft to publish, a
 * draft that must reject comments, a published article that accepts them.
 */
@SpringBootTest
@Transactional
class ArticleServiceTest extends AbstractPostgresTest {

    @Autowired
    private ArticleService service;

    @Test
    void publish_movesDraftToPublished() {
        Article draft = FactoryBot.create(ArticleFactory.class); // DRAFT by default

        Article published = service.publish(draft.getId());

        assertThat(published.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(published.getPublishedAt()).isNotNull();
    }

    @Test
    void addComment_onDraft_isRejected() {
        Article draft = FactoryBot.create(ArticleFactory.class);

        assertThatThrownBy(() -> service.addComment(draft.getId(), "reader", "nice one"))
                .isInstanceOf(IllegalArticleStateException.class);
    }

    @Test
    void addComment_onPublished_succeeds() {
        Article published = FactoryBot.create(ArticleFactory.class, "published");

        Comment comment = service.addComment(published.getId(), "reader", "great read");

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getArticle().getId()).isEqualTo(published.getId());
    }
}
