package com.example.couriermanagement.controller;

import com.example.couriermanagement.dto.request.RouteCalculationRequest;
import com.example.couriermanagement.dto.response.RouteCalculationResponse;
import com.example.couriermanagement.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routes")
@Tag(name = "Deliveries", description = "Управление доставками (менеджер)")
@SecurityRequirement(name = "bearerAuth")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping("/calculate")
    @Operation(
            summary = "Рассчитать время маршрута",
            description = "Используется для проверки времени при создании доставки"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Расчет маршрута"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
            }
    )
    public ResponseEntity<RouteCalculationResponse> calculateRoute(
            @Valid @RequestBody RouteCalculationRequest request
    ) {
        RouteCalculationResponse response = routeService.calculateRoute(request);
        return ResponseEntity.ok(response);
    }
}
