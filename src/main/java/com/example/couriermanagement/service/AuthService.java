package com.example.couriermanagement.service;

import com.example.couriermanagement.dto.request.LoginRequest;
import com.example.couriermanagement.dto.response.LoginResponse;
import com.example.couriermanagement.entity.User;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);

    User getCurrentUser();
}