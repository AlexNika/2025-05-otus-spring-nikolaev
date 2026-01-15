package ru.pricat.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

/**
 * Базовый класс для всех Entity с общими полями.
 */
@Getter
@Setter
@MappedSuperclass
@FieldDefaults(level = PRIVATE)
public abstract class BaseEntity {

    @Column(name = "correlation_id", nullable = false, unique = true, length = 39)
    String correlationId = MessageFormat.format("CID{0}", UUID.randomUUID().toString());

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @Version
    @Column(name = "version")
    Long version;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseEntity that)) return false;
        return Objects.equals(getCorrelationId(), that.getCorrelationId()) &&
               Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
               Objects.equals(getUpdatedAt(), that.getUpdatedAt()) &&
               Objects.equals(getVersion(), that.getVersion());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCorrelationId(), getCreatedAt(), getUpdatedAt(), getVersion());
    }
}