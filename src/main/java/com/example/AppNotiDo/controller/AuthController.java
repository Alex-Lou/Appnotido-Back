package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.LoginRequest;
import com.example.AppNotiDo.service.JwtService;
import com.example.AppNotiDo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> register(@Valid @RequestBody User user, HttpServletResponse response) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        userService.createUser(user);

        // Connexion automatique après inscription
        String token = jwtService.generateToken(user.getUsername());

        // Créer le cookie HttpOnly
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);           // ✅ JavaScript ne peut pas lire
        cookie.setSecure(false);            // ⚠️ false pour localhost, true en production HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 3600);    // 7 jours
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("username", user.getUsername(), "message", "User registered successfully"));
    }

    @Operation(summary = "Se connecter et obtenir un cookie JWT")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            User user = userService.getUserByUsername(loginRequest.getUsername());

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = jwtService.generateToken(user.getUsername());

            // Créer le cookie HttpOnly
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);           // ✅ JavaScript ne peut pas lire
            cookie.setSecure(false);            // ⚠️ false pour localhost, true en production HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 3600);    // 7 jours
            response.addCookie(cookie);

            // Retourner seulement le username (pas le token)
            return ResponseEntity.ok(Map.of("username", user.getUsername()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "Se déconnecter (supprimer le cookie)")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Supprimer le cookie
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // ✅ Expire immédiatement
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
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