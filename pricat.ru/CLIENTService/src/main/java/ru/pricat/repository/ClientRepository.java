package ru.pricat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pricat.model.Client;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы с клиентами в базе данных.
 * Предоставляет методы для выполнения CRUD операций и поиска клиентов.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    /**
     * Находит клиента по имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return Optional с клиентом, если найден, иначе empty Optional
     */
    Optional<Client> findByUsername(String username);

    /**
     * Находит клиента по email адресу.
     *
     * @param email email адрес для поиска
     * @return Optional с клиентом, если найден, иначе empty Optional
     */
    Optional<Client> findByEmail(String email);

    /**
     * Проверяет существование клиента с указанным именем пользователя.
     *
     * @param username имя пользователя для проверки
     * @return true если клиент с таким именем существует, иначе false
     */
    boolean existsByUsername(String username);

    /**
     * Проверяет существование клиента с указанным email адресом.
     *
     * @param email email адрес для проверки
     * @return true если клиент с таким email существует, иначе false
     */
    boolean existsByEmail(String email);

    /**
     * Удаляет клиента по имени пользователя.
     *
     * @param username имя пользователя клиента для удаления
     */
    void deleteByUsername(String username);
}
