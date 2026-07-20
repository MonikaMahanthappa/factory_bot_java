package org.factorybot.springdata;

import org.factorybot.core.Definition;
import org.factorybot.core.Factory;

public class BookFactory extends Factory<Book> {

    public BookFactory() {
        super(Book.class);
    }

    @Override
    protected void define(Definition<Book> f) {
        f.sequence(Book::setTitle, n -> "Book #" + n);
        // The author is built with the same strategy as the book (create→create, build→build).
        f.association(Book::setAuthor, AuthorFactory.class);
    }
}
