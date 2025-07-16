package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Book {
    private long id;

    private String title;

    private Author author;

    private List<Genre> genres;

    public void setBookId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book book)) {
            return false;
        }
        return getId() == book.getId() && Objects.equals(getTitle(), book.getTitle()) && Objects.equals(getAuthor(),
                book.getAuthor()) && Objects.equals(getGenres(), book.getGenres());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getAuthor(), getGenres());
    }

    @Override
    public String toString() {
        return "Id: %d, Title: %s, Author: %s, Genres: %s".formatted(id, title, author, genres);
    }
}
