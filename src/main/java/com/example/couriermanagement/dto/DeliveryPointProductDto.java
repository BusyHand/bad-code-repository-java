package com.example.couriermanagement.dto;

import com.example.couriermanagement.entity.DeliveryPointProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPointProductDto {
    private Long id;
    private ProductDto product;
    private Integer quantity;

    //todo должен с этим работать маппер
    public static DeliveryPointProductDto from(DeliveryPointProduct deliveryPointProduct) {
        return DeliveryPointProductDto.builder()
                .id(deliveryPointProduct.getId())
                .product(ProductDto.from(deliveryPointProduct.getProduct()))
                .quantity(deliveryPointProduct.getQuantity())
                .build();
    }
}