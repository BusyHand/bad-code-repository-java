package com.example.couriermanagement.dto.request;

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
public class RouteWithProducts {

    @NotEmpty(message = "Маршрут не может быть пустым")
    @Valid
    private List<DeliveryPointRequest> route;

    @NotEmpty(message = "Список товаров не может быть пустым")
    @Valid
    private List<DeliveryProductRequest> products;
}