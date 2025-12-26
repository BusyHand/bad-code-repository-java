package com.example.couriermanagement.dto;

import com.example.couriermanagement.entity.DeliveryPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPointDto {
    private Long id;
    private Integer sequence;
    private BigDecimal latitude;
    private BigDecimal longitude;
    @Builder.Default
    private List<DeliveryPointProductDto> products = List.of();

    //todo должен с этим работать маппер
    public static DeliveryPointDto from(DeliveryPoint deliveryPoint) {
        return DeliveryPointDto.builder()
                .id(deliveryPoint.getId())
                .sequence(deliveryPoint.getSequence())
                .latitude(deliveryPoint.getLatitude())
                .longitude(deliveryPoint.getLongitude())
                .products(deliveryPoint.getDeliveryPointProducts().stream()
                        .map(DeliveryPointProductDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}