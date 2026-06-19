package ru.itis.wishlist.logging;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "wishlist.logging")
public class WishlistLoggingProperties {
    private boolean logArgs = true;
    private boolean logReturn = true;
    private long slowThresholdMs = 2000;
}
