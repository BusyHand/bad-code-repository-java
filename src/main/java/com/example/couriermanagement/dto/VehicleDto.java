package com.example.couriermanagement.dto;

import com.example.couriermanagement.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleDto {
    private Long id;
    private String brand;
    private String licensePlate;
    private BigDecimal maxWeight;
    private BigDecimal maxVolume;

    //todo в мапперы
    public static VehicleDto from(Vehicle vehicle) {
        return VehicleDto.builder()
                .id(vehicle.getId())
                .brand(vehicle.getBrand())
                .licensePlate(vehicle.getLicensePlate())
                .maxWeight(vehicle.getMaxWeight())
                .maxVolume(vehicle.getMaxVolume())
                .build();
    }

    //todo неиспользуемый код
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateVehicleRequest {
        @NotBlank(message = "Марка не может быть пустой")
        @Size(max = 100, message = "Марка не может быть длиннее 100 символов")
        private String brand;

        @NotBlank(message = "Госномер не может быть пустым")
        @Size(max = 20, message = "Госномер не может быть длиннее 20 символов")
        private String licensePlate;

        @NotNull(message = "Максимальный вес должен быть указан")
        @Positive(message = "Максимальный вес должен быть положительным")
        private BigDecimal maxWeight;

        @NotNull(message = "Максимальный объем должен быть указан")
        @Positive(message = "Максимальный объем должен быть положительным")
        private BigDecimal maxVolume;
    }

    //todo неиспользуемый код
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateVehicleRequest {
        private String brand;
        private String licensePlate;
        private BigDecimal maxWeight;
        private BigDecimal maxVolume;
    }
}