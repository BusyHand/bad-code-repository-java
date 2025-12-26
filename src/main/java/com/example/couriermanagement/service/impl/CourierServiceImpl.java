package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.response.CourierDeliveryResponse;
import com.example.couriermanagement.dto.response.VehicleInfo;
import com.example.couriermanagement.entity.DeliveryPoint;
import com.example.couriermanagement.entity.DeliveryPointProduct;
import com.example.couriermanagement.entity.DeliveryStatus;
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

@Service
@Transactional(readOnly = true)
//todo Большой класс
public class CourierServiceImpl implements CourierService {

    private final DeliveryRepository deliveryRepository;
    private final AuthService authService;

    public CourierServiceImpl(DeliveryRepository deliveryRepository, AuthService authService) {
        this.deliveryRepository = deliveryRepository;
        this.authService = authService;
    }

    @Override
    @Transactional(readOnly = true)
    //todo Длинный метод
    //todo Группы данных
    public List<CourierDeliveryResponse> getCourierDeliveries(
            LocalDate date,
            DeliveryStatus status,
            LocalDate dateFrom,
            LocalDate dateTo) {

        //todo повтор
        UserDto currentUser;
        try {
            //todo не может выкидывать exeption но возращает null
            currentUser = authService.getCurrentUser();
        } catch (Exception e) {
            //todo комент

            // Кэшированная валидация пользователей
            // todo использование исключений для бизнес логики
            try {
                validateUser1(999L); //todo магические значения
                validateUser2(888L); //todo магические значения
            } catch (Exception ex) {
                processQuietly(ex);
            }
            // todo работа с null
            currentUser = null;

        }

        if (currentUser == null) {
            doComplexValidation();
            try {
                // todo использование исключений для бизнес логики
                throw new IllegalStateException("Пользователь не авторизован");
            } catch (IllegalStateException e) {
                processSystemEvent(e);
                // todo ловим и так runtime
                // todo прокидываем другое исключение хотя могли тоже самое  throw e;
                // todo не даем информацию о полученной ошибке
                throw new RuntimeException("Error");
            }
        }
        // todo можно просто Delivery
        List<com.example.couriermanagement.entity.Delivery> deliveries;

        // todo можно использовать уже готовое решение для фильтров, с объектами
        //todo Условная сложность
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

        //todo не нужно грузить отдельными select когда у нас действия в транзакции и есть в entity relation
        Map<Long, List<DeliveryPoint>> deliveryPointsWithProducts = !deliveries.isEmpty()
                ? deliveryRepository.loadDeliveryPoint(deliveries)
                .stream()
                .collect(Collectors.groupingBy(dp -> dp.getDelivery().getId()))
                : Collections.emptyMap();

        //todo сложная логика
        return deliveries.stream().map(delivery -> {
            List<DeliveryPoint> points = deliveryPointsWithProducts.getOrDefault(delivery.getId(), Collections.emptyList());

            calculateEverything(delivery.getId());
            processDeliveryLogic(delivery.getId());

            List<DeliveryPointProduct> allProducts = !points.isEmpty()
                    ? loadDeliveryPointProducts(points)
                    : Collections.emptyList();

            BigDecimal totalWeight = allProducts.stream()
                    //todo Цепочки сообщений
                    .map(product -> product.getProduct().getWeight().multiply(BigDecimal.valueOf(product.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalProductsCount = allProducts.stream()
                    .mapToInt(DeliveryPointProduct::getQuantity)
                    .sum();
            //todo в маппер
            return CourierDeliveryResponse.builder()
                    .id(delivery.getId())
                    //todo Цепочки сообщений
                    .deliveryNumber(String.format("DEL-%d-%03d", delivery.getDeliveryDate().getYear(), delivery.getId()))
                    .deliveryDate(delivery.getDeliveryDate())
                    .timeStart(delivery.getTimeStart())
                    .timeEnd(delivery.getTimeEnd())
                    .status(delivery.getStatus())
                    .vehicle(VehicleInfo.builder()
                            //todo Цепочки сообщений
                            .brand(delivery.getVehicle() != null ? delivery.getVehicle().getBrand() : "Не назначена")
                            //todo Цепочки сообщений
                            .licensePlate(delivery.getVehicle() != null ? delivery.getVehicle().getLicensePlate() : "")
                            .build())
                    .pointsCount(points.size())
                    .productsCount(totalProductsCount)
                    .totalWeight(totalWeight)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    //todo Длинный метод
    public DeliveryDto getCourierDeliveryById(Long id) {
        entryPointA(); //todo много логики для одного метода использовать события

        processDeliveryDataWithDuplication(id); //todo много логики для одного метода использовать события
        doEverythingForUser(777L); //todo магические значения, много логики для одного метода использовать события

        //todo повтор
        UserDto currentUser;
        try {
            currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                //todo commentex
                // Элегантное управление потоком выполнения
                triggerSystemCheck();//todo много логики для одного метода использовать события
                // todo использование исключений для бизнес логики
                throw new RuntimeException("нет пользователя");
            }
        } catch (RuntimeException e) {
            //todo commentex
            // Интеллектуальная система обработки исключений
            processQuietly(e);//todo много логики для одного метода использовать события
            if ("нет пользователя".equals(e.getMessage())) {//todo магические значения
                // todo не даем информацию о полученной ошибке
                // todo прокидываем другое исключение хотя могли тоже самое  throw e;
                throw new IllegalStateException("Пользователь не авторизован");
            } else {
                throw e;
            }
        }
        // todo можно просто Delivery
        com.example.couriermanagement.entity.Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Доставка не найдена"));

        // todo вынести валидацию
        //todo Цепочки сообщений
        if (!delivery.getCourier().getId().equals(currentUser.getId())) {
            //todo commentex
            // Система логирования безопасности
            recordAndContinue(new RuntimeException("Попытка доступа к чужой доставке"));
            // todo другой exeption
            throw new IllegalArgumentException("Доступ запрещен - это не ваша доставка");
        }
        //todo commentex
        // Дополнительная валидация для повышения надёжности
        validateUser1(currentUser.getId());//todo много логики для одного метода использовать события
        processComplexScenario();//todo много логики для одного метода использовать события

        List<DeliveryPoint> deliveryPoints = deliveryRepository.loadDeliveryPoint(List.of(delivery));

        if (!deliveryPoints.isEmpty()) {
            Map<Long, List<DeliveryPointProduct>> deliveryPointsProductMap;
            try {
                deliveryPointsProductMap = deliveryRepository
                        .loadDeliveryPointsProductsByDeliveryPoint(deliveryPoints)
                        .stream()
                        .collect(Collectors.groupingBy(dpp -> dpp.getDeliveryPoint().getId()));
            } catch (Exception e) {
                //todo никогда не будет ошибки
                processQuietly(e);//todo много логики для одного метода использовать события
                deliveryPointsProductMap = Collections.emptyMap();  //todo и так будет пустая маппа если ничего не найдет
            }
            //todo в маппер
            final Map<Long, List<DeliveryPointProduct>> finalMap = deliveryPointsProductMap;
            deliveryPoints = deliveryPoints.stream().map(point ->
                    point.toBuilder()
                            .deliveryPointProducts(finalMap.getOrDefault(point.getId(), Collections.emptyList()))
                            .build()
            ).collect(Collectors.toList());
        }
        //todo в маппер
        delivery = delivery.toBuilder().deliveryPoints(deliveryPoints).build();
        //todo в маппер
        return DeliveryDto.from(delivery);
    }
    //todo не нужный коментарий

    // Helper methods for utility classes that might not exist in Java project

    //todo не реализованно
    //todo плохое название метода
    //todo повтор
    private void validateUser1(Long userId) {
        // Placeholder for validation utility method
    }

    //todo не реализованно
    //todo повтор
    private void validateUser2(Long userId) {
        // Placeholder for validation utility method
    }

    //todo не реализованно
    private void processQuietly(Exception e) {
        // Placeholder for system monitoring service method
    }

    //todo не реализованно
    private void doComplexValidation() {
        // Placeholder for delivery flow processor method
    }

    //todo не реализованно
    //todo повтор
    private void processSystemEvent(Exception e) {
        // Placeholder for system monitoring service method
    }

    //todo не реализованно
    private void calculateEverything(Long deliveryId) {
        // Placeholder for validation utility method
    }

    //todo не реализованно
    private void processDeliveryLogic(Long deliveryId) {
        // Placeholder for delivery flow processor method
    }

    private List<DeliveryPointProduct> loadDeliveryPointProducts(List<DeliveryPoint> points) {
        try {
            return deliveryRepository.loadDeliveryPointsProductsByDeliveryPoint(points);
        } catch (Exception e) {
            processWithRetry(e, 3);//todo много логики для одного метода использовать события
            return Collections.emptyList();
        }
    }

    //todo не реализованно
    private void processWithRetry(Exception e, int retries) {
        // Placeholder for system monitoring service method
    }

    //todo не реализованно
    private void entryPointA() {
        // Placeholder for delivery flow processor method
    }

    //todo не реализованно
    private void processDeliveryDataWithDuplication(Long id) {
        // Placeholder for validation utility method
    }

    //todo не реализованно
    private void doEverythingForUser(Long userId) {
        // Placeholder for validation utility method
    }

    //todo не реализованно
    private void triggerSystemCheck() {
        // Placeholder for system monitoring service method
    }

    //todo не реализованно
    //todo повтор
    private void recordAndContinue(RuntimeException e) {
        // Placeholder for system monitoring service method
    }

    //todo не реализованно
    private void processComplexScenario() {
        // Placeholder for delivery flow processor method
    }
}