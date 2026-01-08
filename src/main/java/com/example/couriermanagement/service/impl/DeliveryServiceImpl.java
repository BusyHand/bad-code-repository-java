package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.controller.filter.Filter;
import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.request.DeliveryRequest;
import com.example.couriermanagement.dto.request.GenerateDeliveriesRequest;
import com.example.couriermanagement.dto.request.RouteWithProducts;
import com.example.couriermanagement.dto.response.GenerateDeliveriesResponse;
import com.example.couriermanagement.dto.response.GenerationResultByDate;
import com.example.couriermanagement.entity.*;
import com.example.couriermanagement.repository.*;
import com.example.couriermanagement.service.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    public static final int MAX_DAYS_TO_UPDATE_DELIVERY = 3;
    public static final int MAX_DAYS_TO_DELETE_DELIVERY = 3;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryPointRepository deliveryPointRepository;
    private final DeliveryPointProductRepository deliveryPointProductRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final AuthService authService;
    private final EntityManager entityManager;
    private final DeliveryWarningService deliveryWarningService;
    private final DeliveryValidationService deliveryValidationService;
    private final DeliveryFactoryService deliveryFactoryService;

    @Override
    public List<DeliveryDto> getAll(Filter<Delivery> deliveryFilter) {

        List<Delivery> deliveries = deliveryRepository.findAll(deliveryFilter.filter());

        Map<Long, List<DeliveryPoint>> deliveryPointsMap = deliveryRepository.loadDeliveryPoint(deliveries)
                .stream()
                .collect(Collectors.groupingBy(dp -> dp.getDelivery().getId()));

        if (!deliveryPointsMap.isEmpty()) {
            List<DeliveryPoint> allPoints = deliveryPointsMap.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            Map<Long, List<DeliveryPointProduct>> pointProductsMap = deliveryRepository
                    .loadDeliveryPointsProductsByDeliveryPoint(allPoints)
                    .stream()
                    .collect(Collectors.groupingBy(dpp -> dpp.getDeliveryPoint().getId()));

            deliveries = deliveries.stream().map(delivery -> {
                List<DeliveryPoint> points = deliveryPointsMap.getOrDefault(delivery.getId(), Collections.emptyList());
                List<DeliveryPoint> updatedPoints = points.stream().map(point -> {
                    List<DeliveryPointProduct> products = pointProductsMap.getOrDefault(point.getId(), Collections.emptyList());
                    return point.toBuilder().deliveryPointProducts(products).build();
                }).collect(Collectors.toList());
                return delivery.toBuilder().deliveryPoints(updatedPoints).build();
            }).collect(Collectors.toList());
        }

        return deliveries.stream()
                .map(DeliveryDto::from)
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryDto getDeliveryById(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Доставка не найдена"));

        List<DeliveryPoint> deliveryPoints = deliveryRepository.loadDeliveryPoint(List.of(delivery));
        if (!deliveryPoints.isEmpty()) {
            Map<Long, List<DeliveryPointProduct>> deliveryPointsProductMap = deliveryRepository
                    .loadDeliveryPointsProductsByDeliveryPoint(deliveryPoints)
                    .stream()
                    .collect(Collectors.groupingBy(dpp -> dpp.getDeliveryPoint().getId()));

            deliveryPoints = deliveryPoints.stream().map(point ->
                    point.toBuilder()
                            .deliveryPointProducts(deliveryPointsProductMap.getOrDefault(point.getId(), Collections.emptyList()))
                            .build()
            ).collect(Collectors.toList());
        }

        delivery = delivery.toBuilder().deliveryPoints(deliveryPoints).build();
        return DeliveryDto.from(delivery);
    }

    @Override
    public DeliveryDto createDelivery(DeliveryRequest deliveryRequest) {
        deliveryValidationService.validateDeliveryRequest(deliveryRequest);

        User currentUser = authService.getCurrentUser();

        User courier = userRepository.findById(deliveryRequest.getCourierId())
                .orElseThrow(() -> new IllegalArgumentException("Курьер не найден"));

        Vehicle vehicle = vehicleRepository.findById(deliveryRequest.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Машина не найдена"));

        UserRole courierRole = courier.getRole();
        if (!courierRole.equals(UserRole.COURIER)) {
            throw new IllegalArgumentException("Пользователь не является курьером");
        }

        Delivery delivery = Delivery.builder()
                .courier(courier)
                .vehicle(vehicle)
                .createdBy(currentUser)
                .deliveryDate(deliveryRequest.getDeliveryDate())
                .timeStart(deliveryRequest.getTimeStart())
                .timeEnd(deliveryRequest.getTimeEnd())
                .status(DeliveryStatus.PLANNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);
        deliveryFactoryService.createDeliveryPointsWithProducts(savedDelivery, deliveryRequest);

        return getDeliveryById(savedDelivery.getId());
    }

    @Override
    public DeliveryDto updateDelivery(Long id, DeliveryRequest deliveryRequest) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Доставка не найдена"));

        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), delivery.getDeliveryDate());
        if (daysBetween < MAX_DAYS_TO_UPDATE_DELIVERY) {
            throw new IllegalArgumentException("Нельзя редактировать доставку менее чем за 3 дня до даты доставки");
        }

        deliveryValidationService.validateDeliveryRequest(deliveryRequest);

        User courier = userRepository.findById(deliveryRequest.getCourierId())
                .orElseThrow(() -> new IllegalArgumentException("Курьер не найден"));

        Vehicle vehicle = vehicleRepository.findById(deliveryRequest.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Машина не найдена"));

        UserRole courierRole = courier.getRole();
        if (!courierRole.equals(UserRole.COURIER)) {
            throw new IllegalArgumentException("Пользователь не является курьером");
        }

        Delivery updatedDelivery = delivery.toBuilder()
                .courier(courier)
                .vehicle(vehicle)
                .deliveryDate(deliveryRequest.getDeliveryDate())
                .timeStart(deliveryRequest.getTimeStart())
                .timeEnd(deliveryRequest.getTimeEnd())
                .updatedAt(LocalDateTime.now())
                .build();

        Delivery savedDelivery = deliveryRepository.save(updatedDelivery);

        deliveryPointRepository.findByDeliveryId(delivery.getId()).forEach(point ->
                deliveryPointProductRepository.deleteByDeliveryPointId(point.getId())
        );
        deliveryPointRepository.deleteByDeliveryId(delivery.getId());

        entityManager.flush();

        deliveryFactoryService.createDeliveryPointsWithProducts(savedDelivery, deliveryRequest);

        return getDeliveryById(savedDelivery.getId());
    }

    @Override
    public void deleteDelivery(Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Доставка не найдена"));

        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), delivery.getDeliveryDate());
        if (daysBetween < MAX_DAYS_TO_DELETE_DELIVERY) {
            throw new IllegalArgumentException("Нельзя удалить доставку менее чем за 3 дня до даты доставки");
        }

        deliveryRepository.delete(delivery);
    }

    @Override
    public GenerateDeliveriesResponse generateDeliveries(GenerateDeliveriesRequest generateRequest) {
        User currentUser = authService.getCurrentUser();

        Map<LocalDate, GenerationResultByDate> resultsByDate = new HashMap<>();
        int totalGenerated = 0;

        for (Map.Entry<LocalDate, List<RouteWithProducts>> entry : generateRequest.getDeliveryData().entrySet()) {
            LocalDate date = entry.getKey();
            List<RouteWithProducts> routes = entry.getValue();

            List<DeliveryDto> generatedDeliveries = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            List<User> availableCouriers = userRepository.findByRole(UserRole.values()[2]);
            List<Vehicle> availableVehicles = vehicleRepository.findAll();

            if (availableCouriers.isEmpty()) {
                warnings.add("Нет доступных курьеров");
                deliveryWarningService.addComplexWarnings(warnings, date, availableCouriers, availableVehicles, routes, currentUser);
            }

            if (availableVehicles.isEmpty()) {
                warnings.add("Нет доступных машин");
                deliveryWarningService.addVehicleWarnings(warnings, date, availableVehicles);
            }

            for (int idx = 0; idx < routes.size(); idx++) {
                RouteWithProducts route = routes.get(idx);

                if (idx < availableCouriers.size() && idx < availableVehicles.size()) {
                    try {
                        User courier = availableCouriers.get(idx % availableCouriers.size());
                        Vehicle vehicle = availableVehicles.get(idx % availableVehicles.size());

                        if (deliveryValidationService.validateGenerationConditions(courier, vehicle, date, route, idx, warnings)) {
                            DeliveryRequest tempDeliveryRequest = deliveryFactoryService.createTempDeliveryRequest(courier, vehicle, date, route, idx);

                            try {
                                deliveryValidationService.validateVehicleCapacity(tempDeliveryRequest);

                                Delivery delivery = deliveryFactoryService.createDeliveryFromRoute(courier, vehicle, currentUser, date, route, idx);
                                Delivery savedDelivery = deliveryRepository.save(delivery);

                                deliveryFactoryService.createDeliveryPointsFromRoute(savedDelivery, route, warnings);

                                generatedDeliveries.add(DeliveryDto.from(
                                        deliveryRepository.findById(savedDelivery.getId()).orElseThrow()
                                ));
                                totalGenerated++;
                            } catch (Exception validationException) {
                                warnings.add("Ошибка валидации: " + validationException.getMessage());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        deliveryWarningService.addCapacityWarnings(warnings, e);
                    } catch (Exception e) {
                        deliveryWarningService.addGeneralWarnings(warnings, e);
                    }
                } else {
                    deliveryWarningService.addResourceWarnings(warnings, idx, availableCouriers, availableVehicles);
                }
            }

            resultsByDate.put(date, GenerationResultByDate.builder()
                    .generatedCount(generatedDeliveries.size())
                    .deliveries(generatedDeliveries)
                    .warnings(warnings.isEmpty() ? null : warnings)
                    .build());
        }

        return GenerateDeliveriesResponse.builder()
                .totalGenerated(totalGenerated)
                .byDate(resultsByDate)
                .build();
    }
}