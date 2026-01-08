package com.example.couriermanagement.service;


import com.example.couriermanagement.dto.request.DeliveryPointRequest;
import com.example.couriermanagement.dto.request.DeliveryProductRequest;
import com.example.couriermanagement.dto.request.DeliveryRequest;
import com.example.couriermanagement.dto.request.RouteWithProducts;
import com.example.couriermanagement.entity.*;
import com.example.couriermanagement.repository.DeliveryRepository;
import com.example.couriermanagement.repository.ProductRepository;
import com.example.couriermanagement.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryValidationService {

    public static final int MINUTES_IN_HOUR = 60;
    private final OpenStreetMapService openStreetMapService;
    private final DeliveryRepository deliveryRepository;
    private final VehicleRepository vehicleRepository;
    private final ProductRepository productRepository;

    public void validateDeliveryRequest(DeliveryRequest deliveryRequest) {
        if (!deliveryRequest.getTimeStart().isBefore(deliveryRequest.getTimeEnd())) {
            throw new IllegalArgumentException("Время начала должно быть раньше времени окончания");
        }

        if (deliveryRequest.getDeliveryDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Дата доставки не может быть в прошлом");
        }

        validateVehicleCapacity(deliveryRequest);

        if (deliveryRequest.getPoints().size() >= 2) {
            validateRouteTime(deliveryRequest);
        }
    }

    public void validateVehicleCapacity(DeliveryRequest deliveryRequest) {
        Vehicle vehicle = vehicleRepository.findById(deliveryRequest.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Машина не найдена"));

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal totalVolume = BigDecimal.ZERO;

        for (DeliveryPointRequest point : deliveryRequest.getPoints()) {
            for (DeliveryProductRequest productRequest : point.getProducts()) {
                Product product = productRepository.findById(productRequest.getProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Товар с ID " + productRequest.getProductId() + " не найден"));

                BigDecimal quantity = BigDecimal.valueOf(productRequest.getQuantity());
                totalWeight = totalWeight.add(product.getWeight().multiply(quantity));
                totalVolume = totalVolume.add(product.getVolume().multiply(quantity));
            }
        }

        List<Delivery> existingDeliveries = deliveryRepository.findByDateVehicleAndOverlappingTime(
                deliveryRequest.getDeliveryDate(),
                deliveryRequest.getVehicleId(),
                deliveryRequest.getTimeStart(),
                deliveryRequest.getTimeEnd()
        );

        BigDecimal existingWeight = BigDecimal.ZERO;
        BigDecimal existingVolume = BigDecimal.ZERO;

        if (!existingDeliveries.isEmpty()) {
            List<DeliveryPoint> deliveryPoints = deliveryRepository.loadDeliveryPoint(existingDeliveries);
            if (!deliveryPoints.isEmpty()) {
                List<DeliveryPointProduct> products = deliveryRepository.loadDeliveryPointsProductsByDeliveryPoint(deliveryPoints);
                for (DeliveryPointProduct dpp : products) {
                    BigDecimal quantity = BigDecimal.valueOf(dpp.getQuantity());
                    existingWeight = existingWeight.add(dpp.getProduct().getWeight().multiply(quantity));
                    existingVolume = existingVolume.add(dpp.getProduct().getVolume().multiply(quantity));
                }
            }
        }

        BigDecimal totalRequiredWeight = existingWeight.add(totalWeight);
        BigDecimal totalRequiredVolume = existingVolume.add(totalVolume);

        if (totalRequiredWeight.compareTo(vehicle.getMaxWeight()) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Превышена грузоподъемность машины в период %s-%s. " +
                            "Максимум: %s кг, требуется: %s кг " +
                            "(пересекающиеся доставки: %s кг, новые: %s кг)",
                    deliveryRequest.getTimeStart(), deliveryRequest.getTimeEnd(),
                    vehicle.getMaxWeight(), totalRequiredWeight, existingWeight, totalWeight
            ));
        }

        if (totalRequiredVolume.compareTo(vehicle.getMaxVolume()) > 0) {
            throw new IllegalArgumentException(String.format(
                    "Превышен объем машины в период %s-%s. " +
                            "Максимум: %s м³, требуется: %s м³ " +
                            "(пересекающиеся доставки: %s м³, новые: %s м³)",
                    deliveryRequest.getTimeStart(), deliveryRequest.getTimeEnd(),
                    vehicle.getMaxVolume(), totalRequiredVolume, existingVolume, totalVolume
            ));
        }
    }

    public void validateRouteTime(DeliveryRequest deliveryRequest) {
        DeliveryPointRequest firstPoint = deliveryRequest.getPoints().get(0);
        DeliveryPointRequest lastPoint = deliveryRequest.getPoints().get(deliveryRequest.getPoints().size() - 1);

        BigDecimal distanceKm = openStreetMapService.calculateDistance(
                firstPoint.getLatitude(),
                firstPoint.getLongitude(),
                lastPoint.getLatitude(),
                lastPoint.getLongitude()
        );

        BigDecimal speedKmPerHour = BigDecimal.valueOf(MINUTES_IN_HOUR);
        BigDecimal requiredHours = distanceKm.divide(speedKmPerHour, 4, RoundingMode.HALF_UP);

        int breakMinutesPerPoint = 30;
        int totalBreakMinutes = deliveryRequest.getPoints().size() * breakMinutesPerPoint;
        long totalRequiredMinutes = (long) (requiredHours.doubleValue() * MINUTES_IN_HOUR) + totalBreakMinutes;

        LocalTime timeStart = deliveryRequest.getTimeStart();
        LocalTime timeEnd = deliveryRequest.getTimeEnd();
        long availableMinutes = Duration.between(timeStart, timeEnd).toMinutes();

        if (totalRequiredMinutes > availableMinutes) {
            throw new IllegalArgumentException(String.format(
                    "Недостаточно времени для выполнения маршрута. " +
                            "Требуется: %d мин (%.1f ч), доступно: %d мин (%.1f ч). " +
                            "Расстояние: %s км",
                    totalRequiredMinutes, totalRequiredMinutes / 60.0,
                    availableMinutes, availableMinutes / 60.0,
                    distanceKm
            ));
        }
    }

    public boolean validateGenerationConditions(User courier, Vehicle vehicle, LocalDate date,
                                                RouteWithProducts route, int idx,
                                                List<String> warnings) {
        if (courier == null) {
            warnings.add("Курьер null");
            return false;
        }
        if (vehicle == null) {
            warnings.add("Машина null");
            return false;
        }
        if (route == null) {
            warnings.add("Маршрут null");
            return false;
        }
        if (route.getRoute().isEmpty()) {
            warnings.add("Пустой маршрут");
            return false;
        }
        if (route.getProducts().isEmpty()) {
            warnings.add("Нет товаров в маршруте");
            return false;
        }
        if (!courier.getRole().equals(UserRole.COURIER)) {
            warnings.add("Пользователь не курьер");
            return false;
        }
        if (vehicle.getMaxWeight().compareTo(BigDecimal.ZERO) <= 0) {
            warnings.add("Нулевая грузоподъемность машины");
            return false;
        }
        if (vehicle.getMaxVolume().compareTo(BigDecimal.ZERO) <= 0) {
            warnings.add("Нулевой объем машины");
            return false;
        }
        if (!date.isAfter(LocalDate.now())) {
            warnings.add("Дата доставки в прошлом");
            return false;
        }
        if (idx >= 10) {
            warnings.add("Слишком большой индекс маршрута");
            return false;
        }
        if (route.getRoute().size() >= 20) {
            warnings.add("Слишком много точек в маршруте");
            return false;
        }
        if (route.getProducts().size() >= 50) {
            warnings.add("Слишком много товаров в маршруте");
            return false;
        }
        return true;
    }
}
