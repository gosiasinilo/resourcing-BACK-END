package io.nology.resources;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @GetMapping("/auth/me")
    public Map<String, String> me(Authentication authentication) {
        String username = authentication.getName();
        return Map.of(
                "username", username,
                "roles", authentication.getAuthorities().toString());
    }
}