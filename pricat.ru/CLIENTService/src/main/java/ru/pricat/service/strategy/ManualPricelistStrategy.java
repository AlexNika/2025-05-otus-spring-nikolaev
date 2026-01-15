package ru.pricat.service.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.pricat.service.FileService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManualPricelistStrategy implements PricelistObtainingStrategy {

    private final FileService fileService;

    @Override
    public void uploadPricelist(String username, MultipartFile file, String companyFolder) {
        log.info("Manual upload for client: {}", username);
        fileService.uploadFile(username, file, companyFolder);
    }
}
