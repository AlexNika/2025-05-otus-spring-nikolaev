package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Genre {
    private long id;

    private String name;

    public void setGenreId(long id) {
        this.id = id;
    }

    public void setGenreName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Genre genre)) {
            return false;
        }
        return getId() == genre.getId() && Objects.equals(getName(), genre.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "Id: %d, Name: %s".formatted(id, name);
    }
}
