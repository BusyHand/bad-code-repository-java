package com.example.couriermanagement.warning.vehicle.data;

import com.example.couriermanagement.dto.UserDto;
import com.example.couriermanagement.dto.request.RouteWithProducts;
import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.entity.Vehicle;
import com.example.couriermanagement.warning.ValidationContext;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class EmptyVehiclesValidatorContext extends ValidationContext {
    private LocalDate date;
    private List<User> couriers;
    private List<Vehicle> vehicles;
    private List<RouteWithProducts> routes;
    private UserDto user;
}