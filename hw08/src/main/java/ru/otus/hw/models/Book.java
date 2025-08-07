package ru.otus.hw.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class Book {
    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "Book title can't be blank")
    @EqualsAndHashCode.Include
    @ToString.Include
    private String title;

    @Indexed
    @DocumentReference(lazy = true)
    private Author author;

    @Indexed
    @DocumentReference(lazy = true)
    private List<Genre> genres;

    @ReadOnlyProperty
    @DocumentReference(lazy = true, lookup = "{ 'book':?#{#self._id} }")
    private List<Comment> comments;

    @CreatedDate
    private OffsetDateTime created;

    @LastModifiedDate
    private OffsetDateTime updated;

    @Version
    private Long version;

    public Book(String title, Author author) {
        this.title = title;
        this.author = author;
    }

    public Book(String title, Author author, List<Genre> genres) {
        this.title = title;
        this.author = author;
        this.genres = genres;
    }

    public Book(String id, String title, Author author, List<Genre> genres) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genres = genres;
    }

    public Book(String title, Author author, List<Genre> genres, List<Comment> comments) {
        this.title = title;
        this.author = author;
        this.genres = genres;
        this.comments = comments;
    }

    public Book(String id, String title, Author author, List<Genre> genres, List<Comment> comments) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genres = genres;
        this.comments = comments;
    }
}
