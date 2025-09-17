package ru.otus.hw.writers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.CommentDocument;
import ru.otus.hw.repositories.mongo.CommentMongoRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@Component
public class CommentWriter implements ItemWriter<CommentDocument>, AutoCloseable {

    private final CommentMongoRepository commentRepository;

    private final ExecutorService executorService;

    public CommentWriter(CommentMongoRepository commentRepository,
                         @Value("${batch.writer.threads:4}") int threadCount) {
        this.commentRepository = commentRepository;
        this.executorService = Executors.newFixedThreadPool(threadCount,
                r -> {
                    Thread t = new Thread(r, "comment-writer-" + r.hashCode());
                    t.setDaemon(false);
                    return t;
                }
        );
    }

    @Override
    public void write(Chunk<? extends CommentDocument> chunk) throws Exception {
        List<? extends CommentDocument> items = chunk.getItems();
        if (items.isEmpty()) {
            return;
        }
        log.debug("Writing {} comments to MongoDB", items.size());
        int batchSize = 100;
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < items.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, items.size());
                    List<? extends CommentDocument> batch = items.subList(i, endIndex);
                    commentRepository.saveAll(batch);
                    log.debug("Written batch of {} comments to MongoDB", batch.size());
                }
                log.debug("Successfully written {} comments to MongoDB", items.size());
            } catch (Exception e) {
                log.error("Error writing comments to MongoDB", e);
                throw new RuntimeException(e);
            }
        }, executorService);
        future.get(30, TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
