package ru.pricat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.pricat.model.PriceListProcessingHistory;

import java.util.UUID;

/**
 * Репозиторий для работы с историей обработки прайс-листов и message
 */
@Repository
public interface PriceListProcessingHistoryRepository extends JpaRepository<PriceListProcessingHistory, UUID> {
}
