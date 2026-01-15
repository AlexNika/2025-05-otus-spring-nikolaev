package ru.pricat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.pricat.model.File;

import java.util.UUID;

/**
 * Репозиторий для работы с сущностью File.
 * Предоставляет методы для поиска файлов по пользователю с пагинацией.
 */
public interface FileRepository extends JpaRepository<File, UUID> {

    /**
     * Находит все файлы пользователя с пагинацией.
     *
     * @param username имя пользователя
     * @param pageable параметры пагинации
     * @return страница с файлами пользователя
     */
    Page<File> findByUsernameOrderByUploadDateDesc(String username, Pageable pageable);

    /**
     * Проверяет существование файлов у пользователя.
     *
     * @param username имя пользователя
     * @return true если у пользователя есть файлы, иначе false
     */
    boolean existsByUsername(String username);

    /**
     * Подсчитывает общий размер всех файлов пользователя в байтах.
     *
     * @param username имя пользователя
     * @return общий размер файлов в байтах
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM File f WHERE f.username = :username")
    Long getTotalFileSizeByUsername(@Param("username") String username);
}
