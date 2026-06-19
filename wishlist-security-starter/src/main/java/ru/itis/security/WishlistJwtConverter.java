package ru.itis.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public class WishlistJwtConverter {

    private WishlistJwtConverter() {}

    public static JwtAuthenticationConverter build() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("preferred_username");

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();

            log.debug(
                    "Converting JWT: subject={}, username={}, issuer={}, expiresAt={}",
                    jwt.getSubject(),
                    jwt.getClaimAsString("preferred_username"),
                    jwt.getIssuer() != null ? jwt.getIssuer().toString() : null,
                    jwt.getExpiresAt()
            );

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles) {
                for (Object role : roles) {
                    authorities.add(new SimpleGrantedAuthority(role.toString()));
                }
            }
            return authorities;
        });

        return converter;
    }
}
