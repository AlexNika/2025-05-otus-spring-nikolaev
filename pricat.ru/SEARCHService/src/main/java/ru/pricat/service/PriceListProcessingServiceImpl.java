package ru.pricat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pricat.model.dto.PriceItemDto;
import ru.pricat.model.PriceItemMessage;
import ru.pricat.model.PriceListCurrentState;
import ru.pricat.model.PriceListProcessingHistory;
import ru.pricat.repository.PriceListCurrentStateRepository;
import ru.pricat.repository.PriceListProcessingHistoryRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.pricat.model.PriceListProcessingHistory.ProcessingStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceListProcessingServiceImpl implements PriceListProcessingService {

    private final PriceListProcessingHistoryRepository historyRepository;

    private final PriceListCurrentStateRepository stateRepository;

    private final ElasticsearchIndexService elasticsearchIndexService;

    @Override
    @Transactional
    public void processPriceList(String company, UUID batchId, List<PriceItemMessage> messages) {
        log.info("Начало обработки прайс-листа: company={}, batchId={}, items={}", company, batchId, messages.size());

        PriceListProcessingHistory history = new PriceListProcessingHistory();
        history.setBatchId(batchId);
        history.setCompany(company);
        history.setFileProcessedAt(messages.getFirst().fileProcessedAt());
        history.setReceivedAt(Instant.now());
        history.setTotalItems(messages.size());
        history.setStatus(ProcessingStatus.PROCESSING);
        historyRepository.save(history);

        try {
            List<PriceListCurrentState> currentState = stateRepository.findByCompany(company);
            Map<String, PriceListCurrentState> currentMap = currentState.stream()
                    .collect(Collectors.toMap(PriceListCurrentState::getProductId, item -> item));
            List<PriceListCurrentState> newState = messages.stream()
                    .map(this::mapToStateEntity)
                    .collect(Collectors.toList());
            Map<String, PriceListCurrentState> newMap = newState.stream()
                    .collect(Collectors.toMap(PriceListCurrentState::getProductId, item -> item));
            PriceListDelta delta = calculateDelta(currentMap, newMap);
            elasticsearchIndexService.applyDelta(company, delta);

            stateRepository.deleteByCompany(company);
            if (!newState.isEmpty()) {
                stateRepository.saveAll(newState);
            }
            history.setProcessedItems(delta.totalChanges());
            history.setIndexedAt(Instant.now());
            history.setStatus(ProcessingStatus.SUCCESS);
            historyRepository.save(history);
            log.info("Прайс-лист успешно обработан: company={}, batchId={}, added={}, updated={}, deleted={}",
                    company, batchId, delta.addedProducts().size(), delta.updatedProducts().size(),
                    delta.deletedProductIds().size());
        } catch (Exception e) {
            log.error("Ошибка обработки прайс-листа: company={}, batchId={}", company, batchId, e);
            history.setStatus(ProcessingStatus.FAILED);
            history.setErrorMessage(e.getMessage());
            historyRepository.save(history);
            throw new RuntimeException("Ошибка индексации прайс-листа", e);
        }
    }

    private PriceListCurrentState mapToStateEntity(PriceItemMessage message) {
        PriceItemDto dto = message.priceItem();
        PriceListCurrentState state = new PriceListCurrentState();
        state.setCompany(message.company());
        state.setProductId(dto.getProductId());
        state.setProductName(dto.getProductName());
        state.setDescription(dto.getDescription());
        state.setPrice(dto.getPrice());
        state.setCurrency(dto.getCurrency());
        state.setStockQuantity(dto.getStockQuantity());
        state.setCategory(dto.getCategory());
        state.setManufacturer(dto.getManufacturer());
        state.setSupplierCode(dto.getSupplierCode());
        state.setFileProcessedAt(message.fileProcessedAt());
        state.setBatchId(message.batchId());
        return state;
    }

    private PriceListDelta calculateDelta(
            Map<String, PriceListCurrentState> currentMap,
            Map<String, PriceListCurrentState> newMap) {
        List<PriceListCurrentState> added = new ArrayList<>();
        List<PriceListCurrentState> updated = new ArrayList<>();
        List<String> deleted = new ArrayList<>();
        for (var entry : newMap.entrySet()) {
            String productId = entry.getKey();
            PriceListCurrentState newState = entry.getValue();
            if (currentMap.containsKey(productId)) {
                if (!isSameState(currentMap.get(productId), newState)) {
                    updated.add(newState);
                }
            } else {
                added.add(newState);
            }
        }
        for (var entry : currentMap.entrySet()) {
            if (!newMap.containsKey(entry.getKey())) {
                deleted.add(entry.getKey());
            }
        }
        return new PriceListDelta(added, updated, deleted);
    }

    private boolean isSameState(PriceListCurrentState a, PriceListCurrentState b) {
        return Objects.equals(a.getPrice(), b.getPrice()) &&
               Objects.equals(a.getStockQuantity(), b.getStockQuantity()) &&
               Objects.equals(a.getProductName(), b.getProductName()) &&
               Objects.equals(a.getDescription(), b.getDescription()) &&
               Objects.equals(a.getCategory(), b.getCategory()) &&
               Objects.equals(a.getManufacturer(), b.getManufacturer()) &&
               Objects.equals(a.getSupplierCode(), b.getSupplierCode()) &&
               Objects.equals(a.getCurrency(), b.getCurrency());
    }
}
