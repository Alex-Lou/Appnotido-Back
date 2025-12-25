package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.exception.UserNotFoundException;
import com.example.AppNotiDo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user){
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if(userRepository.existsByEmail(user.getEmail())){
            throw new IllegalArgumentException("This email already exists");
        }

        // Valider et initialiser les champs obligatoires
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        // Initialiser les valeurs par défaut si nécessaire
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        if (user.getTheme() == null || user.getTheme().isEmpty()) {
            user.setTheme("light");
        }

        if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
            user.setDisplayName(user.getUsername());
        }

        // Encoder le password UNE SEULE FOIS
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("This user ID does not exist"));
    }

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public void deleteUser(Long id){
        getUserById(id);
        userRepository.deleteById(id);
    }

    public User updateUser(Long id, User updateUser){
        User existingUser = getUserById(id);
        existingUser.setUsername(updateUser.getUsername());
        existingUser.setEmail(updateUser.getEmail());

        if(updateUser.getTheme() != null) {
            existingUser.setTheme(updateUser.getTheme());
        }

        if(updateUser.getDisplayName() != null) {
            existingUser.setDisplayName(updateUser.getDisplayName());
        }

        return userRepository.save(existingUser);
    }

    public User saveUser(User user) {
        // Vérifier si l'email est déjà utilisé par un autre user
        userRepository.findByEmail(user.getEmail())
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(user.getId())) {
                        throw new IllegalArgumentException("Cet email est déjà utilisé");
                    }
                });
        return userRepository.save(user);
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = getUserByUsername(username);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return getUserByUsername(username);
    }

}