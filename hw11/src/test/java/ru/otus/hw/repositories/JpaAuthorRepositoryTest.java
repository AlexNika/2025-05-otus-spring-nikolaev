package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Репозиторий на основе Jpa для работы с авторами")
@DataJpaTest
class JpaAuthorRepositoryTest {

    private static final Long MISSING_ID = 42L;
    private static final Long PRESENT_ID = 1L;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @DisplayName("должен загружать список всех авторов")
    @Test
    void whenFindAllAuthors_thenReturnAllAuthors() {
        int expectedSize = 6;
        Author author4 = new Author("Author_4");
        testEntityManager.persistAndFlush(author4);
        List<Author> authors = authorRepository.findAll();
        assertThat(authors)
                .isNotEmpty()
                .contains(author4)
                .hasSize(expectedSize);
        assertThat(authors.get(3).getFullName())
                .isNotEmpty()
                .isEqualTo("Теодор Драйзер");
    }

    @DisplayName("должен загружать автора по id")
    @Test
    void whenFindAuthorById_thenReturnAuthor() {
        Author author = new Author("New author");
        testEntityManager.persistAndFlush(author);
        Optional<Author> foundAuthor = authorRepository.findById(author.getId());
        assertThat(foundAuthor).isPresent().get().isNotNull().isEqualTo(author);
    }

    @DisplayName("должен вернуть Optional.empty если автора не существует")
    @Test
    void whenFindAuthorByNonExistentId_thenReturnOptionalEmpty() {
        Optional<Author> foundAuthor = authorRepository.findById(MISSING_ID);
        assertThat(foundAuthor).isNotNull().isEmpty();
    }

    @DisplayName("должен загружать список авторов по ids")
    @Test
    void whenAuthorsFindByIds_thenReturnAuthors() {
        Set<Long> ids = Set.of(PRESENT_ID, 3L);
        List<Author> authors = authorRepository.findAllById(ids);
        assertThat(authors)
                .isNotNull()
                .isNotEmpty()
                .hasSize(ids.size())
                .allSatisfy(author -> {
                    assertThat(author.getId()).isPositive();
                    assertThat(author.getFullName()).isNotBlank();
                });
    }

    @DisplayName("должен возвращать пустой список если авторов нет по ids")
    @Test
    void whenFindAuthorByNonExistentIds_thenReturnEmptyList() {
        Set<Long> missingIds = Set.of(21L, MISSING_ID);
        List<Author> authors = authorRepository.findAllById(missingIds);
        assertThat(authors).isNotNull().isEmpty();
    }

    @DisplayName("должен сохранить нового автора")
    @Test
    void whenSaveNewAuthor_thenReturnSavedUser() {
        String fullName = "New author";
        Author author = new Author(fullName);
        Author savedAuthor = authorRepository.save(author);
        assertThat(savedAuthor.getId()).isNotNull().isNotEqualTo(0L);
        assertThat(savedAuthor.getFullName()).isEqualTo(fullName);
    }

    @DisplayName("должен изменить существующего автора")
    @Test
    void whenUpdateAuthor_thenReturnUpdatedAuthor() {
        String fullName = "Author_1";
        String updatedFullName = "Updated author";
        Author author = testEntityManager.find(Author.class, PRESENT_ID);
        author.setFullName(updatedFullName);
        Author updatedAuthor = authorRepository.save(author);
        assertThat(updatedAuthor.getId()).isNotNull().isEqualTo(PRESENT_ID);
        assertThat(updatedAuthor.getFullName()).isNotEmpty().isNotEqualTo(fullName).isEqualTo(updatedFullName);
    }

    @DisplayName("должен удалить автора по id")
    @Test
    void whenDeleteAuthorById_thenAuthorIsDeleted() {
        int authorsSize = authorRepository.findAll().size();
        authorRepository.deleteById(PRESENT_ID);
        assertThat(authorRepository.findAll().size()).isNotEqualTo(authorsSize);
        assertThat(testEntityManager.find(Author.class, PRESENT_ID)).isNull();
        assertThat(authorRepository.findById(PRESENT_ID)).isNotNull().isEmpty();
    }

    @DisplayName("не должен выбрасывать ни одного исключения, даже если автор не найден по id при удалении")
    @Test
    void whenAuthorNotFoundById_thenWillNotThrowAnyException() {
        assertAll(() -> authorRepository.deleteById(MISSING_ID));
    }
}
