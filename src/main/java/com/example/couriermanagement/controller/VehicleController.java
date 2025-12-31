package com.example.couriermanagement.controller;

import com.example.couriermanagement.dto.VehicleDto;
import com.example.couriermanagement.dto.request.VehicleRequest;
import com.example.couriermanagement.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@Tag(name = "Vehicles", description = "Управление машинами (админ)")
@SecurityRequirement(name = "bearerAuth")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    @Operation(
            summary = "Получить список всех машин",
            description = "Получение списка всех зарегистрированных в системе машин"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Список машин"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<List<VehicleDto>> getAllVehicles() {
        List<VehicleDto> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Создать новую машину",
            description = "Создание новой машины. Доступно только для админа"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Машина создана"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен")
            }
    )
    public ResponseEntity<VehicleDto> createVehicle(@Valid @RequestBody VehicleRequest vehicleRequest) {
        VehicleDto vehicle = vehicleService.createVehicle(vehicleRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Обновить данные машины",
            description = "Обновление данных машины. Доступно только для админа"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Машина обновлена"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Машина не найдена")
            }
    )
    public ResponseEntity<VehicleDto> updateVehicle(
            @Parameter(description = "ID машины", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest vehicleRequest
    ) {
        VehicleDto vehicle = vehicleService.updateVehicle(id, vehicleRequest);
        return ResponseEntity.ok(vehicle);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Удалить машину",
            description = "Удаление машины из системы. Доступно только для админа"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Машина удалена"),
                    @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
                    @ApiResponse(responseCode = "404", description = "Машина не найдена")
            }
    )
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "ID машины", example = "1")
            @PathVariable Long id
    ) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }
}
