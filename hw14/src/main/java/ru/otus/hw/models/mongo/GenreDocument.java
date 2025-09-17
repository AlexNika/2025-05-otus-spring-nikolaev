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
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "genres")
public class GenreDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ReadOnlyProperty
    @DocumentReference(lazy = true, lookup = "{ 'genre':?#{#self._id} }")
    private List<BookDocument> books;

    @CreatedDate
    private OffsetDateTime created;

    @LastModifiedDate
    private OffsetDateTime updated;

    @Version
    private Long version;
}
