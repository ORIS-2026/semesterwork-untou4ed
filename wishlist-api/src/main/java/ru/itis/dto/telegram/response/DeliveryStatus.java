package ru.itis.dto.telegram.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeliveryStatus {

    /** sent | delivered | read | expired | revoked */
    private String status;

    @JsonProperty("updated_at")
    private Long updatedAt;
}
