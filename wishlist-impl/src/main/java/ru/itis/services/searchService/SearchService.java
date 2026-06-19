package ru.itis.services.searchService;

import ru.itis.dto.search.SearchResponse;

public interface SearchService {

    SearchResponse search(String query);
}
