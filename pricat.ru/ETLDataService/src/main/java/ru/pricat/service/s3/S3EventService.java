package ru.pricat.service.s3;

import org.springframework.transaction.annotation.Transactional;
import ru.pricat.model.dto.s3.CreateS3EventRequest;
import ru.pricat.model.dto.s3.S3EventDto;
import ru.pricat.model.entity.ProcessingStatus;
import ru.pricat.model.entity.S3EventTypes;

import java.util.List;
import java.util.Optional;

public interface S3EventService {
    S3EventDto saveEventFromJson(String jsonEvent);

    S3EventDto saveEvent(CreateS3EventRequest request);

    @Transactional(readOnly = true)
    Optional<S3EventDto> findById(Long id);

    @Transactional(readOnly = true)
    List<S3EventDto> findByEventType(S3EventTypes eventType);

    @Transactional(readOnly = true)
    List<S3EventDto> findObjectCreatedEvents();

    @Transactional(readOnly = true)
    List<S3EventDto> findObjectRemovedEvents();

    @Transactional(readOnly = true)
    List<S3EventDto> findByBucketName(String bucketName);

    @Transactional(readOnly = true)
    List<S3EventDto> findByBucketAndEventType(String bucketName, S3EventTypes eventType);

    @Transactional(readOnly = true)
    List<S3EventDto> findAll();

    void deleteEvent(Long id);

    @Transactional(readOnly = true)
    Optional<String> getFullEventData(Long id);

    void updateEventStatus(Long eventId, ProcessingStatus status, String error);
}
