package com.example.couriermanagement.service;

import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.UserRequest;
import com.example.couriermanagement.dto.request.UserUpdateRequest;
import com.example.couriermanagement.entity.UserRole;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers(UserRole role);

    UserDto createUser(UserRequest userRequest);

    UserDto updateUser(Long id, UserUpdateRequest userUpdateRequest);

    void deleteUser(Long id);
}