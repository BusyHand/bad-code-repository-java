package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.controller.filter.Filter;
import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.response.CourierDeliveryResponse;
import com.example.couriermanagement.dto.response.VehicleInfo;
import com.example.couriermanagement.entity.*;
import com.example.couriermanagement.repository.DeliveryRepository;
import com.example.couriermanagement.service.AuthService;
import com.example.couriermanagement.service.CourierService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@Transactional(readOnly = true)
public class CourierServiceImpl implements CourierService {

    private final DeliveryRepository deliveryRepository;
    private final AuthService authService;

    public CourierServiceImpl(DeliveryRepository deliveryRepository, AuthService authService) {
        this.deliveryRepository = deliveryRepository;
        this.authService = authService;
    }

    private Specification<Delivery> byUserId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("courier").get("id"), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourierDeliveryResponse> getCourierDeliveries(Filter<Delivery> courierDeliveryFilter) {

        User currentUser = authService.getCurrentUser();

        var filter = courierDeliveryFilter.filter()
                .and(byUserId(currentUser.getId()));

        List<Delivery> deliveries = deliveryRepository.findAll(filter);

        Map<Long, List<DeliveryPoint>> deliveryPointsWithProducts = !deliveries.isEmpty()
                ? deliveryRepository.loadDeliveryPoint(deliveries).stream()
                .collect(groupingBy(dp -> dp.getDelivery().getId()))
                : Collections.emptyMap();

        return deliveries.stream().map(delivery -> {
            List<DeliveryPoint> points = deliveryPointsWithProducts.getOrDefault(delivery.getId(), Collections.emptyList());

            List<DeliveryPointProduct> allProducts = !points.isEmpty()
                    ? deliveryRepository.loadDeliveryPointsProductsByDeliveryPoint(points)
                    : Collections.emptyList();

            BigDecimal totalWeight = allProducts.stream()
                    .map(DeliveryPointProduct::getTotalWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalProductsCount = allProducts.stream()
                    .mapToInt(DeliveryPointProduct::getQuantity)
                    .sum();

            Vehicle vehicle = delivery.getVehicle();
            return CourierDeliveryResponse.builder()
                    .id(delivery.getId())
                    .deliveryNumber(generateDeliveryNumber(delivery))
                    .deliveryDate(delivery.getDeliveryDate())
                    .timeStart(delivery.getTimeStart())
                    .timeEnd(delivery.getTimeEnd())
                    .status(delivery.getStatus())
                    .vehicle(getVehicleInfo(vehicle))
                    .pointsCount(points.size())
                    .productsCount(totalProductsCount)
                    .totalWeight(totalWeight)
                    .build();
        }).collect(toList());
    }

    private VehicleInfo getVehicleInfo(Vehicle vehicle) {
        return VehicleInfo.builder()
                .brand(vehicle != null ? vehicle.getBrand() : "Не назначена")
                .licensePlate(vehicle != null ? vehicle.getLicensePlate() : "")
                .build();
    }

    private String generateDeliveryNumber(final Delivery delivery) {
        final String format = "DEL-%d-%03d";
        return format(format, delivery.getDeliveryDate().getYear(), delivery.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryDto getCourierDeliveryById(Long id) {
        Delivery delivery = deliveryRepository.findWithPointsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Доставка не найдена"));
        List<DeliveryPoint> points = deliveryRepository.findPointsWithProducts(delivery.getId());
        delivery.setDeliveryPoints(points);
        return DeliveryDto.from(delivery);
    }


}