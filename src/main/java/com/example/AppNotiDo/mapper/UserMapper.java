package com.example.AppNotiDo.mapper;

import com.example.AppNotiDo.domain.User;
import com.example.AppNotiDo.dto.UserDTO;

public class UserMapper {

    public static UserDTO toDTO(User user){
        UserDTO userDTO = new UserDTO();

        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setTheme(user.getTheme());  // ← AJOUT MANQUANT

        userDTO.setDisplayName(user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()
                ? user.getDisplayName()
                : user.getUsername());

        if (user.getTasks() != null) {
            userDTO.setTaskCount(user.getTasks().size());
        } else {
            userDTO.setTaskCount(0);
        }

        return userDTO;
    }
}
