package ru.itis.services.compilationService;

import org.springframework.data.domain.Slice;
import ru.itis.dto.compilation.response.CompilationResponse;

import java.util.UUID;

public interface CompilationService {

    Slice<CompilationResponse> findCompilationsFeed(UUID userId, int page, int size);

    CompilationResponse addCompilation(UUID requesterId, UUID groupId, String title, String description, int categoryId);
}
