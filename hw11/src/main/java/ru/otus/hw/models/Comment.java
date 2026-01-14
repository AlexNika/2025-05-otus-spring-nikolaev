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
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("comments")
public class Comment {
    @Id
    private Long id;

    @NotBlank(message = "Comment text can't be blank")
    private String text;

    @Column("book_id")
    private Long bookId;

    @Transient
    private Book book;

    private LocalDateTime created;

    private LocalDateTime updated;

    public Comment(Long id, String text, Long bookId) {
        this.id = id;
        this.text = text;
        this.bookId = bookId;
    }

    public Comment(String text, Long bookId) {
        this.text = text;
        this.bookId = bookId;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Comment comment)) {
            return false;
        }
        return getId() != null && Objects.equals(getId(), comment.getId());
    }

    @Override
    public final int hashCode() {
        return Comment.class.hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
               "id = " + id + ", " +
               "text = " + text + ")";
    }

}
