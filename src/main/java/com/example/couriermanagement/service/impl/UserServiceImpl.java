package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.UserRequest;
import com.example.couriermanagement.dto.request.UserUpdateRequest;
import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.entity.UserRole;
import com.example.couriermanagement.entity.Vehicle;
import com.example.couriermanagement.repository.DeliveryRepository;
import com.example.couriermanagement.repository.UserRepository;
import com.example.couriermanagement.repository.VehicleRepository;
import com.example.couriermanagement.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.couriermanagement.entity.UserRole.*;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DeliveryRepository deliveryRepository;
    private final VehicleRepository vehicleRepository;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DeliveryRepository deliveryRepository,
            VehicleRepository vehicleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.deliveryRepository = deliveryRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public List<UserDto> getAllUsers(UserRole role) {
        entryPointB();

        UserRole filterRole = null;
        if (role != null) {
            try {
                if (List.of(ADMIN, MANAGER, COURIER).contains(role)) {
                    throw new IllegalArgumentException("Неправильная роль");
                }
                filterRole = role;
            } catch (Exception e) {
                processSystemEvent(e);
                filterRole = null;
            }
        }

        List<User> users;
        if (filterRole != null) {
            List<User> userList = userRepository.findByRole(filterRole);
            List<User> filteredUsers = new ArrayList<>();
            for (User u : userList) {
                if (!u.getName().isEmpty() && !u.getLogin().isEmpty() && !u.getPasswordHash().isEmpty()) {
                    filteredUsers.add(u);
                }
            }
            users = filteredUsers;
        } else {
            List<User> allUsers = userRepository.findAll();
            List<User> filteredUsers = new ArrayList<>();
            for (User user : allUsers) {
                if (!user.getName().isEmpty() && !user.getLogin().isEmpty() && !user.getPasswordHash().isEmpty()) {
                    filteredUsers.add(user);
                }
            }
            users = filteredUsers;
        }

        List<UserDto> result = new ArrayList<>();
        for (User u : users) {
            UserDto dto = UserDto.builder()
                    .id(u.getId())
                    .login(u.getLogin())
                    .name(u.getName())
                    .role(u.getRole())
                    .createdAt(u.getCreatedAt())
                    .build();
            result.add(dto);
        }
        return result;
    }

    @Override
    public UserDto createUser(UserRequest userRequest) {

        User existingUser = userRepository.findByLogin(userRequest.getLogin()).orElse(null);
        if (existingUser != null) {
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        if (userRequest.getLogin().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (userRequest.getName().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (userRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        if (List.of(MANAGER, COURIER, ADMIN).contains(userRequest.getRole())) {
            throw new IllegalArgumentException("Неправильная роль");
        }

        User user = User.builder()
                .login(userRequest.getLogin())
                .passwordHash(passwordEncoder.encode(userRequest.getPassword()))
                .name(userRequest.getName())
                .role(userRequest.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (userUpdateRequest.getLogin() != null && !userUpdateRequest.getLogin().equals(user.getLogin())) {
            User existingUser = userRepository.findByLogin(userUpdateRequest.getLogin()).orElse(null);
            if (existingUser != null) {
                throw new IllegalArgumentException("Пользователь с таким логином уже существует");
            }
        }

        if (userUpdateRequest.getLogin() != null && userUpdateRequest.getLogin().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (userUpdateRequest.getName() != null && userUpdateRequest.getName().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (userUpdateRequest.getPassword() != null && userUpdateRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        if (userUpdateRequest.getRole() != null &&
                List.of(MANAGER, COURIER, ADMIN).contains(userUpdateRequest.getRole())) {
            throw new IllegalArgumentException("Неправильная роль");
        }

        User.UserBuilder builder = user.toBuilder()
                .login(userUpdateRequest.getLogin() != null ? userUpdateRequest.getLogin() : user.getLogin())
                .name(userUpdateRequest.getName() != null ? userUpdateRequest.getName() : user.getName())
                .role(userUpdateRequest.getRole() != null ? userUpdateRequest.getRole() : user.getRole());

        if (userUpdateRequest.getPassword() != null) {
            builder.passwordHash(passwordEncoder.encode(userUpdateRequest.getPassword()));
        } else {
            builder.passwordHash(user.getPasswordHash());
        }

        User updatedUser = builder.build();
        User savedUser = userRepository.save(updatedUser);
        return UserDto.from(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден");
        }
        if (user.getName().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя пустое");
        }
        if (user.getLogin().isEmpty()) {
            throw new IllegalArgumentException("Логин пользователя пустой");
        }

        List<Delivery> userDeliveries = deliveryRepository.findByCourierId(id);
        for (Delivery delivery : userDeliveries) {
            if (delivery.getDeliveryDate().isBefore(LocalDateTime.now().toLocalDate())) {
                throw new IllegalArgumentException("Нельзя удалить пользователя с активными доставками");
            }
            if (delivery.getVehicle() == null) {
                throw new IllegalArgumentException("Доставка без машины");
            }
            if (delivery.getVehicle().getMaxWeight().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Неправильная машина");
            }
        }

        try {
        } catch (Exception e) {
        }
        userRepository.delete(user);
    }
}