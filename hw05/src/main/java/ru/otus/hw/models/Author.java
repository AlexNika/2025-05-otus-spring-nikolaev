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
public class Author {
    private long id;

    private String fullName;

    public void setAuthorId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Author author)) {
            return false;
        }
        return getId() == author.getId() && Objects.equals(getFullName(), author.getFullName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFullName());
    }

    @Override
    public String toString() {
        return "Id: %d, FullName: %s".formatted(id, fullName);
    }


}
