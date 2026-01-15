package ru.otus.hw.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("books")
public class Book {
    @Id
    private Long id;

    @NotBlank(message = "Book title can't be blank")
    private String title;

    @Column("author_id")
    private Long authorId;

    @Transient
    private Author author;

    @Transient
    private Set<Genre> genres;

    @Transient
    private List<Comment> comments;

    private LocalDateTime created;

    private LocalDateTime updated;

    public Book(String title, Long authorId, Set<Genre> genres) {
        this.title = title;
        this.authorId = authorId;
        this.genres = genres;
    }

    public Book(Long id, String title, Long authorId, Set<Genre> genres) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.genres = genres;
    }

    public Book(String title, Long authorId, Set<Genre> genres, List<Comment> comments) {
        this.title = title;
        this.authorId = authorId;
        this.genres = genres;
        this.comments = comments;
    }

    public Book(Long id, String title, Long authorId, Set<Genre> genres, List<Comment> comments) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.genres = genres;
        this.comments = comments;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book book)) {
            return false;
        }
        return getId() != null && Objects.equals(getId(), book.getId());
    }

    @Override
    public final int hashCode() {
        return Book.class.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
               "id = " + id + ", " +
               "title = " + title + ")";
    }
}
