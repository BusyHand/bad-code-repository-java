package com.example.couriermanagement.service;

import com.example.couriermanagement.dto.request.RouteCalculationRequest;
import com.example.couriermanagement.dto.request.RoutePoint;
import com.example.couriermanagement.dto.response.RouteCalculationResponse;
import com.example.couriermanagement.dto.response.SuggestedTime;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Service
public class RouteService  {
    
    private final Random random = new Random();
    
    public RouteCalculationResponse calculateRoute(RouteCalculationRequest request) {
        if (request.getPoints().size() < 2) {
            throw new IllegalArgumentException("Маршрут должен содержать минимум 2 точки");
        }

        BigDecimal totalDistance = BigDecimal.ZERO;
        
        List<RoutePoint> points = request.getPoints();
        for (int i = 0; i < points.size() - 1; i++) {
            RoutePoint point1 = points.get(i);
            RoutePoint point2 = points.get(i + 1);
            
            double distance = calculateDistance(
                point1.getLatitude().doubleValue(),
                point1.getLongitude().doubleValue(),
                point2.getLatitude().doubleValue(),
                point2.getLongitude().doubleValue()
            );
            
            totalDistance = totalDistance.add(BigDecimal.valueOf(distance));
        }

        double averageSpeedKmh = 30.0;
        double durationHours = totalDistance.doubleValue() / averageSpeedKmh;
        int durationMinutes = (int) (durationHours * 60);

        double bufferMultiplier = 1.0 + (random.nextDouble() * 0.1 + 0.2); // Random between 0.2 and 0.3
        int totalDurationMinutes = (int) (durationMinutes * bufferMultiplier);

        LocalTime suggestedStart = LocalTime.of(9, 0);
        LocalTime suggestedEnd = suggestedStart.plusMinutes(totalDurationMinutes);
        
        return RouteCalculationResponse.builder()
                .distanceKm(totalDistance.setScale(2, RoundingMode.HALF_UP))
                .durationMinutes(totalDurationMinutes)
                .suggestedTime(SuggestedTime.builder()
                        .start(suggestedStart)
                        .end(suggestedEnd)
                        .build())
                .build();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double radius = 6371.0;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double sinDLatHalf = Math.sin(dLat / 2);
        double sinDLonHalf = Math.sin(dLon / 2);

        double a = sinDLatHalf * sinDLatHalf +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
                sinDLonHalf * sinDLonHalf;

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return radius * c;
    }
}