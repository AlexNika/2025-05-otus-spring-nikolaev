package ru.pricat.service.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pricat.converter.S3EventConverter;
import ru.pricat.model.dto.s3.CreateS3EventRequest;
import ru.pricat.model.dto.s3.S3EventDto;
import ru.pricat.model.dto.s3.mapper.S3EventMapper;
import ru.pricat.model.entity.ProcessingStatus;
import ru.pricat.model.entity.S3Event;
import ru.pricat.model.entity.S3EventTypes;
import ru.pricat.repository.S3EventRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с S3 событиями
 * Обеспечивает бизнес-логику и преобразование между Entity/DTO
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("SpellCheckingInspection")
public class S3EventServiceImpl implements S3EventService {

    private final S3EventRepository s3EventRepository;

    private final S3EventConverter s3EventConverter;

    private final S3EventMapper s3EventMapper;

    /**
     * Метод сохраняет событие из JSON строки
     *
     * @param jsonEvent JSON строка с событием от MinIO
     * @return DTO сохраненного события (без fullEventData)
     */
    @Override
    public S3EventDto saveEventFromJson(String jsonEvent) {
        log.info("Обработка S3 события из JSON");
        S3Event entity = s3EventConverter.convertJsonToEntity(jsonEvent);
        S3Event saved = s3EventRepository.save(entity);
        log.info("S3 событие сохранено с ID: {}, Тип: {}", saved.getId(), saved.getEventType());
        return s3EventMapper.toS3EventDto(saved);
    }

    /**
     * Метод сохраняет событие из DTO запроса
     *
     * @param request DTO с данными для создания события
     * @return DTO сохраненного события
     */
    @Override
    public S3EventDto saveEvent(CreateS3EventRequest request) {
        S3Event entity = s3EventMapper.toEntity(request);
        S3Event saved = s3EventRepository.save(entity);
        return s3EventMapper.toS3EventDto(saved);
    }

    /**
     * Метод находит событие по ID
     *
     * @param id идентификатор события
     * @return Optional с DTO события если найдено
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<S3EventDto> findById(Long id) {
        return s3EventRepository.findById(id)
                .map(s3EventMapper::toS3EventDto);
    }

    /**
     * Метод находит все события по типу
     *
     * @param eventType тип события для фильтрации
     * @return список DTO событий указанного типа
     */
    @Transactional(readOnly = true)
    @Override
    public List<S3EventDto> findByEventType(S3EventTypes eventType) {
        return s3EventMapper.toS3EventList(s3EventRepository.findByEventType(eventType));
    }

    /**
     * Метод находит все события создания объектов
     *
     * @return список DTO событий создания объектов
     */
    @Transactional(readOnly = true)
    @Override
    public List<S3EventDto> findObjectCreatedEvents() {
        List<S3EventTypes> createdEvents = List.of(
                S3EventTypes.S3_OBJECTCREATED_COMPLETEMULTIPARTUPLOAD,
                S3EventTypes.S3_OBJECTCREATED_COPY,
                S3EventTypes.S3_OBJECTCREATED_DELETETAGGING,
                S3EventTypes.S3_OBJECTCREATED_POST,
                S3EventTypes.S3_OBJECTCREATED_PUT,
                S3EventTypes.S3_OBJECTCREATED_PUTLEGALHOLD,
                S3EventTypes.S3_OBJECTCREATED_PUTRETENTION,
                S3EventTypes.S3_OBJECTCREATED_PUTTAGGING
        );
        return s3EventMapper.toS3EventList(s3EventRepository.findByEventTypeIn(createdEvents));
    }

    /**
     * Метод находит все события удаления объектов
     *
     * @return список DTO событий удаления объектов
     */
    @Transactional(readOnly = true)
    @Override
    public List<S3EventDto> findObjectRemovedEvents() {
        List<S3EventTypes> removedEvents = List.of(
                S3EventTypes.S3_OBJECTREMOVED_DELETE,
                S3EventTypes.S3_OBJECTREMOVED_DELETEMARKERCREATED
        );
        return s3EventMapper.toS3EventList(s3EventRepository.findByEventTypeIn(removedEvents));
    }

    /**
     * Метод находит все события по имени бакета
     *
     * @param bucketName имя бакета для фильтрации
     * @return список DTO событий в указанном бакете
     */
    @Transactional(readOnly = true)
    @Override
    public List<S3EventDto> findByBucketName(String bucketName) {
        return s3EventMapper.toS3EventList(s3EventRepository.findByBucketName(bucketName));
    }

    /**
     * Метод находит события по комбинации бакета и типа
     *
     * @param bucketName имя бакета
     * @param eventType  тип события
     * @return список DTO событий, удовлетворяющих условиям
     */
    @Transactional(readOnly = true)
    @Override
    public List<S3EventDto> findByBucketAndEventType(String bucketName, S3EventTypes eventType) {
        return s3EventMapper.toS3EventList(
                s3EventRepository.findByBucketNameAndEventType(bucketName, eventType)
        );
    }

    /**
     * Метод возвращает все события
     *
     * @return список всех DTO событий
     */
    @Transactional(readOnly = true)
    @Override
    public List<S3EventDto> findAll() {
        return s3EventMapper.toS3EventList(s3EventRepository.findAll());
    }

    /**
     * Метод удаляет событие по ID
     *
     * @param id идентификатор события для удаления
     */
    @Transactional
    @Override
    public void deleteEvent(Long id) {
        s3EventRepository.deleteById(id);
        log.info("S3 событие удалено с ID: {}", id);
    }

    /**
     * Метод получает полное событие в JSON по ID (для внутреннего использования)
     *
     * @param id идентификатор события
     * @return полное событие в JSON формате
     */
    @Transactional(readOnly = true)
    @Override
    public Optional<String> getFullEventData(Long id) {
        return s3EventRepository.findById(id)
                .map(S3Event::getFullEventData);
    }

    /**
     * Метод обновляет статус обработки события
     */
    @Override
    public void updateEventStatus(Long eventId, ProcessingStatus status, String error) {
        s3EventRepository.findById(eventId).ifPresent(event -> {
            event.setProcessingStatus(status);
            event.setProcessingAttempts(event.getProcessingAttempts() + 1);
            if (error != null) {
                event.setLastError(error);
            }
            if (status == ProcessingStatus.COMPLETED) {
                event.setProcessedAt(Instant.now());
            }
            s3EventRepository.save(event);
            log.debug("Event {} status updated to: {}", eventId, status);
        });
    }
}
