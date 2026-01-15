package ru.pricat.service.queue;

import ru.pricat.model.dto.s3.S3EventDto;

public interface EventQueueService {
    boolean addEvent(S3EventDto eventDto);

    void removeEventSignature(S3EventDto eventDto);

    void cleanupExpiredSignatures();

    S3EventDto takeEvent() throws InterruptedException;

    S3EventDto pollEvent();

    int getQueueSize();

    boolean isEmpty();

    void stop();

    void start();
}
