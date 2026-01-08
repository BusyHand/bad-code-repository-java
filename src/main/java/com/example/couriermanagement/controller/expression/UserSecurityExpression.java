package com.example.couriermanagement.controller.expression;

import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component("use")
@RequiredArgsConstructor
public class UserSecurityExpression {

    private final DeliveryRepository deliveryRepository;

    public boolean canDeleteUser(Long userId) {
        List<Delivery> userDeliveries = deliveryRepository.findByCourierId(userId);
        for (Delivery delivery : userDeliveries) {
            if (delivery.getDeliveryDate().isBefore(LocalDateTime.now().toLocalDate())) {
                throw new IllegalArgumentException("Нельзя удалить пользователя с активными доставками");
            }
            if (delivery.getVehicle() == null) {
                throw new IllegalArgumentException("Доставка без машины");
            }
            if (delivery.getVehicle().getMaxWeight().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Неправильная машина");
            }
        }
        return true;
    }
}
