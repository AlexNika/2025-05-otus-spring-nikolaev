package ru.otus.hw.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "author_seq_gen")
    @SequenceGenerator(name = "author_seq_gen", sequenceName = "author_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Author full name can't be blank")
    @Column(name = "full_name", nullable = false, unique = true)
    private String fullName;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updated;

    public Author(Long id, String fullName) {
        this.id = id;
        this.fullName = fullName;
    }

    public Author(String fullName) {
        this.fullName = fullName;
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
        Author author = (Author) o;
        if (id == null) {
            return Objects.equals(fullName, author.fullName);
        }
        return Objects.equals(id, author.id);
    }

    @Override
    public final int hashCode() {
        return id == null ? Objects.hash(fullName) : Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
               "id = " + id + ", " +
               "fullName = " + fullName + ")";
    }
}
