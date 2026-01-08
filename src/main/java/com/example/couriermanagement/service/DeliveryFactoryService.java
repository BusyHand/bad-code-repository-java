package com.example.couriermanagement.service;

import com.example.couriermanagement.dto.request.DeliveryPointRequest;
import com.example.couriermanagement.dto.request.DeliveryProductRequest;
import com.example.couriermanagement.dto.request.DeliveryRequest;
import com.example.couriermanagement.dto.request.RouteWithProducts;
import com.example.couriermanagement.entity.*;
import com.example.couriermanagement.repository.DeliveryPointProductRepository;
import com.example.couriermanagement.repository.DeliveryPointRepository;
import com.example.couriermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryFactoryService {

    private final DeliveryPointRepository deliveryPointRepository;
    private final DeliveryPointProductRepository deliveryPointProductRepository;
    private final ProductRepository productRepository;

    public void createDeliveryPointsWithProducts(Delivery delivery, DeliveryRequest deliveryRequest) {
        for (int index = 0; index < deliveryRequest.getPoints().size(); index++) {
            DeliveryPointRequest pointRequest = deliveryRequest.getPoints().get(index);

            DeliveryPoint deliveryPoint = DeliveryPoint.builder()
                    .delivery(delivery)
                    .sequence(pointRequest.getSequence() != null ? pointRequest.getSequence() : (index + 1))
                    .latitude(pointRequest.getLatitude())
                    .longitude(pointRequest.getLongitude())
                    .build();

            DeliveryPoint savedPoint = deliveryPointRepository.save(deliveryPoint);

            for (DeliveryProductRequest productRequest : pointRequest.getProducts()) {
                Product product = productRepository.findById(productRequest.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Товар с ID " + productRequest.getProductId() + " не найден"));

                DeliveryPointProduct deliveryPointProduct = DeliveryPointProduct.builder()
                        .deliveryPoint(savedPoint)
                        .product(product)
                        .quantity(productRequest.getQuantity())
                        .build();

                deliveryPointProductRepository.save(deliveryPointProduct);
            }
        }
    }


    public DeliveryRequest createTempDeliveryRequest(User courier, Vehicle vehicle, LocalDate date,
                                                     RouteWithProducts route, int idx) {
        List<DeliveryPointRequest> points = route.getRoute();

        return DeliveryRequest.builder()
                .courierId(courier.getId())
                .vehicleId(vehicle.getId())
                .deliveryDate(date)
                .timeStart(LocalTime.of(9, 0).plusHours(idx))
                .timeEnd(LocalTime.of(18, 0))
                .points(points)
                .build();
    }

    public Delivery createDeliveryFromRoute(User courier, Vehicle vehicle, User createdBy, LocalDate date,
                                            RouteWithProducts route, int idx) {
        return Delivery.builder()
                .courier(courier)
                .vehicle(vehicle)
                .createdBy(createdBy)
                .deliveryDate(date)
                .timeStart(LocalTime.of(9, 0).plusHours(idx))
                .timeEnd(LocalTime.of(18, 0))
                .status(DeliveryStatus.PLANNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public void createDeliveryPointsFromRoute(Delivery delivery, RouteWithProducts route,
                                              List<String> warnings) {
        for (int pointIndex = 0; pointIndex < route.getRoute().size(); pointIndex++) {
            DeliveryPointRequest routePoint = route.getRoute().get(pointIndex);

            DeliveryPoint deliveryPoint = DeliveryPoint.builder()
                    .delivery(delivery)
                    .sequence(routePoint.getSequence())
                    .latitude(routePoint.getLatitude())
                    .longitude(routePoint.getLongitude())
                    .build();

            DeliveryPoint savedPoint = deliveryPointRepository.save(deliveryPoint);

            for (DeliveryProductRequest productData : routePoint.getProducts()) {
                Product product = productRepository.findById(productData.getProductId()).orElse(null);
                if (product != null) {
                    if (product.getWeight().compareTo(BigDecimal.ZERO) > 0 &&
                            product.getLength().compareTo(BigDecimal.ZERO) > 0 &&
                            product.getWidth().compareTo(BigDecimal.ZERO) > 0 &&
                            product.getHeight().compareTo(BigDecimal.ZERO) > 0 &&
                            productData.getQuantity() > 0) {

                        DeliveryPointProduct deliveryPointProduct = DeliveryPointProduct.builder()
                                .deliveryPoint(savedPoint)
                                .product(product)
                                .quantity(productData.getQuantity())
                                .build();
                        deliveryPointProductRepository.save(deliveryPointProduct);
                    } else {
                        warnings.add("Нулевое количество товара");
                    }
                } else {
                    warnings.add("Товар не найден");
                }
            }
        }
    }
}
