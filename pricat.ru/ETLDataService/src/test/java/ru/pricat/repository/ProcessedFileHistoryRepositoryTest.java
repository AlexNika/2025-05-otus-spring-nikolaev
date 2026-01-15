package ru.pricat.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import ru.pricat.model.entity.ProcessedFileHistory;
import ru.pricat.model.entity.ProcessedFileHistory.ProcessingStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.default_schema=etldataprocessor"
})
@SuppressWarnings("unchecked")
public class ProcessedFileHistoryRepositoryTest {

    @Autowired
    private ProcessedFileHistoryRepository repository;

    @Autowired
    private TestEntityManager testEntityManager;

    private ProcessedFileHistory successFile;

    private final Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @BeforeEach
    void setUp() {
        successFile = ProcessedFileHistory.builder()
                .fileName("success.csv")
                .filePath("/data/input/success.csv")
                .fileHash("abc123def456")
                .fileSize(1024L)
                .company("testCompanyFolder")
                .processedAt(now.minusSeconds(100))
                .status(ProcessingStatus.SUCCESS)
                .recordsProcessed(100)
                .recordsFailed(0)
                .batchId(UUID.randomUUID())
                .processingTimeMs(500L)
                .s3ETag("etag-success-123")
                .build();

        ProcessedFileHistory failedFile = ProcessedFileHistory.builder()
                .fileName("failed.csv")
                .filePath("/data/input/failed.csv")
                .fileHash("def456ghi789")
                .fileSize(512L)
                .company("testCompanyFolder")
                .processedAt(now.minusSeconds(50))
                .status(ProcessingStatus.FAILED)
                .recordsProcessed(0)
                .recordsFailed(1)
                .errorMessage("File not found")
                .batchId(UUID.randomUUID())
                .build();

        ProcessedFileHistory partialFile = ProcessedFileHistory.builder()
                .fileName("partial.json")
                .filePath("/data/input/partial.json")
                .fileHash("xyz789uvw123")
                .fileSize(2048L)
                .company("anotherCompany")
                .processedAt(now.minusSeconds(30))
                .status(ProcessingStatus.PARTIAL)
                .recordsProcessed(50)
                .recordsFailed(5)
                .batchId(UUID.randomUUID())
                .build();

        testEntityManager.persistAndFlush(successFile);
        testEntityManager.persistAndFlush(failedFile);
        testEntityManager.persistAndFlush(partialFile);
    }

    @Test
    void shouldFindById() {
        Optional<ProcessedFileHistory> found = repository.findById(successFile.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFileName()).isEqualTo("success.csv");
        assertThat(found.get().getStatus()).isEqualTo(ProcessingStatus.SUCCESS);
    }

    @Test
    void shouldFindByFilePathAndFileHash() {
        Optional<ProcessedFileHistory> found = repository.findByFilePathAndFileHash(
                "/data/input/success.csv", "abc123def456");

        assertThat(found).isPresent();
        assertThat(found.get().getCompany()).isEqualTo("testCompanyFolder");
    }

