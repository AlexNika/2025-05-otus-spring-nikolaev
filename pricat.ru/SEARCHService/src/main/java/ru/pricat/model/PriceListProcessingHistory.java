package ru.pricat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pricelist_processing_history", schema = "datasearchengine")
@Getter
@Setter
public class PriceListProcessingHistory {

    public enum ProcessingStatus {
        SUCCESS, FAILED, PROCESSING
    }

    @Id
    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "company", nullable = false)
    private String company;

    @Column(name = "file_processed_at", nullable = false)
    private Instant fileProcessedAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "indexed_at")
    private Instant indexedAt;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems;

    @Column(name = "processed_items")
    private Integer processedItems;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProcessingStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
