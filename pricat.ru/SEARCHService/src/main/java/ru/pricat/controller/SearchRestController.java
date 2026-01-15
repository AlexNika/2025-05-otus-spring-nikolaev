package ru.pricat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pricat.model.PriceItemDocument;
import ru.pricat.service.SearchService;

import java.util.List;

import static ru.pricat.util.AppConstants.API_V1_SEARCH_PATH;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_V1_SEARCH_PATH)
public class SearchRestController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String company,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String searchQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();

        Pageable pageable = PageRequest.of(page, size);
        Page<PriceItemDocument> results = searchService.search(searchQuery, company, pageable);

        SearchResponse response = new SearchResponse(
            results.getContent(),
            results.getNumber(),
            results.getTotalPages(),
            results.getTotalElements(),
            searchService.getAllCompanies()
        );

        return ResponseEntity.ok(response);
    }

    public record SearchResponse(
        List<PriceItemDocument> results,
        int currentPage,
        int totalPages,
        long totalElements,
        List<String> companies
    ) {}
}
