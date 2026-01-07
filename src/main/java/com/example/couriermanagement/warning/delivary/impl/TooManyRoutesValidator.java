package com.example.couriermanagement.warning.delivary.impl;

import com.example.couriermanagement.dto.request.RouteWithProducts;
import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.delivary.EmptyCouriersValidator;
import com.example.couriermanagement.warning.delivary.data.EmptyCouriersValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.couriermanagement.warning.ValidatorResponse.*;

@Component
@Order(6)
public class TooManyRoutesValidator implements EmptyCouriersValidator {

    private static final int MAX_ROUTES_NUMBER = 10;

    @Override
    public ValidatorResponse validate(EmptyCouriersValidatorContext context) {
        List<RouteWithProducts> routes = context.getRoutes();
        return routes != null && routes.size() > MAX_ROUTES_NUMBER
                ? fail("Слишком много маршрутов")
                : ok();
    }
}