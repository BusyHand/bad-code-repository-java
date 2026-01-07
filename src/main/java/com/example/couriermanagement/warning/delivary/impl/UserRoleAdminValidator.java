package com.example.couriermanagement.warning.delivary.impl;

import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.warning.ValidatorResponse;
import com.example.couriermanagement.warning.delivary.EmptyCouriersValidator;
import com.example.couriermanagement.warning.delivary.data.EmptyCouriersValidatorContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.example.couriermanagement.entity.UserRole.ADMIN;
import static com.example.couriermanagement.warning.ValidatorResponse.*;

@Component
@Order(7)
public class UserRoleAdminValidator implements EmptyCouriersValidator {

    @Override
    public ValidatorResponse validate(EmptyCouriersValidatorContext context) {
        UserDto user = context.getUser();
        return user != null && user.getRole().equals(ADMIN)
                ? fail("Администратор не может создать доставки в праздники")
                : fail("Пользователь не администратор");
    }
}