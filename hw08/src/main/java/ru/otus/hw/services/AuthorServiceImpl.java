package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.mappers.AuthorMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    private final BookRepository bookRepository;

    private final CommentRepository commentRepository;

    private final AuthorMapper mapper;

    @Override
    public List<AuthorDto> findAll() {
        return authorRepository.findAll().stream().map(mapper::toAuthorDto).toList();
    }

    @Override
    public List<AuthorDto> findByIds(Set<String> ids) {
        if (isEmpty(ids)) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Author.class.getSimpleName()));
        }
        return authorRepository.findAllById(ids).stream().map(mapper::toAuthorDto).toList();
    }

    @Override
    public Optional<AuthorDto> findById(String id) {
        return authorRepository.findById(id).map(mapper::toAuthorDto);
    }

    @Override
    public AuthorDto insert(@Valid String fullName) {
        return mapper.toAuthorDto(authorRepository.save(new Author(fullName)));
    }

    @Override
    public AuthorDto update(String id, @Valid String fullName) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Author.class.getSimpleName(), id)));
        author.setFullName(fullName);
        return mapper.toAuthorDto(authorRepository.save(author));
    }

    @Override
    public void deleteById(String authorId) {
        List<Book> books = bookRepository.findAllByAuthorId(authorId);
        authorRepository.deleteById(authorId);
        bookRepository.deleteAllByAuthorId(authorId);
        books.forEach(book -> commentRepository.deleteAllByBookId(book.getId()));
    }
}
