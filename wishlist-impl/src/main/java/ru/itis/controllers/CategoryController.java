package ru.itis.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.api.CategoryApi;
import ru.itis.dto.category.CategoryResponse;
import ru.itis.repositories.CategoryRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }
}
