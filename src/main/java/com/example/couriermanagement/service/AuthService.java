package com.example.couriermanagement.service;

import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.LoginRequest;
import com.example.couriermanagement.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    UserDto getCurrentUser();
}