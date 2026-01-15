package ru.otus.hw.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table("genres")
public class Genre {
    @Id
    private Long id;

    @NotBlank(message = "Genre name can't be blank")
    private String name;

    private LocalDateTime created;

    private LocalDateTime updated;

    public Genre(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Genre(String name) {
        this.name = name;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Genre that)) {
            return false;
        }
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public final int hashCode() {
        return Genre.class.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
               "id = " + id + ", " +
               "name = " + name + ")";
    }
}
