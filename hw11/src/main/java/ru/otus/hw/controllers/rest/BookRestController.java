package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CreateUpdateBookDto;
import ru.otus.hw.services.BookService;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/books")
public class BookRestController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<Flux<BookDto>> getAllBooks() {
        Flux<BookDto> books = bookService.findAll();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public Mono<BookDto> getBook(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PostMapping
    public Mono<BookDto> createBook(@Valid @RequestBody CreateUpdateBookDto bookDto) {
        return bookService.insert(bookDto.title(), bookDto.authorId(), Set.copyOf(bookDto.genreIds()));
    }

    @PutMapping("/{id}")
    public Mono<BookDto> updateBook(@PathVariable Long id,
                                    @Valid @RequestBody CreateUpdateBookDto bookDto) {
        return bookService.update(id, bookDto.title(), bookDto.authorId(),
                Set.copyOf(bookDto.genreIds()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteBook(@PathVariable Long id) {
        return bookService.deleteById(id)
            .thenReturn(ResponseEntity.noContent().<Void>build())
            .onErrorReturn(ResponseEntity.notFound().build());
    }
}