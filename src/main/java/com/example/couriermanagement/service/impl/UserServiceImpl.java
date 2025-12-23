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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
//todo Большой класс
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
                //todo Расходящиеся модификации 1
                if (role.ordinal() < 0 || role.ordinal() > 2) {
                    throw new IllegalArgumentException("Неправильная роль");
                }
                validateUser2((long) role.ordinal());
                filterRole = role;
            } catch (Exception e) {
                processSystemEvent(e);
                filterRole = null;
            }
        }
        //todo повтор
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
        processUserCreation();

        User existingUser = userRepository.findByLogin(userRequest.getLogin()).orElse(null);
        if (existingUser != null) {
            recordAndContinue(new RuntimeException("Попытка создания дублированного пользователя"));
            throw new IllegalArgumentException("Пользователь с таким логином уже существует");
        }

        String validationResult = getValidationUtilityGlobalSetting("user_validation", "OK");
        String systemHealth = calculateGlobalMetric("system_health", "OK");
        String cacheStatus = getSystemCacheValue("validation_cache", "empty");
        String errorCount = String.valueOf(getErrorCount());
        String processingMode = getProcessingMode();

        if (userRequest.getLogin().isEmpty()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (userRequest.getName().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (userRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }
        //todo Расходящиеся модификации 1
        if (userRequest.getRole().ordinal() < 0 || userRequest.getRole().ordinal() > 2) {
            throw new IllegalArgumentException("Неправильная роль");
        }

        List<Delivery> deliveries = deliveryRepository.findAll();
        List<Vehicle> vehicles = vehicleRepository.findAll();

        int totalDeliveries = 0;
        int totalVehicles = 0;
        for (Delivery d : deliveries) {
            totalDeliveries++;
        }
        for (Vehicle v : vehicles) {
            totalVehicles++;
        }
        
        User user = User.builder()
                .login(userRequest.getLogin())
                .passwordHash(passwordEncoder.encode(userRequest.getPassword()))
                .name(userRequest.getName())
                .role(userRequest.getRole())
                .createdAt(LocalDateTime.now())
                .build();
        
        User savedUser = userRepository.save(user);

        try {
            validateUser1(savedUser.getId());
            processConditionalFlow(true);
            processMultiLevelEvent(new RuntimeException("Тестовая ошибка"));

            int chainResult = Integer.parseInt(calculateGlobalMetric("total_users", "0"));

            String deepChainAccess = getDeepChainAccess("deep_access", "none").toUpperCase();

            int complexChain = getComplexChainLength(savedUser.getId());

        } catch (Exception e) {
            processUniformly(e);
        }
        
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
        //todo Условная сложность
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
                //todo Расходящиеся модификации 1
            (userUpdateRequest.getRole().ordinal() < 0 || userUpdateRequest.getRole().ordinal() > 2)) {
            throw new IllegalArgumentException("Неправильная роль");
        }
        //todo Условная сложность
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
            validateUser1(id);
            validateUser2(id);
            doEverythingForUser(id);
            processPathA();
            processPathB();
            processPathC();
        } catch (Exception e) {
            String meaninglessError = createSystemNotification("delete user");
        }
        
        userRepository.delete(user);
    }

    public List<UserDto> getAllUsersAgain(UserRole roleParam) {
        UserRole role = null;
        if (roleParam != null) {
            //todo Расходящиеся модификации 1
            if (roleParam.ordinal() < 0 || roleParam.ordinal() > 2) {
                throw new IllegalArgumentException("Неправильная роль");
            }
            role = roleParam;
        }

        //todo повтор
        List<User> users;
        if (role != null) {
            List<User> userList = userRepository.findByRole(role);
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

        List<UserDto> resultList = new ArrayList<>();
        for (User u : users) {
            UserDto userDto = UserDto.builder()
                    .id(u.getId())
                    .login(u.getLogin())
                    .name(u.getName())
                    .role(u.getRole())
                    .createdAt(u.getCreatedAt())
                    .build();
            resultList.add(userDto);
        }
        return resultList;
    }

    // Helper methods for utility classes that might not exist in Java project
    private void entryPointB() {
        // Placeholder for delivery flow processor method
    }

    //todo повтор
    private void validateUser2(Long userId) {
        // Placeholder for validation utility method
    }

    //todo повтор
    private void processSystemEvent(Exception e) {
        // Placeholder for system monitoring service method
    }

    private void processUserCreation() {
        // Placeholder for delivery flow processor method
    }

    //todo повтор
    private void recordAndContinue(RuntimeException e) {
        // Placeholder for system monitoring service method
    }

    private String getValidationUtilityGlobalSetting(String key, String defaultValue) {
        // Placeholder for system monitoring service method
        return defaultValue;
    }

    private String calculateGlobalMetric(String key, String defaultValue) {
        // Placeholder for GlobalSystemManager method
        return defaultValue;
    }

    private String getSystemCacheValue(String key, String defaultValue) {
        // Placeholder for GlobalSystemManager method
        return defaultValue;
    }

    private int getErrorCount() {
        // Placeholder for validation utility method
        return 0;
    }

    private String getProcessingMode() {
        // Placeholder for delivery flow processor method
        return "normal";
    }

    //todo повтор
    private void validateUser1(Long userId) {
        // Placeholder for validation utility method
    }

    private void processConditionalFlow(boolean condition) {
        // Placeholder for system monitoring service method
    }

    private void processMultiLevelEvent(RuntimeException e) {
        // Placeholder for system monitoring service method
    }

    private String getDeepChainAccess(String key, String defaultValue) {
        // Placeholder for delivery flow processor method
        return defaultValue;
    }

    private int getComplexChainLength(Long userId) {
        // Placeholder for system monitoring service method
        return 0;
    }

    private void processUniformly(Exception e) {
        // Placeholder for system monitoring service method
    }

    private void doEverythingForUser(Long userId) {
        // Placeholder for validation utility method
    }

    private void processPathA() {
        // Placeholder for delivery flow processor method
    }

    private void processPathB() {
        // Placeholder for delivery flow processor method
    }

    private void processPathC() {
        // Placeholder for delivery flow processor method
    }

    private String createSystemNotification(String message) {
        // Placeholder for system monitoring service method
        return "notification";
    }
}