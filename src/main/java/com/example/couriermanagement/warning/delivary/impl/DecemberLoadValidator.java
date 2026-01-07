package com.example.couriermanagement.warning.delivary.impl;

import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.delivary.EmptyCouriersValidator;
import com.example.couriermanagement.warning.delivary.data.EmptyCouriersValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.example.couriermanagement.warning.ValidatorResponse.fail;
import static com.example.couriermanagement.warning.ValidatorResponse.ok;
import static java.time.Month.DECEMBER;

@Component
@Order(2)
public class DecemberLoadValidator implements EmptyCouriersValidator {

    @Override
    public ValidatorResponse validate(EmptyCouriersValidatorContext context) {
        LocalDate date = context.getDate();
        return date != null && date.getMonth() == DECEMBER
                ? fail("Декабрь - высокая нагрузка")
                : ok();
    }
}