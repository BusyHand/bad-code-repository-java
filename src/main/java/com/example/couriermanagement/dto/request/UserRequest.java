package com.example.couriermanagement.dto.request;

import com.example.couriermanagement.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для создания пользователя")
public class UserRequest {

    @NotBlank(message = "Логин обязателен")
    @Schema(description = "Логин пользователя", example = "courier1")
    private String login;

    @NotBlank(message = "Пароль обязателен")
    @Schema(description = "Пароль пользователя", example = "password123")
    private String password;

    @NotBlank(message = "Имя обязательно")
    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    private String name;

    @NotNull(message = "Роль обязательна")
    @Schema(description = "Роль пользователя")
    private UserRole role;
}
