package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Репозиторий на основе Jpa для работы с авторами")
@DataJpaTest
@Import(JpaAuthorRepository.class)
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
        //Given
        int expectedSize = 4;
        Author author4 = new Author("Author_4");
        testEntityManager.persistAndFlush(author4);

        //When
        List<Author> authors = authorRepository.findAll();

        //Then (there are already 3 records in the database)
        assertThat(authors)
                .isNotEmpty()
                .contains(author4)
                .hasSize(expectedSize);
        assertThat(authors.get(3).getFullName())
                .isNotEmpty()
                .isEqualTo("Author_4");
    }

    @DisplayName("должен загружать автора по id")
    @Test
    void whenFindAuthorById_thenReturnAuthor() {
        //Given
        Author author = new Author("New author");
        testEntityManager.persistAndFlush(author);

        //When
        Optional<Author> foundAuthor = authorRepository.findById(author.getId());

        //Then
        assertThat(foundAuthor).isPresent().get().isNotNull().isEqualTo(author);
    }

    @DisplayName("должен вернуть Optional.empty если автора не существует")
    @Test
    void whenFindAuthorByNonExistentId_thenReturnOptionalEmpty() {
        //Given - author MISSING_ID

        //When
        Optional<Author> foundAuthor = authorRepository.findById(MISSING_ID);

        //Then
        assertThat(foundAuthor).isNotNull().isEmpty();
    }

    @DisplayName("должен загружать список авторов по ids")
    @Test
    void whenAuthorsFindByIds_thenReturnAuthors() {
        //Given
        Set<Long> ids = Set.of(PRESENT_ID, 3L);

        //When
        List<Author> authors = authorRepository.findByIds(ids);

        //Then
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
        //Given
        Set<Long> missingIds = Set.of(21L, MISSING_ID);

        //When
        List<Author> authors = authorRepository.findByIds(missingIds);

        //Then
        assertThat(authors).isNotNull().isEmpty();
    }

    @DisplayName("должен сохранить нового автора")
    @Test
    void whenSaveNewAuthor_thenReturnSavedUser() {
        //Given
        String fullName = "New author";
        Author author = new Author(0L, fullName);

        //When
        Author savedAuthor = authorRepository.save(author);

        //Then
        assertThat(savedAuthor.getId()).isNotNull().isNotEqualTo(0L);
        assertThat(savedAuthor.getFullName()).isEqualTo(fullName);
    }

    @DisplayName("должен изменить существующего автора")
    @Test
    void whenUpdateAuthor_thenReturnUpdatedAuthor() {
        //Given - author PRESENT_ID
        String fullName = "Author_1";
        String updatedFullName = "Updated author";
        Author author = testEntityManager.find(Author.class, PRESENT_ID);

        //When
        author.setFullName(updatedFullName);
        Author updatedAuthor = authorRepository.save(author);

        //Then
        assertThat(updatedAuthor.getId()).isNotNull().isEqualTo(PRESENT_ID);
        assertThat(updatedAuthor.getFullName()).isNotEmpty().isNotEqualTo(fullName).isEqualTo(updatedFullName);
    }

    @DisplayName("должен удалить автора по id")
    @Test
    void whenDeleteAuthorById_thenAuthorIsDeleted() {
        //Given - author PRESENT_ID
        int authorsSize = authorRepository.findAll().size();

        //When
        authorRepository.deleteById(PRESENT_ID);

        //Then
        assertThat(authorRepository.findAll().size()).isNotEqualTo(authorsSize);
        assertThat(testEntityManager.find(Author.class, PRESENT_ID)).isNull();
        assertThat(authorRepository.findById(PRESENT_ID)).isNotNull().isEmpty();
    }

    @DisplayName("должен выбрасывать исключение EntityNotFoundException если автор не найден по id при удалении")
    @Test
    void whenAuthorNotFoundById_thenThrowEntityNotFoundException() {
        //Given - author MISSING_ID

        //When
        Throwable exception = assertThrows(EntityNotFoundException.class,
                () -> authorRepository.deleteById(MISSING_ID));

        //Then
        assertEquals("Can't delete author with id %d. Author not found".formatted(MISSING_ID), exception.getMessage());
    }
}
