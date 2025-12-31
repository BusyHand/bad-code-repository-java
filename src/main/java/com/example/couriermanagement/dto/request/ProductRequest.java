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
public class ProductRequest {

    @NotBlank(message = "Название товара не может быть пустым")
    private String name;

    @NotNull(message = "Вес не может быть null")
    @Positive(message = "Вес должен быть положительным числом")
    private BigDecimal weight;

    @NotNull(message = "Длина не может быть null")
    @Positive(message = "Длина должна быть положительным числом")
    private BigDecimal length;

    @NotNull(message = "Ширина не может быть null")
    @Positive(message = "Ширина должна быть положительным числом")
    private BigDecimal width;

    @NotNull(message = "Высота не может быть null")
    @Positive(message = "Высота должна быть положительным числом")
    private BigDecimal height;
}