package ru.itis.dto.telegram.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestStatus {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("request_cost")
    private Double requestCost;

    @JsonProperty("is_refunded")
    private Boolean isRefunded;

    @JsonProperty("remaining_balance")
    private Double remainingBalance;

    @JsonProperty("delivery_status")
    private DeliveryStatus deliveryStatus;

    @JsonProperty("verification_status")
    private VerificationStatus verificationStatus;
}
