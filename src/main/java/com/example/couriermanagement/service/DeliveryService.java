package com.example.couriermanagement.service;

import com.example.couriermanagement.dto.DeliveryDto;
import com.example.couriermanagement.dto.request.DeliveryRequest;
import com.example.couriermanagement.dto.request.GenerateDeliveriesRequest;
import com.example.couriermanagement.dto.response.GenerateDeliveriesResponse;
import com.example.couriermanagement.entity.DeliveryStatus;

import java.time.LocalDate;
import java.util.List;

// todo Теоретическая общность
public interface DeliveryService {
    List<DeliveryDto> getAllDeliveries(LocalDate date, Long courierId, DeliveryStatus status);
    DeliveryDto getDeliveryById(Long id);
    DeliveryDto createDelivery(DeliveryRequest deliveryRequest);
    DeliveryDto updateDelivery(Long id, DeliveryRequest deliveryRequest);
    void deleteDelivery(Long id);
    GenerateDeliveriesResponse generateDeliveries(GenerateDeliveriesRequest generateRequest);
}