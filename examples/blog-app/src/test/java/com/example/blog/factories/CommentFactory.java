package com.example.blog.factories;

import com.example.blog.domain.Comment;
import org.factorybot.core.Definition;
import org.factorybot.core.Factory;

public class CommentFactory extends Factory<Comment> {

    public CommentFactory() {
        super(Comment.class);
    }

    @Override
    protected void define(Definition<Comment> f) {
        f.attr(Comment::setAuthorName, () -> faker().internet().username());
        f.attr(Comment::setBody, () -> faker().lorem().sentence());
        // Comments are only valid on published articles, so associate a published one by default.
        f.association(Comment::setArticle, ArticleFactory.class, "published");
    }
}
