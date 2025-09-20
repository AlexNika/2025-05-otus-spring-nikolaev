package ru.otus.hw.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq_gen")
    @SequenceGenerator(name = "comment_seq_gen", sequenceName = "comment_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Comment text can't be blank")
    @Column(nullable = false)
    private String text;

    @Column(name = "created_by")
    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updated;

    public Comment(Long id, String text, Book book) {
        this.id = id;
        this.text = text;
        this.book = book;
    }

    public Comment(String text, Book book) {
        this.text = text;
        this.book = book;
    }

    @PrePersist
    public void prePersist() {
        if (createdBy == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                createdBy = authentication.getName();
            } else {
                createdBy = "system";
            }
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Comment comment = (Comment) o;
        if (id == null) {
            return Objects.equals(text, comment.text) &&
                   Objects.equals(book, comment.book);
        }
        return Objects.equals(id, comment.id);
    }

    @Override
    public final int hashCode() {
        return id == null ? Objects.hash(text, book) : Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
               "id = " + id + ", " +
               "text = " + text + ")";
    }

}
