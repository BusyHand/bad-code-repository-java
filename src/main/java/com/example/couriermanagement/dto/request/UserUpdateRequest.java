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
@Schema(description = "Данные для обновления пользователя")
public class UserUpdateRequest {

    @NotBlank(message = "Имя не может быть пустым")
    @Schema(description = "Имя пользователя", example = "Иван Иванов")
    private String name;

    @NotBlank(message = "Логин не может быть пустым")
    @Schema(description = "Логин пользователя", example = "courier1")
    private String login;

    @Schema(description = "Роль пользователя")
    @NotNull(message = "Роль обязательна")
    private UserRole role;

    @NotBlank(message = "Пароль не может быть пустым")
    @Schema(description = "Пароль пользователя", example = "newpassword123")
    private String password;
}
