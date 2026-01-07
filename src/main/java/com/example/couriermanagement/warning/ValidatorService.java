package com.example.couriermanagement.warning;

import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.RouteWithProducts;
import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.entity.Vehicle;
import com.example.couriermanagement.warning.delivary.EmptyCouriersValidator;
import com.example.couriermanagement.warning.delivary.data.EmptyCouriersValidatorContext;
import com.example.couriermanagement.warning.vehicle.EmptyVehiclesValidator;
import com.example.couriermanagement.warning.vehicle.data.EmptyVehiclesValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidatorService {

    private final List<EmptyCouriersValidator> emptyCouriersValidators;
    private final List<EmptyVehiclesValidator> emptyVehiclesValidators;

    public void validateEmptyCouriers(
            List<String> warnings, LocalDate date, List<User> couriers,
            List<Vehicle> vehicles, List<RouteWithProducts> routes, UserDto user) {
        EmptyCouriersValidatorContext validationContext = EmptyCouriersValidatorContext.builder()
                .couriers(couriers)
                .vehicles(vehicles)
                .routes(routes)
                .user(user)
                .date(date)
                .build();
        validate(warnings, emptyCouriersValidators, validationContext);
    }

    public void validateEmptyVehicles(List<String> warnings, LocalDate date, List<Vehicle> vehicles) {
        EmptyVehiclesValidatorContext validationContext = EmptyVehiclesValidatorContext.builder()
                .vehicles(vehicles)
                .date(date)
                .build();
        validate(warnings, emptyVehiclesValidators, validationContext);
    }

    private <T extends ValidationContext> void validate(
            List<String> warnings,
            List<? extends WarningValidator<T>> validators,
            T context
    ) {
        validators.stream()
                .map(v -> v.validate(context))
                .takeWhile(ValidatorResponse::isFail)
                .map(ValidatorResponse::getMessage)
                .forEachOrdered(warnings::add);
    }
}