    @Test
    void shouldReturnEmptyWhenFilePathOrHashNotFound() {
        Optional<ProcessedFileHistory> found = repository.findByFilePathAndFileHash("/not/found", "wronghash");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindByCorrelationId() {
        String corrId = "corr-12345";
        ProcessedFileHistory withCorrId = ProcessedFileHistory.builder()
                .fileName("corr-file.txt")
                .filePath("/tmp/corr.txt")
                .fileHash("hash123")
                .fileSize(100L)
                .company("testCompanyFolder")
                .processedAt(now)
                .status(ProcessingStatus.SUCCESS)
                .build();

        withCorrId.setCorrelationId(corrId);

        testEntityManager.persistAndFlush(withCorrId);

        Optional<ProcessedFileHistory> found = repository.findByCorrelationId(corrId);

        assertThat(found).isPresent();
        assertThat(found.get().getFileName()).isEqualTo("corr-file.txt");
    }

    @Test
    void shouldFindByBatchId() {
        Optional<ProcessedFileHistory> found = repository.findByBatchId(successFile.getBatchId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(ProcessingStatus.SUCCESS);
    }

    @Test
    void shouldFindByCompanyAndStatus() {
        List<ProcessedFileHistory> files = repository.findByCompanyAndStatus("testCompanyFolder", ProcessingStatus.SUCCESS);

        assertThat(files).hasSize(1);
        assertThat(files.getFirst().getFileName()).isEqualTo("success.csv");
    }

    @Test
    void shouldFindByProcessedAtAfter() {
        Instant after = now.minusSeconds(60);
        List<ProcessedFileHistory> files = repository.findByProcessedAtAfter(after);

        assertThat(files).hasSize(2); // failedFile и partialFile
        assertThat(files).extracting(ProcessedFileHistory::getProcessedAt)
                .allSatisfy(time -> assertThat(time).isAfterOrEqualTo(after));
    }

    @Test
    void shouldFindByProcessedAtBetween() {
        Instant start = now.minusSeconds(60);
        Instant end = now.minusSeconds(40);

        List<ProcessedFileHistory> files = repository.findByProcessedAtBetween(start, end);

        assertThat(files).hasSize(1);
        assertThat(files.getFirst().getFileName()).isEqualTo("failed.csv");
    }

    @Test
    void shouldCheckExistsByFilePathAndFileHash() {
        boolean exists = repository.existsByFilePathAndFileHash("/data/input/success.csv", "abc123def456");
        assertThat(exists).isTrue();

        boolean notExists = repository.existsByFilePathAndFileHash("/not/found", "wronghash");
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldFindTop10ByCompanyOrderByProcessedAtDesc() {
        List<ProcessedFileHistory> recent = repository.findTop10ByCompanyOrderByProcessedAtDesc("testCompanyFolder");

        assertThat(recent).hasSize(2);
        assertThat(recent.get(0).getFileName()).isEqualTo("failed.csv"); // последний по времени
        assertThat(recent.get(1).getFileName()).isEqualTo("success.csv");
    }

    @Test
    void shouldGetProcessingStatistics() {
        Instant since = now.minusSeconds(200);
        Object result = repository.getProcessingStatistics("testCompanyFolder", since);

        assertThat(result).isInstanceOf(Map.class);
        Map<String, Object> stats = (Map<String, Object>) result;

        assertThat(stats).containsKeys("totalFiles", "totalRecords", "avgProcessingTime", "successCount", "failedCount");
        assertThat(stats.get("totalFiles")).isEqualTo(2L);
        assertThat(stats.get("successCount")).isEqualTo(1L);
        assertThat(stats.get("failedCount")).isEqualTo(1L);
    }

    @Test
    void shouldFindPotentialDuplicates() {
        ProcessedFileHistory duplicate = ProcessedFileHistory.builder()
                .fileName("success.csv")
                .filePath("/old/success.csv")
                .fileHash("newhash999")
                .fileSize(1024L)
                .company("testCompanyFolder")
                .processedAt(now.minusSeconds(10))
                .status(ProcessingStatus.SUCCESS)
                .build();

        testEntityManager.persistAndFlush(duplicate);

        Instant since = now.minusSeconds(500);
        List<ProcessedFileHistory> duplicates = repository.findPotentialDuplicates("success.csv", 1024L, since);

        assertThat(duplicates).hasSize(2);
        assertThat(duplicates).extracting(ProcessedFileHistory::getFileName)
                .containsOnly("success.csv");
    }

    @Test
    void shouldDeleteOldRecords() {
        Instant cutoff = now.minusSeconds(80);
        int deleted = repository.deleteOldRecords(cutoff);

        assertThat(deleted).isEqualTo(1); // successFile старше

        Optional<ProcessedFileHistory> deletedFile = repository.findById(successFile.getId());
        assertThat(deletedFile).isEmpty();
    }

    @Test
    void shouldFindFilesForRetry() {
        Instant before = now.minusSeconds(40);
        List<ProcessedFileHistory> files = repository.findByStatusAndProcessedAtBefore(ProcessingStatus.FAILED, before);

        assertThat(files).hasSize(1);
        assertThat(files.getFirst().getFileName()).isEqualTo("failed.csv");
    }
}