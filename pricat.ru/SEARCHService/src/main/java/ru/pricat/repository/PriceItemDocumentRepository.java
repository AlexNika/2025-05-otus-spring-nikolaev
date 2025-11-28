package ru.pricat.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import ru.pricat.model.PriceItemDocument;

/**
 * Репозиторий для работы с elasticsearch
 */
@Repository
public interface PriceItemDocumentRepository extends ElasticsearchRepository<PriceItemDocument, String> {
}
