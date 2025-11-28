package ru.pricat.service.strategy;

import org.springframework.web.multipart.MultipartFile;

public interface PricelistObtainingStrategy {

    void uploadPricelist(String username, MultipartFile file, String companyFolder);
}
