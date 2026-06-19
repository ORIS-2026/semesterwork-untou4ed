package ru.itis.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.itis.properties.KeycloakProperties;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final KeycloakProperties keycloak;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("keycloakUrl", keycloak.getUrl());
        model.addAttribute("keycloakRealm", keycloak.getRealm());
        model.addAttribute("keycloakClientId", keycloak.getFrontendClientId());
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/me")
    public String mePage() {
        return "me";
    }

    @GetMapping("/feed")
    public String feedPage() {
        return "feed";
    }

    @GetMapping("/groups")
    public String groupsPage() {
        return "groups";
    }

    @GetMapping("/subscriptions")
    public String subscriptionsPage() {
        return "subscriptions";
    }

    @GetMapping("/users/{userId}")
    public String userPage() {
        return "user";
    }

    @GetMapping("/oauth/success")
    public String oauthSuccessPage(Model model) {
        model.addAttribute("keycloakUrl",      keycloak.getUrl());
        model.addAttribute("keycloakRealm",    keycloak.getRealm());
        model.addAttribute("keycloakClientId", keycloak.getFrontendClientId());
        return "oauth-success";
    }
}
