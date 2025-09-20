package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.otus.hw.models.Genre;

@RepositoryRestResource(collectionResourceRel = "genres", path = "genre")
public interface GenreRepository extends JpaRepository<Genre, Long> {
}
