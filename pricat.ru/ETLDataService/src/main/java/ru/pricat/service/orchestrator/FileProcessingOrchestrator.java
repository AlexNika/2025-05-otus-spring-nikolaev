package ru.pricat.service.orchestrator;

import org.springframework.transaction.annotation.Transactional;
import ru.pricat.model.dto.history.FileProcessingResult;

public interface FileProcessingOrchestrator {
    @Transactional
    FileProcessingResult processFile(String company, String fileKey);
}
