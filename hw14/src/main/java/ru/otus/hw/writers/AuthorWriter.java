package ru.otus.hw.writers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.AuthorDocument;
import ru.otus.hw.repositories.mongo.AuthorMongoRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@Component
public class AuthorWriter implements ItemWriter<AuthorDocument>, AutoCloseable {

    private final AuthorMongoRepository authorRepository;

    private final ExecutorService executorService;

    public AuthorWriter(AuthorMongoRepository authorRepository,
                        @Value("${batch.writer.threads:4}") int threadCount) {
        this.authorRepository = authorRepository;
        this.executorService = Executors.newFixedThreadPool(threadCount,
                r -> {
                    Thread t = new Thread(r, "author-writer-" + r.hashCode());
                    t.setDaemon(false);
                    return t;
                }
        );
    }

    @Override
    public void write(Chunk<? extends AuthorDocument> chunk) throws Exception {
        List<? extends AuthorDocument> items = chunk.getItems();
        if (items.isEmpty()) {
            return;
        }
        log.debug("Writing {} authors to MongoDB", items.size());
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                authorRepository.saveAll(items);
                log.debug("Successfully written {} authors to MongoDB", items.size());
            } catch (Exception e) {
                log.error("Error writing authors to MongoDB", e);
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