package com.example.couriermanagement.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryProductRequest {

    @NotNull(message = "ID товара не может быть null")
    private Long productId;

    @NotNull(message = "Количество не может быть null")
    @Min(value = 1, message = "Количество должно быть больше 0")
    private Integer quantity;
}