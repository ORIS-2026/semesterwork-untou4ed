package ru.itis.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.api.SearchApi;
import ru.itis.dto.search.SearchResponse;
import ru.itis.services.searchService.SearchService;

@RestController
@RequiredArgsConstructor
public class SearchController implements SearchApi {

    private final SearchService searchService;

    @Override
    public SearchResponse search(String q) {
        return searchService.search(q);
    }
}
