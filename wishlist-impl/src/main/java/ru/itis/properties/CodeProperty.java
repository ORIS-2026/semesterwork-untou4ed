package ru.itis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "phone-verification-code")
@Data
public class CodeProperty {
    private int singleSmsCooldown;
    private int maxSmsInTotalTime;
    private int totalTime;
    private int expiration;
    private int enterSingleCodeAttempts;
}
