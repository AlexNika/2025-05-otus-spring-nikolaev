package ru.otus.hw.writers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.mongo.BookDocument;
import ru.otus.hw.repositories.mongo.BookMongoRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@Component
public class BookWriter implements ItemWriter<BookDocument>, AutoCloseable {

    private final BookMongoRepository bookRepository;

    private final ExecutorService executorService;

    public BookWriter(BookMongoRepository bookRepository,
                      @Value("${batch.writer.threads:4}") int threadCount) {
        this.bookRepository = bookRepository;
        this.executorService = Executors.newFixedThreadPool(threadCount,
                r -> {
                    Thread t = new Thread(r, "book-writer-" + r.hashCode());
                    t.setDaemon(false);
                    return t;
                }
        );
    }

    @Override
    public void write(Chunk<? extends BookDocument> chunk) throws Exception {
        List<? extends BookDocument> items = chunk.getItems();
        if (items.isEmpty()) {
            return;
        }
        log.debug("Writing {} books to MongoDB", items.size());
        int batchSize = 50;
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                for (int i = 0; i < items.size(); i += batchSize) {
                    int endIndex = Math.min(i + batchSize, items.size());
                    List<? extends BookDocument> batch = items.subList(i, endIndex);
                    bookRepository.saveAll(batch);
                    log.debug("Written batch of {} books to MongoDB", batch.size());
                }
                log.debug("Successfully written {} books to MongoDB", items.size());
            } catch (Exception e) {
                log.error("Error writing books to MongoDB", e);
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