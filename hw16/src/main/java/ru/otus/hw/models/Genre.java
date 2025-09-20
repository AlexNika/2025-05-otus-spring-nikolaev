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
@Table(name = "genres")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "genre_seq_gen")
    @SequenceGenerator(name = "genre_seq_gen", sequenceName = "genre_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Genre name can't be blank")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updated;

    public Genre(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Genre(String name) {
        this.name = name;
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
        Genre genre = (Genre) o;
        if (id == null) {
            return Objects.equals(name, genre.name);
        }
        return Objects.equals(id, genre.id);
    }

    @Override
    public final int hashCode() {
        return id == null ? Objects.hash(name) : Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
               "id = " + id + ", " +
               "name = " + name + ")";
    }
}
