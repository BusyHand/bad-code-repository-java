package com.example.couriermanagement.dto;

import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryDto {
    private Long id;
    private String deliveryNumber;
    private UserDto courier;
    private VehicleDto vehicle;
    private UserDto createdBy;
    private LocalDate deliveryDate;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private DeliveryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DeliveryPointDto> deliveryPoints;
    private BigDecimal totalWeight;
    private BigDecimal totalVolume;
    @Builder.Default
    private Boolean canEdit = false;

    //todo должен с этим работать маппер
    public static DeliveryDto from(Delivery delivery) {
        // Calculate total weight and volume
        BigDecimal totalWeight = delivery.getDeliveryPoints().stream()
                .flatMap(dp -> dp.getDeliveryPointProducts().stream())
                .map(dpp -> dpp.getProduct().getWeight().multiply(BigDecimal.valueOf(dpp.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVolume = delivery.getDeliveryPoints().stream()
                .flatMap(dp -> dp.getDeliveryPointProducts().stream())
                .map(dpp -> dpp.getProduct().getVolume().multiply(BigDecimal.valueOf(dpp.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean canEdit = delivery.getDeliveryDate().isAfter(LocalDate.now().plusDays(3));

        return DeliveryDto.builder()
                .id(delivery.getId())
                .deliveryNumber("DEL-" + delivery.getDeliveryDate().getYear() + "-" + 
                        String.format("%03d", delivery.getId()))
                .courier(delivery.getCourier() != null ? UserDto.from(delivery.getCourier()) : null)
                .vehicle(delivery.getVehicle() != null ? VehicleDto.from(delivery.getVehicle()) : null)
                .createdBy(UserDto.from(delivery.getCreatedBy()))
                .deliveryDate(delivery.getDeliveryDate())
                .timeStart(delivery.getTimeStart())
                .timeEnd(delivery.getTimeEnd())
                .status(delivery.getStatus())
                .createdAt(delivery.getCreatedAt())
                .updatedAt(delivery.getUpdatedAt())
                .deliveryPoints(delivery.getDeliveryPoints().stream()
                        .map(DeliveryPointDto::from)
                        .collect(Collectors.toList()))
                .totalWeight(totalWeight)
                .totalVolume(totalVolume)
                .canEdit(canEdit)
                .build();
    }

    //todo использовать в контроллере
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeliveryFilterRequest {
        private LocalDate startDate;
        private LocalDate endDate;
        private Long courierId;
        private DeliveryStatus status;
    }
}