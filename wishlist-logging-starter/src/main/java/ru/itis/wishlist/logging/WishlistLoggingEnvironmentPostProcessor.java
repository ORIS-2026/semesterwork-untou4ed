package ru.itis.wishlist.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class WishlistLoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String CONSOLE_PATTERN =
            "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} %clr(%-5level)" +
            " %clr(---){faint} %clr(${spring.application.name:-app}){magenta}" +
            "  %clr(%logger){cyan} %clr(:){faint} %msg%n";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.containsProperty("logging.pattern.console")) {
            return;
        }
        environment.getPropertySources().addLast(
                new MapPropertySource("wishlistLoggingDefaults",
                        Map.of("logging.pattern.console", CONSOLE_PATTERN))
        );
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
