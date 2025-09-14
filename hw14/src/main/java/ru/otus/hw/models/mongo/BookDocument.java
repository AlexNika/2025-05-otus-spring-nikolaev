package ru.otus.hw.models.mongo;

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
public class BookDocument {
    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    @Indexed(unique = true)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String title;

    @Indexed
    @DocumentReference(lazy = true)
    private AuthorDocument author;

    @Indexed
    @DocumentReference(lazy = true)
    private List<GenreDocument> genres;

    @ReadOnlyProperty
    @DocumentReference(lazy = true, lookup = "{ 'book':?#{#self._id} }")
    private List<CommentDocument> comments;

    @CreatedDate
    private OffsetDateTime created;

    @LastModifiedDate
    private OffsetDateTime updated;

    @Version
    private Long version;
}
