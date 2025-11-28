package ru.pricat.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Enum для типов событий S3 из MinIO
 */
@Getter
@AllArgsConstructor
@SuppressWarnings("SpellCheckingInspection")
public enum S3EventTypes {
    S3_OBJECTACCESSED_GET("s3:ObjectAccessed:Get"),
    S3_OBJECTACCESSED_GETLEGALHOLD("s3:ObjectAccessed:GetLegalHold"),
    S3_OBJECTACCESSED_GETRETENTION("s3:ObjectAccessed:GetRetention"),
    S3_OBJECTACCESSED_HEAD("s3:ObjectAccessed:Head"),

    S3_OBJECTCREATED_COMPLETEMULTIPARTUPLOAD("s3:ObjectCreated:CompleteMultipartUpload"),
    S3_OBJECTCREATED_COPY("s3:ObjectCreated:Copy"),
    S3_OBJECTCREATED_DELETETAGGING("s3:ObjectCreated:DeleteTagging"),
    S3_OBJECTCREATED_POST("s3:ObjectCreated:Post"),
    S3_OBJECTCREATED_PUT("s3:ObjectCreated:Put"),
    S3_OBJECTCREATED_PUTLEGALHOLD("s3:ObjectCreated:PutLegalHold"),
    S3_OBJECTCREATED_PUTRETENTION("s3:ObjectCreated:PutRetention"),
    S3_OBJECTCREATED_PUTTAGGING("s3:ObjectCreated:PutTagging"),

    S3_OBJECTREMOVED_DELETE("s3:ObjectRemoved:Delete"),
    S3_OBJECTREMOVED_DELETEMARKERCREATED("s3:ObjectRemoved:DeleteMarkerCreated"),

    S3_REPLICATION_OPERATIONCOMPLETEDREPLICATION("s3:Replication:OperationCompletedReplication"),
    S3_REPLICATION_OPERATIONFAILEDREPLICATION("s3:Replication:OperationFailedReplication"),
    S3_REPLICATION_OPERATIONMISSEDTHRESHOLD("s3:Replication:OperationMissedThreshold"),
    S3_REPLICATION_OPERATIONNOTTRACKED("s3:Replication:OperationNotTracked"),
    S3_REPLICATION_OPERATIONREPLICATEDAFTERTHRESHOLD("s3:Replication:OperationReplicatedAfterThreshold"),

    S3_OBJECTRESTORE_POST("s3:ObjectRestore:Post"),
    S3_OBJECTRESTORE_COMPLETED("s3:ObjectRestore:Completed"),
    S3_OBJECTTRANSITION_FAILED("s3:ObjectTransition:Failed"),
    S3_OBJECTTRANSITION_COMPLETE("s3:ObjectTransition:Complete"),

    S3_SCANNER_MANYVERSIONS("s3:Scanner:ManyVersions"),
    S3_SCANNER_BIGPREFIX("s3:Scanner:BigPrefix"),

    S3_BUCKETCREATED("s3:BucketCreated"),
    S3_BUCKETREMOVED("s3:BucketRemoved");

    private final String eventTypeString;

    /**
     * Конвертирует строковое представление события в enum
     *
     * @param eventTypeString строковое представление события S3
     * @return соответствующий enum или null если не найден
     */
    public static S3EventTypes fromString(String eventTypeString) {
        return Arrays.stream(S3EventTypes.values())
                .filter(type -> type.getEventTypeString().equals(eventTypeString))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(MessageFormat.format("Unknown S3 event type: {0}",
                                eventTypeString)));
    }

    /**
     * Проверяет, является ли событие событием создания объекта
     *
     * @return true если событие относится к созданию объекта
     */
    public boolean isObjectCreated() {
        return this.eventTypeString.startsWith("s3:ObjectCreated:");
    }

    /**
     * Проверяет, является ли событие событием удаления объекта
     *
     * @return true если событие относится к удалению объекта
     */
    public boolean isObjectRemoved() {
        return this.eventTypeString.startsWith("s3:ObjectRemoved:");
    }

    /**
     * Проверяет, является ли событие событием доступа к объекту
     *
     * @return true если событие относится к доступу к объекту
     */
    public boolean isObjectAccessed() {
        return this.eventTypeString.startsWith("s3:ObjectAccessed:");
    }
}
