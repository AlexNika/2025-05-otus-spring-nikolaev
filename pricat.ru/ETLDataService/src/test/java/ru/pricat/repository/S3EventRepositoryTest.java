package ru.pricat.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import ru.pricat.model.entity.ProcessingStatus;
import ru.pricat.model.entity.S3Event;
import ru.pricat.model.entity.S3EventTypes;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.default_schema=etldataprocessor"
})
public class S3EventRepositoryTest {

    @Autowired
    private S3EventRepository s3EventRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @BeforeEach
    void setUp() {
        S3Event eventPut = S3Event.builder()
                .eventType(S3EventTypes.S3_OBJECTCREATED_PUT)
                .bucketName("my-bucket")
                .objectKey("data/file.csv")
                .objectSize(1024L)
                .objectETag("abc123")
                .objectContentType("text/csv")
                .eventTime(Instant.now().minusSeconds(100))
                .fullEventData("{\"event\": \"put\"}")
                .processingStatus(ProcessingStatus.RECEIVED)
                .processingAttempts(0)
                .build();

        S3Event eventDelete = S3Event.builder()
                .eventType(S3EventTypes.S3_OBJECTREMOVED_DELETE)
                .bucketName("my-bucket")
                .objectKey("data/old.txt")
                .objectSize(null)
                .objectETag("def456")
                .eventTime(Instant.now().minusSeconds(50))
                .fullEventData("{\"event\": \"delete\"}")
                .processingStatus(ProcessingStatus.FAILED)
                .processingAttempts(3)
                .lastError("Processing failed")
                .build();

        S3Event eventRestore = S3Event.builder()
                .eventType(S3EventTypes.S3_OBJECTRESTORE_POST)
                .bucketName("archive-bucket")
                .objectKey("backup.zip")
                .objectSize(2048L)
                .eventTime(Instant.now())
                .fullEventData("{\"event\": \"restore\"}")
                .processingStatus(ProcessingStatus.COMPLETED)
                .processingAttempts(1)
                .processedAt(Instant.now())
                .build();

        testEntityManager.persistAndFlush(eventPut);
        testEntityManager.persistAndFlush(eventDelete);
        testEntityManager.persistAndFlush(eventRestore);
    }

    @Test
    void shouldFindEventsByEventType() {
        List<S3Event> events = s3EventRepository.findByEventType(S3EventTypes.S3_OBJECTCREATED_PUT);

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().getBucketName()).isEqualTo("my-bucket");
        assertThat(events.getFirst().getProcessingStatus()).isEqualTo(ProcessingStatus.RECEIVED);
    }

    @Test
    void shouldFindEventsByBucketName() {
        List<S3Event> events = s3EventRepository.findByBucketName("my-bucket");

        assertThat(events).hasSize(2);
        assertThat(events).extracting(S3Event::getEventType)
                .containsOnly(S3EventTypes.S3_OBJECTCREATED_PUT, S3EventTypes.S3_OBJECTREMOVED_DELETE);
    }

    @Test
    void shouldFindEventsByObjectKeyContaining() {
        List<S3Event> events = s3EventRepository.findByObjectKeyContaining("data/");

        assertThat(events).hasSize(2);
        assertThat(events).extracting(S3Event::getObjectKey)
                .contains("data/file.csv", "data/old.txt");
    }

    @Test
    void shouldFindEventsByBucketNameAndEventType() {
        List<S3Event> events = s3EventRepository.findByBucketNameAndEventType(
                "my-bucket", S3EventTypes.S3_OBJECTREMOVED_DELETE);

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().getLastError()).isEqualTo("Processing failed");
    }

    @Test
    void shouldFindEventsByEventTypeIn() {
        List<S3EventTypes> types = List.of(S3EventTypes.S3_OBJECTCREATED_PUT, S3EventTypes.S3_OBJECTRESTORE_POST);

        List<S3Event> events = s3EventRepository.findByEventTypeIn(types);

        assertThat(events).hasSize(2);
        assertThat(events).extracting(S3Event::getEventType)
                .containsExactlyInAnyOrder(S3EventTypes.S3_OBJECTCREATED_PUT, S3EventTypes.S3_OBJECTRESTORE_POST);
    }

    @Test
    void shouldReturnEmptyList_WhenNoEventsMatchCriteria() {
        List<S3Event> events = s3EventRepository.findByBucketName("nonexistent-bucket");

        assertThat(events).isEmpty();
    }

    @Test
    void shouldFindCompletedEventsWithProcessedAt() {
        List<S3Event> events = s3EventRepository.findByProcessingStatus(ProcessingStatus.COMPLETED);

        assertThat(events).hasSize(1);
        assertThat(events.getFirst().getProcessedAt()).isNotNull();
        assertThat(events.getFirst().getBucketName()).isEqualTo("archive-bucket");
    }
}