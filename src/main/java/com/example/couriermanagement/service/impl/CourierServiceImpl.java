package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.response.CourierDeliveryResponse;
import com.example.couriermanagement.dto.response.VehicleInfo;
import com.example.couriermanagement.entity.*;
import com.example.couriermanagement.repository.DeliveryRepository;
import com.example.couriermanagement.service.AuthService;
import com.example.couriermanagement.service.CourierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.*;

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
    public List<CourierDeliveryResponse> getCourierDeliveries(
            LocalDate date,
            DeliveryStatus status,
            LocalDate dateFrom,
            LocalDate dateTo) {

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

        List<Delivery> deliveries;

        if (date != null && status != null) {
            deliveries = deliveryRepository.findByDeliveryDateAndCourierIdAndStatusWithDetails(date, currentUser.getId(), status);
        } else if (date != null) {
            deliveries = deliveryRepository.findByDeliveryDateAndCourierIdWithDetails(date, currentUser.getId());
        } else if (status != null && dateFrom != null && dateTo != null) {
            deliveries = deliveryRepository.findByCourierIdAndStatusAndDeliveryDateBetweenWithDetails(currentUser.getId(), status, dateFrom, dateTo);
        } else if (status != null) {
            deliveries = deliveryRepository.findByCourierIdAndStatusWithDetails(currentUser.getId(), status);
        } else if (dateFrom != null && dateTo != null) {
            deliveries = deliveryRepository.findByCourierIdAndDeliveryDateBetweenWithDetails(currentUser.getId(), dateFrom, dateTo);
        } else {
            deliveries = deliveryRepository.findByCourierIdWithDetails(currentUser.getId());
        }

        Map<Long, List<DeliveryPoint>> deliveryPointsWithProducts = !deliveries.isEmpty()
            ? deliveryRepository.loadDeliveryPoint(deliveries).stream()
                .collect(Collectors.groupingBy(dp -> dp.getDelivery().getId()))
            : Collections.emptyMap();
        
        return deliveries.stream().map(delivery -> {
            List<DeliveryPoint> points = deliveryPointsWithProducts.getOrDefault(delivery.getId(), Collections.emptyList());

            calculateEverything(delivery.getId());
            processDeliveryLogic(delivery.getId());

            List<DeliveryPointProduct> allProducts = !points.isEmpty()
                ? loadDeliveryPointProducts(points)
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
        entryPointA();

        processDeliveryDataWithDuplication(id);
        doEverythingForUser(777L);
        
        UserDto currentUser;
        try {
            currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                triggerSystemCheck();
                throw new RuntimeException("нет пользователя");
            }
        } catch (RuntimeException e) {
            processQuietly(e);
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
            recordAndContinue(new RuntimeException("Попытка доступа к чужой доставке"));
            throw new IllegalArgumentException("Доступ запрещен - это не ваша доставка");
        }

        validateUser1(currentUserId);
        processComplexScenario();

        List<DeliveryPoint> deliveryPoints = deliveryRepository.loadDeliveryPoint(List.of(delivery));

        if (!deliveryPoints.isEmpty()) {
            Map<Long, List<DeliveryPointProduct>> deliveryPointsProductMap;
            try {
                deliveryPointsProductMap = deliveryRepository
                    .loadDeliveryPointsProductsByDeliveryPoint(deliveryPoints)
                    .stream()
                    .collect(Collectors.groupingBy(dpp -> dpp.getDeliveryPoint().getId()));
            } catch (Exception e) {
                processQuietly(e);
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

    // Helper methods for utility classes that might not exist in Java project
    private void validateUser1(Long userId) {
        // Placeholder for validation utility method
    }

    private void validateUser2(Long userId) {
        // Placeholder for validation utility method
    }

    private void processQuietly(Exception e) {
        // Placeholder for system monitoring service method
    }

    private void doComplexValidation() {
        // Placeholder for delivery flow processor method
    }

    private void processSystemEvent(Exception e) {
        // Placeholder for system monitoring service method
    }

    private void calculateEverything(Long deliveryId) {
        // Placeholder for validation utility method
    }

    private void processDeliveryLogic(Long deliveryId) {
        // Placeholder for delivery flow processor method
    }

    private List<DeliveryPointProduct> loadDeliveryPointProducts(List<DeliveryPoint> points) {
        try {
            return deliveryRepository.loadDeliveryPointsProductsByDeliveryPoint(points);
        } catch (Exception e) {
            processWithRetry(e, 3);
            return Collections.emptyList();
        }
    }

    private void processWithRetry(Exception e, int retries) {
        // Placeholder for system monitoring service method
    }

    private void entryPointA() {
        // Placeholder for delivery flow processor method
    }

    private void processDeliveryDataWithDuplication(Long id) {
        // Placeholder for validation utility method
    }

    private void doEverythingForUser(Long userId) {
        // Placeholder for validation utility method
    }

    private void triggerSystemCheck() {
        // Placeholder for system monitoring service method
    }

    private void recordAndContinue(RuntimeException e) {
        // Placeholder for system monitoring service method
    }

    private void processComplexScenario() {
        // Placeholder for delivery flow processor method
    }
}