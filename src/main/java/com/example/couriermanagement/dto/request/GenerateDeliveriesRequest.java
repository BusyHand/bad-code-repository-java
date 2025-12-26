package com.example.couriermanagement.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateDeliveriesRequest {

    @NotEmpty(message = "Данные для генерации доставок не могут быть пустыми")
    @Valid
    private Map<LocalDate, List<RouteWithProducts>> deliveryData;
}