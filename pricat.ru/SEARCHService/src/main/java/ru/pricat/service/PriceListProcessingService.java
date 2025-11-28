package ru.pricat.service;

import ru.pricat.model.PriceItemMessage;
import ru.pricat.model.PriceListCurrentState;

import java.util.List;
import java.util.UUID;

public interface PriceListProcessingService {

    void processPriceList(String company, UUID batchId, List<PriceItemMessage> messages);

    record PriceListDelta(
            List<PriceListCurrentState> addedProducts,
            List<PriceListCurrentState> updatedProducts,
            List<String> deletedProductIds
    ) {
        public int totalChanges() {
            return addedProducts.size() + updatedProducts.size() + deletedProductIds.size();
        }
    }
}
