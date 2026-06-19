package ru.itis.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.api.CompilationApi;
import ru.itis.dto.compilation.AddCompilationRequest;
import ru.itis.dto.compilation.response.CompilationResponse;
import ru.itis.services.compilationService.CompilationService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CompilationController implements CompilationApi {

    private final CompilationService compilationService;

    @Override
    public Slice<CompilationResponse> getCompilationsFeed(Jwt jwt, int page, int size) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return compilationService.findCompilationsFeed(userId, page, size);
    }

    @Override
    public ResponseEntity<CompilationResponse> addCompilation(Jwt jwt, AddCompilationRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        CompilationResponse response = compilationService.addCompilation(
                userId,
                request.getGroupId(),
                request.getTitle(),
                request.getDescription(),
                request.getCategoryId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
