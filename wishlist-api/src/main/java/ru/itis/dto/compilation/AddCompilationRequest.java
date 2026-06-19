package ru.itis.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddCompilationRequest {
    private UUID groupId;
    private String title;
    private String description;
    private int categoryId;
}
