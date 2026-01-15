package ru.pricat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.pricat.model.PriceItemDocument;
import ru.pricat.service.SearchService;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public String searchForm(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "") String company,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Pageable pageable = PageRequest.of(page, 20);
        return getString(query, company, model, pageable);
    }

    private String getString(@RequestParam(defaultValue = "") String query,
                             @RequestParam(defaultValue = "") String company,
                             Model model,
                             Pageable pageable) {
        Page<PriceItemDocument> results = searchService.search(query, company, pageable);

        model.addAttribute("query", query);
        model.addAttribute("company", company);
        model.addAttribute("results", results);
        model.addAttribute("companies", searchService.getAllCompanies());
        return "search";
    }
}
