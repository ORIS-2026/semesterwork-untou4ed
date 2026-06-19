package ru.itis.wishlist.logging;

import ru.itis.wishlist.logging.aspect.WishlistLoggingAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@AutoConfiguration
@EnableConfigurationProperties(WishlistLoggingProperties.class)
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "wishlist.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WishlistLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WishlistLoggingAspect wishlistLoggingAspect(WishlistLoggingProperties properties) {
        return new WishlistLoggingAspect(properties);
    }
}
