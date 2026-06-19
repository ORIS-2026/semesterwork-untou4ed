package ru.itis.api;

import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.itis.dto.compilation.AddCompilationRequest;
import ru.itis.dto.compilation.response.CompilationResponse;

@RequestMapping("/api/v1/compilations")
@RestController
public interface CompilationApi {

    @GetMapping("/feed")
    Slice<CompilationResponse> getCompilationsFeed(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<CompilationResponse> addCompilation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AddCompilationRequest request
    );
}
