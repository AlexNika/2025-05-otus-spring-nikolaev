package ru.pricat.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.pricat.model.entity.S3Event;
import ru.pricat.model.entity.S3EventTypes;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Компонент для преобразования JSON событий S3 в Entity.
 * Метод извлекает только необходимые бизнес-поля и сохраняет полное событие в JSONB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3EventConverter {

    private final ObjectMapper objectMapper;

    /**
     * Конвертирует JSON строку события S3 в Entity
     *
     * @param jsonEvent JSON строка с событием от MinIO
     * @return Entity с извлеченными полями и полным событием в JSONB
     * @throws RuntimeException если преобразование не удалось
     */
    public S3Event convertJsonToEntity(String jsonEvent) {
        try {
            JsonNode root = objectMapper.readTree(jsonEvent);

            return S3Event.builder()
                    .eventType(extractEventType(root))
                    .bucketName(extractBucketName(root))
                    .objectKey(extractObjectKey(root))
                    .objectSize(extractObjectSize(root))
                    .objectETag(extractObjectETag(root))
                    .objectContentType(extractObjectContentType(root))
                    .eventTime(extractEventTime(root))
                    .fullEventData(jsonEvent)
                    .build();

        } catch (Exception e) {
            log.error("Ошибка преобразования JSON в S3EventEntity: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось преобразовать S3 событие из JSON", e);
        }
    }

    private S3EventTypes extractEventType(JsonNode root) {
        String eventTypeStr = root.has("EventName") ? root.get("EventName").asText() : null;
        return eventTypeStr != null ?
                S3EventTypes.fromString(eventTypeStr) : null;
    }

    private String extractBucketName(JsonNode root) {
        return root.path("Records").path(0).path("s3").path("bucket")
                .path("name").asText(null);
    }

    private String extractObjectKey(JsonNode root) {
        String encodedKey = root.path("Records").path(0).path("s3")
                .path("object").path("key").asText(null);
        return encodedKey != null ?
                URLDecoder.decode(encodedKey, StandardCharsets.UTF_8) : null;
    }

    private Long extractObjectSize(JsonNode root) {
        JsonNode sizeNode = root.path("Records").path(0).path("s3")
                .path("object").path("size");
        return sizeNode.isMissingNode() ? null : sizeNode.asLong();
    }

    private String extractObjectETag(JsonNode root) {
        return root.path("Records").path(0).path("s3").path("object")
                .path("eTag").asText(null);
    }

    private String extractObjectContentType(JsonNode root) {
        return root.path("Records").path(0).path("s3").path("object")
                .path("contentType").asText(null);
    }

    private Instant extractEventTime(JsonNode root) {
        String eventTimeStr = root.path("Records").path(0)
                .path("eventTime").asText(null);
        return eventTimeStr != null ?
                Instant.from(DateTimeFormatter.ISO_DATE_TIME.parse(eventTimeStr)) : null;
    }
}
