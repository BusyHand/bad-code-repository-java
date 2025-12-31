package com.example.couriermanagement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPointRequest {

    @NotNull(message = "Номер последовательности не может быть null")
    @Min(value = 1, message = "Номер последовательности должен быть больше 0")
    private Integer sequence;

    @NotNull(message = "Широта не может быть null")
    private BigDecimal latitude;

    @NotNull(message = "Долгота не может быть null")
    private BigDecimal longitude;

    @NotEmpty(message = "Список товаров не может быть пустым")
    @Valid
    private List<DeliveryProductRequest> products;
}