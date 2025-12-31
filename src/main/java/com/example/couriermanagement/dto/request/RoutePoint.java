package com.example.couriermanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Точка маршрута для расчета")
public class RoutePoint {

    @Schema(description = "Широта", example = "55.7558")
    private BigDecimal latitude;

    @Schema(description = "Долгота", example = "37.6173")
    private BigDecimal longitude;
}
