package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.controller.filter.impl.UserFilter;
import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.UserRequest;
import com.example.couriermanagement.dto.request.UserUpdateRequest;
import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.repository.DeliveryRepository;
import com.example.couriermanagement.repository.UserRepository;
import com.example.couriermanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DeliveryRepository deliveryRepository;

    @Override
    public List<UserDto> getAllUsers(UserFilter userFilter) {
        return userRepository.findAll(userFilter.filter())
                .stream()
                .map(user -> UserDto.builder()
                        .id(user.getId())
                        .login(user.getLogin())
                        .name(user.getName())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt())
                        .build())
                .toList();
    }

    @Override
    public UserDto createUser(UserRequest userRequest) {

        if (userRepository.existsByLogin(userRequest.getLogin())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        User user = User.builder()
                .login(userRequest.getLogin())
                .passwordHash(passwordEncoder.encode(userRequest.getPassword()))
                .name(userRequest.getName())
                .role(userRequest.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        return UserDto.from(userRepository.save(user));
    }


    @Override
    public UserDto updateUser(Long id, UserUpdateRequest userUpdateRequest) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (userUpdateRequest.getLogin() != null && !userUpdateRequest.getLogin().equals(user.getLogin())
                && userRepository.existsByLogin(userUpdateRequest.getLogin())) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        User.UserBuilder builder = user.toBuilder()
                .login(userUpdateRequest.getLogin())
                .name(userUpdateRequest.getName())
                .role(userUpdateRequest.getRole())
                .passwordHash(passwordEncoder.encode(userUpdateRequest.getPassword()));

        return UserDto.from(userRepository.save(builder.build()));
    }


    @Override
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        userRepository.delete(user);
    }
}