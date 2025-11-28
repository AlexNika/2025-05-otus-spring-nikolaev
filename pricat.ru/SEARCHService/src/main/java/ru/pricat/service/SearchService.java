package ru.pricat.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.pricat.model.PriceItemDocument;

import java.util.List;

public interface SearchService {

    Page<PriceItemDocument> search(String query, String company, Pageable pageable);

    List<String> getAllCompanies();
}