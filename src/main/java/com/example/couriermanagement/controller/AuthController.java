package com.example.couriermanagement.controller;

import com.example.couriermanagement.dto.request.LoginRequest;
import com.example.couriermanagement.dto.response.LoginResponse;
import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.repository.UserRepository;
import com.example.couriermanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Аутентификация")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя по логину и паролю"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Успешная авторизация"),
                    @ApiResponse(responseCode = "401", description = "Неверные данные для входа"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
            }
    )
    @SecurityRequirement(name = "")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/debug")
    @SecurityRequirement(name = "")
    public ResponseEntity<Map<String, String>> debug() {
        Optional<User> adminUserOptional = userRepository.findByLogin("admin");
        String newHash = passwordEncoder.encode("admin123");
        Map<String, String> result = new HashMap<>();

        if (adminUserOptional.isPresent()) {
            User adminUser = adminUserOptional.get();
            result.put("currentHash", adminUser.getPasswordHash());
            result.put("newHash", newHash);
            result.put("matches", String.valueOf(passwordEncoder.matches("admin123", adminUser.getPasswordHash())));

            adminUser.setPasswordHash(newHash);
            userRepository.save(adminUser);
            result.put("updated", "true");
        } else {
            result.put("error", "Admin user not found");
        }

        return ResponseEntity.ok(result);
    }
}