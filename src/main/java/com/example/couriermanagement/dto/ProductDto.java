package com.example.couriermanagement.dto;

import com.example.couriermanagement.entity.Product;
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
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal volume;

    public static ProductDto from(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .weight(product.getWeight())
                .length(product.getLength())
                .width(product.getWidth())
                .height(product.getHeight())
                .volume(product.getVolume())
                .build();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateProductRequest {
        @NotBlank(message = "Название не может быть пустым")
        private String name;

        @NotNull(message = "Вес должен быть указан")
        @Positive(message = "Вес должен быть положительным")
        private BigDecimal weight;

        @NotNull(message = "Длина должна быть указана")
        @Positive(message = "Длина должна быть положительной")
        private BigDecimal length;

        @NotNull(message = "Ширина должна быть указана")
        @Positive(message = "Ширина должна быть положительной")
        private BigDecimal width;

        @NotNull(message = "Высота должна быть указана")
        @Positive(message = "Высота должна быть положительной")
        private BigDecimal height;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateProductRequest {
        private String name;
        private BigDecimal weight;
        private BigDecimal length;
        private BigDecimal width;
        private BigDecimal height;
    }
}