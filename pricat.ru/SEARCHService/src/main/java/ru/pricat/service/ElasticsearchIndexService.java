package ru.pricat.service;

public interface ElasticsearchIndexService {
    void applyDelta(String company, PriceListProcessingService.PriceListDelta delta);
}
