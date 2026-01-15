package ru.pricat.service;

import ru.pricat.model.PriceItemMessage;

public interface BatchAggregatorService {
    void addMessage(PriceItemMessage message);
}
