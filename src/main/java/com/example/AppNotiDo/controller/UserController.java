package com.example.AppNotiDo.controller;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.UserDTO;
import com.example.AppNotiDo.dto.UserProfileUpdateRequest;
import com.example.AppNotiDo.mapper.UserMapper;
import com.example.AppNotiDo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    // 🔥 NOUVEAU : Mettre à jour UNIQUEMENT le displayName
    @Operation(summary = "Mettre à jour le nom d'affichage de l'utilisateur connecté")
    @PatchMapping("/profile")
    public UserDTO updateCurrentUserDisplayName(
            @Valid @RequestBody UserProfileUpdateRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        user.setDisplayName(request.getDisplayName());
        User updatedUser = userService.saveUser(user);  // Ajoute saveUser dans UserService si besoin
        return UserMapper.toDTO(updatedUser);
    }
}