package com.example.blog.factories;

import com.example.blog.domain.Author;
import org.factorybot.core.Definition;
import org.factorybot.core.Factory;

public class AuthorFactory extends Factory<Author> {

    public AuthorFactory() {
        super(Author.class);
    }

    @Override
    protected void define(Definition<Author> f) {
        f.attr(Author::setName, () -> faker().name().fullName());
        // A sequence keeps emails unique across builds (handy under the app's @Email/uniqueness rules).
        f.sequence(Author::setEmail, n -> "author" + n + "@example.com");
    }
}
