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
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "authors")
public class Author {
    @Id
    private String id;

    @NotBlank(message = "Author full name can't be blank")
    @Field(name = "full_name")
    @Indexed(unique = true)
    private String fullName;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ReadOnlyProperty
    @DocumentReference(lazy = true, lookup = "{ 'author':?#{#self._id} }")
    private List<Book> books;

    @CreatedDate
    private OffsetDateTime created;

    @LastModifiedDate
    private OffsetDateTime updated;

    @Version
    private Long version;

    public Author(String fullName) {
        this.fullName = fullName;
    }
}
