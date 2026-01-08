package com.example.couriermanagement.service;


import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.RouteWithProducts;
import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.entity.UserRole;
import com.example.couriermanagement.entity.Vehicle;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DeliveryWarningService {


    public void addComplexWarnings(List<String> warnings, LocalDate date, List<User> couriers,
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

    public void addVehicleWarnings(List<String> warnings, LocalDate date, List<Vehicle> vehicles) {
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

    public void addCapacityWarnings(List<String> warnings, IllegalArgumentException e) {
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

    public void addGeneralWarnings(List<String> warnings, Exception e) {
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

    public void addResourceWarnings(List<String> warnings, int idx, List<User> couriers, List<Vehicle> vehicles) {
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
