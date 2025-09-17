package ru.otus.hw.writers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.GenreDocument;
import ru.otus.hw.repositories.mongo.GenreMongoRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class GenreWriter implements ItemWriter<GenreDocument>, AutoCloseable {

    private final GenreMongoRepository genreRepository;

    private final ExecutorService executorService;

    public GenreWriter(GenreMongoRepository genreRepository,
                       @Value("${batch.writer.threads:2}") int threadCount) {
        this.genreRepository = genreRepository;
        this.executorService = Executors.newFixedThreadPool(threadCount,
                r -> {
                    Thread t = new Thread(r, "genre-writer-" + r.hashCode());
                    t.setDaemon(false);
                    return t;
                }
        );
    }

    @Override
    public void write(Chunk<? extends GenreDocument> chunk) throws Exception {
        List<? extends GenreDocument> items = chunk.getItems();
        if (items.isEmpty()) {
            return;
        }
        log.debug("Writing {} genres to MongoDB", items.size());
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                genreRepository.saveAll(items);
                log.debug("Successfully written {} genres to MongoDB", items.size());
            } catch (Exception e) {
                log.error("Error writing genres to MongoDB", e);
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
