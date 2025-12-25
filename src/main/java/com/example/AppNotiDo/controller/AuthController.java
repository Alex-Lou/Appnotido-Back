package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.LoginRequest;
import com.example.AppNotiDo.service.JwtService;
import com.example.AppNotiDo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Tag(name = "Authentification", description = "API d'authentification")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Operation(summary = "S'inscrire (créer un compte)")
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        // Initialiser les champs obligatoires s'ils sont null
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }
        if (user.getTheme() == null || user.getTheme().isEmpty()) {
            user.setTheme("light");
        }
        if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
            user.setDisplayName(user.getUsername());
        }

        // Le password sera encodé dans UserService.createUser()
        userService.createUser(user);

        // Connexion automatique après inscription
        String token = jwtService.generateToken(user.getUsername());

        // ✅ Créer le cookie HttpOnly avec SameSite=Lax
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false)  // false pour HTTP local, true en production HTTPS
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")  // ← CRITIQUE pour cross-site
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("username", user.getUsername(), "message", "User registered successfully"));
    }

    @Operation(summary = "Se connecter et obtenir un cookie JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.getUserByUsername(loginRequest.getUsername());

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = jwtService.generateToken(user.getUsername());

            // ✅ Créer le cookie HttpOnly avec SameSite=Lax
            ResponseCookie cookie = ResponseCookie.from("token", token)
                    .httpOnly(true)
                    .secure(false)  // false pour HTTP local, true en production HTTPS
                    .path("/")
                    .maxAge(Duration.ofDays(7))
                    .sameSite("Lax")  // ← CRITIQUE pour cross-site
                    .build();

            // Retourner seulement le username (pas le token)
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(Map.of("username", user.getUsername()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Se déconnecter (supprimer le cookie)")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // ✅ Supprimer le cookie en mettant maxAge=0
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @Operation(summary = "Mettre à jour le thème de l'utilisateur")
    @PutMapping("/theme")
    public ResponseEntity<?> updateTheme(@RequestBody Map<String, String> request, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        user.setTheme(request.get("theme"));
        userService.updateUser(user.getId(), user);

        return ResponseEntity.ok(Map.of("message", "Theme updated", "theme", user.getTheme()));
    }

    @Operation(summary = "Récupérer le thème de l'utilisateur")
    @GetMapping("/theme")
    public ResponseEntity<?> getTheme(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
        }

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        return ResponseEntity.ok(Map.of("theme", user.getTheme()));
    }
}
