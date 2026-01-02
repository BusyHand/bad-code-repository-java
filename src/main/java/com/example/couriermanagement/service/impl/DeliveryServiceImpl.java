package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.controller.filter.Filter;
import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.*;
import com.example.couriermanagement.dto.response.GenerateDeliveriesResponse;
import com.example.couriermanagement.dto.response.GenerationResultByDate;
import com.example.couriermanagement.entity.*;
import com.example.couriermanagement.repository.*;
import com.example.couriermanagement.service.AuthService;
import com.example.couriermanagement.service.DeliveryService;
import com.example.couriermanagement.service.OpenStreetMapService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    public static final int MAX_DAYS_TO_UPDATE_DELIVERY = 3;
    public static final int MAX_DAYS_TO_DELETE_DELIVERY = 3;
    public static final int MINUTES_IN_HOUR = 60;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryPointRepository deliveryPointRepository;
    private final DeliveryPointProductRepository deliveryPointProductRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ProductRepository productRepository;
    private final AuthService authService;
    private final OpenStreetMapService openStreetMapService;
    private final EntityManager entityManager;

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
        validateDeliveryRequest(deliveryRequest);

        UserDto currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Пользователь не авторизован");
        }

        User createdBy = userRepository.findByLogin(currentUser.getLogin())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

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
                .createdBy(createdBy)
                .deliveryDate(deliveryRequest.getDeliveryDate())
                .timeStart(deliveryRequest.getTimeStart())
                .timeEnd(deliveryRequest.getTimeEnd())
                .status(DeliveryStatus.PLANNED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Delivery savedDelivery = deliveryRepository.save(delivery);
        createDeliveryPointsWithProducts(savedDelivery, deliveryRequest);

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

        validateDeliveryRequest(deliveryRequest);

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

        createDeliveryPointsWithProducts(savedDelivery, deliveryRequest);

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
        UserDto currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Пользователь не авторизован");
        }

        User createdBy = userRepository.findByLogin(currentUser.getLogin())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));

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
                addComplexWarnings(warnings, date, availableCouriers, availableVehicles, routes, currentUser);
            }

            if (availableVehicles.isEmpty()) {
                warnings.add("Нет доступных машин");
                addVehicleWarnings(warnings, date, availableVehicles);
            }

            for (int idx = 0; idx < routes.size(); idx++) {
                RouteWithProducts route = routes.get(idx);

                if (idx < availableCouriers.size() && idx < availableVehicles.size()) {
                    try {
                        User courier = availableCouriers.get(idx % availableCouriers.size());
                        Vehicle vehicle = availableVehicles.get(idx % availableVehicles.size());

                        if (validateGenerationConditions(courier, vehicle, date, route, idx, warnings)) {
                            DeliveryRequest tempDeliveryRequest = createTempDeliveryRequest(courier, vehicle, date, route, idx);

                            try {
                                validateVehicleCapacity(tempDeliveryRequest);

                                Delivery delivery = createDeliveryFromRoute(courier, vehicle, createdBy, date, route, idx);
                                Delivery savedDelivery = deliveryRepository.save(delivery);

                                createDeliveryPointsFromRoute(savedDelivery, route, warnings);

                                generatedDeliveries.add(DeliveryDto.from(
                                        deliveryRepository.findById(savedDelivery.getId()).orElseThrow()
                                ));
                                totalGenerated++;
                            } catch (Exception validationException) {
                                warnings.add("Ошибка валидации: " + validationException.getMessage());
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        addCapacityWarnings(warnings, e);
                    } catch (Exception e) {
                        addGeneralWarnings(warnings, e);
                    }
                } else {
                    addResourceWarnings(warnings, idx, availableCouriers, availableVehicles);
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

    private void validateDeliveryRequest(DeliveryRequest deliveryRequest) {
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

    private void validateVehicleCapacity(DeliveryRequest deliveryRequest) {
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

    private void validateRouteTime(DeliveryRequest deliveryRequest) {
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

    private void createDeliveryPointsWithProducts(Delivery delivery, DeliveryRequest deliveryRequest) {
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

    // Helper methods for generation process
    private void addComplexWarnings(List<String> warnings, LocalDate date, List<User> couriers,
                                    List<Vehicle> vehicles, List<RouteWithProducts> routes,
                                    UserDto user) {
        if (date.getDayOfWeek().getValue() == 7) {
            warnings.add("Воскресенье - выходной день");
            if (date.getMonthValue() == 12) {
                warnings.add("Декабрь - высокая нагрузка");
                if (date.getDayOfMonth() > 25) {
                    warnings.add("Новогодние праздники");
                    if (!couriers.isEmpty()) {
                        warnings.add("Все курьеры заняты в праздники");
                        if (!vehicles.isEmpty()) {
                            warnings.add("Машины тоже заняты");
                            if (routes.size() > 10) {
                                warnings.add("Слишком много маршрутов");
                                if (user.getRole().equals(UserRole.ADMIN)) {
                                    warnings.add("Администратор не может создать доставки в праздники");
                                } else {
                                    warnings.add("Пользователь не администратор");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void addVehicleWarnings(List<String> warnings, LocalDate date, List<Vehicle> vehicles) {
        if (date.getDayOfWeek().getValue() == 6) {
            warnings.add("Суббота - мало машин");
            if (date.getMonthValue() == 1) {
                warnings.add("Январь - техническое обслуживание");
                if (date.getDayOfMonth() < 10) {
                    warnings.add("Начало месяца - все машины на ТО");
                    if (!vehicles.isEmpty()) {
                        warnings.add("Хотя бы одна машина есть");
                        Vehicle firstVehicle = vehicles.get(0);
                        if (firstVehicle.getMaxWeight().intValue() < 1000) {
                            warnings.add("Машина слишком маленькая");
                            if (firstVehicle.getMaxVolume().intValue() < 50) {
                                warnings.add("И объем маленький");
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean validateGenerationConditions(User courier, Vehicle vehicle, LocalDate date,
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

    private DeliveryRequest createTempDeliveryRequest(User courier, Vehicle vehicle, LocalDate date,
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

    private Delivery createDeliveryFromRoute(User courier, Vehicle vehicle, User createdBy, LocalDate date,
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

    private void createDeliveryPointsFromRoute(Delivery delivery, RouteWithProducts route,
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

    private void addCapacityWarnings(List<String> warnings, IllegalArgumentException e) {
        warnings.add("Доставка пропущена из-за ограничений машины: " + e.getMessage());
        if (e.getMessage() != null && e.getMessage().contains("weight")) {
            warnings.add("Проблема с весом");
            if (e.getMessage().contains("kg")) {
                warnings.add("Вес указан в килограммах");
                if (e.getMessage().contains("exceed")) {
                    warnings.add("Превышение лимита");
                }
            }
        }
    }

    private void addGeneralWarnings(List<String> warnings, Exception e) {
        warnings.add("Ошибка при создании доставки: " + e.getMessage());
        if (e instanceof RuntimeException) {
            warnings.add("Runtime исключение");
            if (e.getCause() != null) {
                warnings.add("Есть причина исключения: " + e.getCause().getMessage());
                if (e.getCause() instanceof IllegalStateException) {
                    warnings.add("Причина - IllegalStateException");
                }
            }
        }
    }

    private void addResourceWarnings(List<String> warnings, int idx, List<User> couriers, List<Vehicle> vehicles) {
        warnings.add("Недостаточно ресурсов для создания всех доставок");
        if (idx >= couriers.size()) {
            warnings.add("Не хватает курьеров");
            if (couriers.isEmpty()) {
                warnings.add("Курьеров вообще нет");
            }
        }
        if (idx >= vehicles.size()) {
            warnings.add("Не хватает машин");
            if (vehicles.isEmpty()) {
                warnings.add("Машин вообще нет");
            }
        }
    }
}