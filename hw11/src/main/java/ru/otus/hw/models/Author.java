package ru.otus.hw.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("authors")
public class Author {
    @Id
    private Long id;

    @NotBlank(message = "Author full name can't be blank")
    @Column("full_name")
    private String fullName;

    private LocalDateTime created;

    private LocalDateTime updated;

    public Author(Long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public Author(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Author that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public final int hashCode() {
        return Author.class.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
               "id = " + id + ", " +
               "fullName = " + fullName + ")";
    }
}
