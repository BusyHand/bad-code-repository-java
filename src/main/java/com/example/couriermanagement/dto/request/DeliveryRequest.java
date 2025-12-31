package com.example.couriermanagement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryRequest {

    @NotNull(message = "ID курьера не может быть null")
    private Long courierId;

    @NotNull(message = "ID транспорта не может быть null")
    private Long vehicleId;

    @NotNull(message = "Дата доставки не может быть null")
    @Future(message = "Дата доставки должна быть в будущем")
    private LocalDate deliveryDate;

    @NotNull(message = "Время начала не может быть null")
    private LocalTime timeStart;

    @NotNull(message = "Время окончания не может быть null")
    private LocalTime timeEnd;

    @NotEmpty(message = "Список точек доставки не может быть пустым")
    @Valid
    private List<DeliveryPointRequest> points;
}