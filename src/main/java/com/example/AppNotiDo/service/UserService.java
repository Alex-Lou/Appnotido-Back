package com.example.AppNotiDo.service;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.exception.UserNotFoundException;
import com.example.AppNotiDo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User createUser(User user){
        if(userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if(userRepository.existsByEmail(user.getEmail())){
            throw new IllegalArgumentException("This email already exists");
        }

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

        return userRepository.save(existingUser);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}