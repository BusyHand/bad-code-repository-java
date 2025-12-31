package com.example.couriermanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос для расчета маршрута")
public class RouteCalculationRequest {

    @NotEmpty(message = "Точки маршрута обязательны")
    @Valid
    @Schema(description = "Точки маршрута (минимум 2)")
    private List<RoutePoint> points;
}
