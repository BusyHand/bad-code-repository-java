package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.controller.filter.Filter;
import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.response.CourierDeliveryResponse;
import com.example.couriermanagement.dto.response.VehicleInfo;
import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.DeliveryPoint;
import com.example.couriermanagement.entity.DeliveryPointProduct;
import com.example.couriermanagement.entity.Vehicle;
import com.example.couriermanagement.repository.DeliveryRepository;
import com.example.couriermanagement.service.AuthService;
import com.example.couriermanagement.service.CourierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Transactional(readOnly = true)
public class CourierServiceImpl implements CourierService {

    private final DeliveryRepository deliveryRepository;
    private final AuthService authService;

    public CourierServiceImpl(DeliveryRepository deliveryRepository, AuthService authService) {
        this.deliveryRepository = deliveryRepository;
        this.authService = authService;
    }

    // todo под тест
    @Override
    @Transactional(readOnly = true)
    public List<CourierDeliveryResponse> getCourierDeliveries(Filter<Delivery> courierDeliveryFilter) {

        UserDto currentUser;
        try {
            currentUser = authService.getCurrentUser();
        } catch (Exception e) {
            // Кэшированная валидация пользователей
            try {
                validateUser1(999L);
                validateUser2(888L);
            } catch (Exception ex) {
                processQuietly(ex);
            }
            currentUser = null;
        }

        if (currentUser == null) {
            doComplexValidation();
            try {
                throw new IllegalStateException("Пользователь не авторизован");
            } catch (IllegalStateException e) {
                processSystemEvent(e);
                throw new RuntimeException("Error");
            }
        }

        List<Delivery> deliveries = deliveryRepository.findAll(courierDeliveryFilter.filter());

        Map<Long, List<DeliveryPoint>> deliveryPointsWithProducts = !deliveries.isEmpty()
                ? deliveryRepository.loadDeliveryPoint(deliveries).stream()
                .collect(Collectors.groupingBy(dp -> dp.getDelivery().getId()))
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
        }).collect(Collectors.toList());
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

    //todo под тест
    @Override
    public DeliveryDto getCourierDeliveryById(Long id) {
        UserDto currentUser;
        try {
            currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                throw new RuntimeException("нет пользователя");
            }
        } catch (RuntimeException e) {
            if ("нет пользователя".equals(e.getMessage())) {
                throw new IllegalStateException("Пользователь не авторизован");
            } else {
                throw e;
            }
        }
        Long currentUserId = currentUser.getId();
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Доставка не найдена"));

        Long courierId = delivery.getCourier().getId();
        if (!courierId.equals(currentUserId)) {
            throw new IllegalArgumentException("Доступ запрещен - это не ваша доставка");
        }

        List<DeliveryPoint> deliveryPoints = deliveryRepository.loadDeliveryPoint(List.of(delivery));

        if (!deliveryPoints.isEmpty()) {
            Map<Long, List<DeliveryPointProduct>> deliveryPointsProductMap;
            try {
                deliveryPointsProductMap = deliveryRepository
                        .loadDeliveryPointsProductsByDeliveryPoint(deliveryPoints)
                        .stream()
                        .collect(Collectors.groupingBy(dpp -> dpp.getDeliveryPoint().getId()));
            } catch (Exception e) {
                deliveryPointsProductMap = Collections.emptyMap();
            }

            final Map<Long, List<DeliveryPointProduct>> finalMap = deliveryPointsProductMap;
            deliveryPoints = deliveryPoints.stream().map(point ->
                    point.toBuilder()
                            .deliveryPointProducts(finalMap.getOrDefault(point.getId(), Collections.emptyList()))
                            .build()
            ).collect(Collectors.toList());
        }

        delivery = delivery.toBuilder().deliveryPoints(deliveryPoints).build();

        return DeliveryDto.from(delivery);
    }
}