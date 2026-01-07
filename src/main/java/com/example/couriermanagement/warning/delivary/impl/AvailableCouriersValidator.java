package com.example.couriermanagement.warning.delivary.impl;

import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.delivary.EmptyCouriersValidator;
import com.example.couriermanagement.warning.delivary.data.EmptyCouriersValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.example.couriermanagement.warning.ValidatorResponse.fail;
import static com.example.couriermanagement.warning.ValidatorResponse.ok;

@Component
@Order(4)
public class AvailableCouriersValidator implements EmptyCouriersValidator {

    @Override
    public ValidatorResponse validate(EmptyCouriersValidatorContext context) {
        List<User> couriers = context.getCouriers();
        return couriers != null && couriers.isEmpty()
                ? fail("Все курьеры заняты в праздники")
                : ok();
    }
}