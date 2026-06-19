package ru.itis.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@PropertySource("classpath:wishlist-security-defaults.properties")
@EnableConfigurationProperties(WishlistSecurityProperties.class)
@ConditionalOnClass(SecurityFilterChain.class)
@EnableMethodSecurity
@Slf4j
public class WishlistSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationConverter wishlistJwtAuthenticationConverter() {
        return WishlistJwtConverter.build();
    }

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http,
            WishlistSecurityProperties properties,
            JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

        String[] permitAll  = properties.getPermitAll().toArray(new String[0]);
        String[] requireAuth = properties.getRequireAuth().toArray(new String[0]);

        http
                .csrf(CsrfConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permitAll).permitAll()
                        .requestMatchers(requireAuth).authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String uri = request.getRequestURI();
                            log.warn(
                                    "Unauthorized request: method={}, uri={}, remoteAddr={}, error={}",
                                    request.getMethod(),
                                    uri,
                                    request.getRemoteAddr(),
                                    authException.getMessage()
                            );
                            if (uri.startsWith("/api/")) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            } else {
                                // если sendError то Spring Boot рендерит error.html
                                response.sendError(HttpStatus.UNAUTHORIZED.value());
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String uri = request.getRequestURI();
                            log.warn(
                                    "Access denied: method={}, uri={}, remoteAddr={}, error={}",
                                    request.getMethod(),
                                    uri,
                                    request.getRemoteAddr(),
                                    accessDeniedException.getMessage()
                            );
                            if (uri.startsWith("/api/")) {
                                response.setStatus(HttpStatus.FORBIDDEN.value());
                            } else {
                                response.sendError(HttpStatus.FORBIDDEN.value());
                            }
                        })
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }
}
