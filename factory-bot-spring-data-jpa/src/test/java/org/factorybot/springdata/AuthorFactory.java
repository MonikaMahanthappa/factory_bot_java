package org.factorybot.springdata;

import org.factorybot.core.Definition;
import org.factorybot.core.Factory;

public class AuthorFactory extends Factory<Author> {

    public AuthorFactory() {
        super(Author.class);
    }

    @Override
    protected void define(Definition<Author> f) {
        f.attr(Author::setName, () -> faker().book().author());
        f.attr(Author::setEmail, ev -> "author" + System.identityHashCode(ev) + "@example.com");
    }
}
