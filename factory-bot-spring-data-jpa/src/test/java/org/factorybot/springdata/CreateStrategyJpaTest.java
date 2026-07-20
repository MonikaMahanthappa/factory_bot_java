package org.factorybot.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import org.factorybot.core.FactoryBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Proves the {@code create} strategy persists via Spring Data JPA, cascades association persistence,
 * and that {@code build} stays in-memory. Runs inside {@code @DataJpaTest}, so every test's writes roll
 * back — the {@code count() == 0} assertions at each start demonstrate that isolation.
 */
@DataJpaTest
@Import(FactoryBotJpaAutoConfiguration.class)
class CreateStrategyJpaTest {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private SpringDataPersistenceHandler persistenceHandler;

    @BeforeEach
    void resetFactoryBot() {
        // reset() clears the global persistence handler; re-install the Spring-managed one.
        FactoryBot.reset();
        FactoryBot.setPersistenceHandler(persistenceHandler);
    }

    @Test
    void create_persistsEntity() {
        assertThat(authorRepository.count()).isZero(); // previous tests rolled back

        Author author = FactoryBot.create(AuthorFactory.class);

        assertThat(author.getId()).isNotNull();
        assertThat(authorRepository.findById(author.getId())).isPresent();
        assertThat(authorRepository.count()).isEqualTo(1);
    }

    @Test
    void create_cascadesAssociationPersistence() {
        assertThat(bookRepository.count()).isZero();

        Book book = FactoryBot.create(BookFactory.class);

        assertThat(book.getId()).isNotNull();
        assertThat(book.getAuthor()).isNotNull();
        assertThat(book.getAuthor().getId()).isNotNull(); // author was created first
        assertThat(bookRepository.count()).isEqualTo(1);
        assertThat(authorRepository.count()).isEqualTo(1);
    }

    @Test
    void build_doesNotPersist_norItsAssociation() {
        Book book = FactoryBot.build(BookFactory.class);

        assertThat(book.getTitle()).isNotBlank();
        assertThat(book.getId()).isNull();
        assertThat(book.getAuthor()).isNotNull();
        assertThat(book.getAuthor().getId()).isNull(); // association built, not persisted
        assertThat(bookRepository.count()).isZero();
        assertThat(authorRepository.count()).isZero();
    }
}
