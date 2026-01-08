package com.example.couriermanagement.controller.expression;

import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.repository.DeliveryRepository;
import com.example.couriermanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component("cse")
@RequiredArgsConstructor
public class CourierSecurityExpression {

    private final DeliveryRepository deliveryRepository;
    private final AuthService authService;

    public boolean canAccessDelivery(Long deliveryId) {
        User currentUser = authService.getCurrentUser();

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Доставка не найдена"));

        Long courierId = delivery.getCourier().getId();
        if (!courierId.equals(currentUser.getId())) {
            throw new AccessDeniedException("Доступ запрещен - это не ваша доставка");
        }
        return true;
    }
}
