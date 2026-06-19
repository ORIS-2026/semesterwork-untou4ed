package ru.itis.dto.telegram.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendVerificationRequest {

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String code;

    private Integer ttl;

    @JsonProperty("sender_username")
    private String senderUsername;

    @JsonProperty("callback_url")
    private String callbackUrl;
}
