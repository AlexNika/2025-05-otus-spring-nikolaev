package ru.pricat.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

/**
 * Конфигурационный класс для инициализации индекса Elasticsearch.
 * При запуске приложения проверяет наличие индекса 'price-items' и создает его,
 * если он не существует, используя маппинг из файла 'pricelist-mapping.json'.
 */
@Slf4j
@Component
public class ElasticsearchIndexConfig {

    /**
     * Клиент для взаимодействия с Elasticsearch.
     */
    private final ElasticsearchClient esClient;

    /**
     * Ресурс, представляющий файл маппинга индекса, расположенный в classpath.
     */
    private final Resource indexConfig;

    /**
     * Конструктор класса.
     *
     * @param esClient     клиент Elasticsearch
     * @param indexConfig  ресурс файла маппинга индекса (по умолчанию classpath:mappings/pricelist-mapping.json)
     */
    public ElasticsearchIndexConfig(ElasticsearchClient esClient,
                                    @org.springframework.beans.factory.annotation
                                            .Value("classpath:mappings/pricelist-mapping.json") Resource indexConfig) {
        this.esClient = esClient;
        this.indexConfig = indexConfig;
    }

    /**
     * Метод, вызываемый после инициализации бина.
     * Проверяет существование индекса 'price-items' и создает его с использованием
     * маппинга из файла 'pricelist-mapping.json', если индекс отсутствует.
     *
     * @throws RuntimeException если возникает ошибка при взаимодействии с Elasticsearch
     *                          или при чтении файла маппинга
     */
    @PostConstruct
    public void createIndex() {
        String indexName = "price-items";
        try {
            boolean exists = esClient.indices()
                .exists(b -> b.index(indexName))
                .value();
            if (!exists) {
                CreateIndexRequest request = CreateIndexRequest.of(builder -> {
                            try {
                                return builder
                                    .index(indexName)
                                    .withJson(indexConfig.getInputStream());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
                esClient.indices().create(request);
                log.info("Elasticsearch index created successfully: {}", indexName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create Elasticsearch index: " + indexName, e);
        }
    }
}