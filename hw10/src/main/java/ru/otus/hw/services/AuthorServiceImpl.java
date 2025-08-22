package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.mapper.AuthorMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

@Service
@Validated
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    private final AuthorMapper mapper;

    @Override
    public List<AuthorDto> findAll() {
        return authorRepository.findAll().stream().map(mapper::toAuthorDto).toList();
    }

    @Override
    public List<AuthorDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Author.class.getSimpleName()));
        }
        return authorRepository.findAllById(ids).stream().map(mapper::toAuthorDto).toList();
    }

    @Override
    public Optional<AuthorDto> findById(long id) {
        return authorRepository.findById(id).map(mapper::toAuthorDto);
    }

    @Override
    @Transactional
    public AuthorDto insert(@Valid String fullName) {
        return mapper.toAuthorDto(authorRepository.save(new Author(fullName)));
    }

    @Override
    @Transactional
    public AuthorDto update(long id, @Valid String fullName) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Author.class.getSimpleName(), id)));
        author.setFullName(fullName);
        return mapper.toAuthorDto(authorRepository.save(author));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        authorRepository.deleteById(id);
    }
}
