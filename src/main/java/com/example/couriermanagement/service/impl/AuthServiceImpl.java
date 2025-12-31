package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.LoginRequest;
import com.example.couriermanagement.dto.response.LoginResponse;
import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.repository.UserRepository;
import com.example.couriermanagement.security.JwtUtil;
import com.example.couriermanagement.service.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByLogin(loginRequest.getLogin());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid login or password");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid login or password");
        }

        String token = jwtUtil.generateToken(user.getLogin(), user.getRole().name());

        return new LoginResponse(token, UserDto.from(user));
    }

    @Override
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByLogin(username);
        if (userOptional.isEmpty()) {
            return null;
        }

        return UserDto.from(userOptional.get());
    }
}