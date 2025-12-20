package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.UserDTO;
import com.example.AppNotiDo.dto.UserProfileUpdateRequest;
import com.example.AppNotiDo.dto.ChangePasswordRequest;
import com.example.AppNotiDo.mapper.UserMapper;
import com.example.AppNotiDo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Users", description = "API de gestion des utilisateurs")
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Récupérer tous les utilisateurs avec pagination")
    @GetMapping
    public Page<UserDTO> getAllUsers(
            @Parameter(description = "Numéro de la page")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page")
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<User> userPage = userService.getAllUsers(page, size);
        return userPage.map(UserMapper::toDTO);
    }

    @Operation(summary = "Récupérer un utilisateur par son ID")
    @GetMapping("/{id}")
    public UserDTO getUserById(
            @Parameter(description = "ID de l'utilisateur")
            @PathVariable Long id
    ) {
        User user = userService.getUserById(id);
        return UserMapper.toDTO(user);
    }

    @Operation(summary = "Récupérer un utilisateur par son username")
    @GetMapping("/username/{username}")
    public UserDTO getUserByUsername(
            @Parameter(description = "Username de l'utilisateur")
            @PathVariable String username
    ) {
        User user = userService.getUserByUsername(username);
        return UserMapper.toDTO(user);
    }

    @Operation(summary = "Mettre à jour un utilisateur")
    @PutMapping("/{id}")
    public UserDTO updateUser(
            @Parameter(description = "ID de l'utilisateur à modifier")
            @PathVariable Long id,
            @Valid @RequestBody User user
    ) {
        User updatedUser = userService.updateUser(id, user);
        return UserMapper.toDTO(updatedUser);
    }

    @Operation(summary = "Supprimer un utilisateur")
    @DeleteMapping("/{id}")
    public void deleteUser(
            @Parameter(description = "ID de l'utilisateur à supprimer")
            @PathVariable Long id
    ) {
        userService.deleteUser(id);
    }

    @Operation(summary = "Récupérer le profil de l'utilisateur connecté")
    @GetMapping("/profile")
    public UserDTO getCurrentUserProfile(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        return UserMapper.toDTO(user);
    }

    @Operation(summary = "Mettre à jour le profil de l'utilisateur connecté")
    @PatchMapping("/profile")
    public UserDTO updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        User updatedUser = userService.saveUser(user);
        return UserMapper.toDTO(updatedUser);
    }

    @Operation(summary = "Changer le mot de passe de l'utilisateur connecté")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        try {
            String username = authentication.getName();
            userService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Supprimer le compte de l'utilisateur connecté")
    @DeleteMapping("/me")
    public void deleteCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        userService.deleteUser(user.getId());
    }
}
