package com.example.couriermanagement.controller;

import com.example.couriermanagement.controller.filter.impl.UserFilter;
import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.UserRequest;
import com.example.couriermanagement.dto.request.UserUpdateRequest;
import com.example.couriermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Управление пользователями (админ)")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(
            summary = "Получить список пользователей",
            description = "Получение списка пользователей с возможностью фильтрации по роли. Доступно только для админа"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список пользователей"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    //todo под тест
    public ResponseEntity<List<UserDto>> getAllUsers(
            @ParameterObject @ModelAttribute UserFilter userFilter
    ) {
        List<UserDto> users = userService.getAllUsers(userFilter);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @Operation(
            summary = "Создать нового пользователя",
            description = "Создание нового пользователя. Доступно только для админа"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Пользователь создан"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserDto user = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить данные пользователя",
            description = "Обновление данных пользователя. Доступно только для админа"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Пользователь обновлен"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "ID пользователя", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        UserDto user = userService.updateUser(id, userUpdateRequest);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and @use.canDeleteUser(#id)")
    @Operation(
            summary = "Удалить пользователя",
            description = "Удаление пользователя из системы. Доступно только для админа"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Пользователь удален"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
            }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя", example = "1")
            @Param("id")
            @PathVariable Long id
    ) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
