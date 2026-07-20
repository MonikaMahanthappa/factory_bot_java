package com.example.blog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.blog.domain.Article;
import com.example.blog.factories.ArticleFactory;
import com.example.blog.support.AbstractPostgresTest;
import org.factorybot.core.FactoryBot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Web-layer test. Factories seed the database, then the real HTTP endpoints are exercised via MockMvc —
 * no hand-built fixtures.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleControllerTest extends AbstractPostgresTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listPublished_returnsSeededArticle() throws Exception {
        Article article = FactoryBot.create(ArticleFactory.class, "published");

        mockMvc.perform(get("/articles").param("status", "PUBLISHED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]", article.getId()).exists());
    }

    @Test
    void postComment_onPublishedArticle_isCreated() throws Exception {
        Article article = FactoryBot.create(ArticleFactory.class, "published");

        mockMvc.perform(post("/articles/{id}/comments", article.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorName\":\"reader\",\"body\":\"great read\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.articleId").value(article.getId()));
    }
}
