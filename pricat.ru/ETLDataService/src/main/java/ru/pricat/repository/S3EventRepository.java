package ru.pricat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import ru.pricat.model.entity.ProcessingStatus;
import ru.pricat.model.entity.S3Event;
import ru.pricat.model.entity.S3EventTypes;

import java.util.List;

/**
 * Репозиторий для доступа к данным S3 событий
 * Наследует стандартные CRUD операции от JpaRepository
 */
@Repository
@SuppressWarnings({"SpellCheckingInspection", "unused"})
@RepositoryRestResource(path = "s3_events", collectionResourceRel = "s3_events")
public interface S3EventRepository extends JpaRepository<S3Event, Long> {

    /**
     * Метод находит все события по типу
     *
     * @param eventType тип события из enum S3EventTypes
     * @return список событий указанного типа
     */
    List<S3Event> findByEventType(S3EventTypes eventType);

    /**
     * Метод находит все события по имени бакета
     *
     * @param bucketName имя бакета
     * @return список событий в указанном бакете
     */
    List<S3Event> findByBucketName(String bucketName);

    /**
     * Метод находит события по шаблону ключа объекта
     *
     * @param keyPattern шаблон для поиска в ключах объектов
     * @return список событий с ключами, содержащими шаблон
     */
    List<S3Event> findByObjectKeyContaining(String keyPattern);

    /**
     * Метод находит события по комбинации бакета и типа события
     *
     * @param bucketName имя бакета
     * @param eventType  тип события из enum S3EventTypes
     * @return список событий, удовлетворяющих обоим условиям
     */
    List<S3Event> findByBucketNameAndEventType(String bucketName, S3EventTypes eventType);

    /**
     * Метод находит события определенных типов (например, все события создания)
     *
     * @param eventTypes список типов событий для поиска
     * @return список событий указанных типов
     */
    List<S3Event> findByEventTypeIn(List<S3EventTypes> eventTypes);

    /**
     * Метод находит все события с указанным статусом обработки
     *
     * @param status статус обработки из enum ProcessingStatus
     * @return список событий с указанным статусом обработки
     */
    List<S3Event> findByProcessingStatus(ProcessingStatus status);
}