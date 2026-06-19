package ru.itis.dto.telegram.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VerificationStatus {

    /** code_valid | code_invalid | code_max_attempts_exceeded | expired | revoked */
    private String status;

    @JsonProperty("updated_at")
    private Long updatedAt;

    @JsonProperty("code_entered")
    private String codeEntered;
}
