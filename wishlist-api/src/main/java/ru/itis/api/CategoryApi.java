package ru.itis.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.dto.category.CategoryResponse;

import java.util.List;

@RequestMapping("/api/v1/categories")
@RestController
public interface CategoryApi {

    @GetMapping
    List<CategoryResponse> getAll();
}
