package ru.itis.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.dto.search.SearchResponse;

@RequestMapping("/api/v1/search")
@RestController
public interface SearchApi {

    @GetMapping
    SearchResponse search(@RequestParam String q);
}
