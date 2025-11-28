package ru.pricat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.pricat.model.PriceItemDocument;
import ru.pricat.model.PriceListCurrentState;
import ru.pricat.repository.PriceItemDocumentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElasticsearchIndexServiceImpl implements ElasticsearchIndexService {

    private final PriceItemDocumentRepository documentRepository;

    @Override
    public void applyDelta(String company, PriceListProcessingService.PriceListDelta delta) {
        List<PriceItemDocument> documentsToSave = delta.addedProducts().stream()
                .map(this::mapToDocument)
                .collect(Collectors.toList());
        documentsToSave.addAll(
                delta.updatedProducts().stream()
                        .map(this::mapToDocument)
                        .toList()
        );
        if (!documentsToSave.isEmpty()) {
            documentRepository.saveAll(documentsToSave);
        }
        if (!delta.deletedProductIds().isEmpty()) {
            documentRepository.deleteAllById(delta.deletedProductIds());
        }
    }

    private PriceItemDocument mapToDocument(PriceListCurrentState state) {
        PriceItemDocument doc = new PriceItemDocument();
        doc.setId(state.getProductId());
        doc.setCompanyId(state.getCompany());
        doc.setProductName(state.getProductName());
        doc.setDescription(state.getDescription());
        doc.setPrice(state.getPrice());
        doc.setCategory(state.getCategory());
        doc.setManufacturer(state.getManufacturer());
        doc.setStockQuantity(state.getStockQuantity());
        doc.setFileProcessedAt(state.getFileProcessedAt());
        return doc;
    }
}
