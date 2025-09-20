package ru.otus.hw.indicators;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LibraryContentHealthIndicator implements HealthIndicator {

    private final AuthorRepository authorRepository;

    private final BookRepository bookRepository;

    private final GenreRepository genreRepository;

    @Override
    public Health health() {
        long authorsCount = authorRepository.count();
        long booksCount = bookRepository.count();
        long genresCount = genreRepository.count();
        Map<String, Long> details = Map.of(
                "Authors count", authorsCount,
                "Books count", booksCount,
                "Genres count", genresCount
        );
        if (authorsCount > 0 && booksCount > 0 && genresCount > 0) {
            return Health
                    .status(Status.UP)
                    .up()
                    .withDetail("message", "Первичное заполнение библиотеки выполнено!")
                    .withDetails(details)
                    .build();
        }
        return Health
                .status(Status.DOWN)
                .down()
                .withDetail("message", "Библиотека не наполнена!")
                .withDetails(details)
                .build();
    }

}
