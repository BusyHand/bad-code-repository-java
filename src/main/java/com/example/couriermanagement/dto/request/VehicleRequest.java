package com.example.couriermanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotBlank(message = "Марка транспорта не может быть пустой")
    private String brand;

    @NotBlank(message = "Номер транспорта не может быть пустым")
    private String licensePlate;

    @NotNull(message = "Максимальный вес не может быть null")
    @Positive(message = "Максимальный вес должен быть положительным числом")
    private BigDecimal maxWeight;

    @NotNull(message = "Максимальный объем не может быть null")
    @Positive(message = "Максимальный объем должен быть положительным числом")
    private BigDecimal maxVolume;
}