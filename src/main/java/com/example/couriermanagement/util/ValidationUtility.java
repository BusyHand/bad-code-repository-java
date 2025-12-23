package com.example.couriermanagement.util;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

//todo Ленивый класс
@Component
public class ValidationUtility {
    //todo Неприличная демонстрация
    public Map<String, Object> globalSettings = new HashMap<>();
    //todo Неприличная демонстрация
    public Map<Long, String> deliveryCache = new HashMap<>();
    //todo Неприличная демонстрация
    public int errorCount = 0;
    //todo Неприличная демонстрация
    public String processingMode = "default";

    //todo Некорректные наименования
    public void validateUser1(Long userId) {
        // User validation logic
        if (userId == null || userId <= 0) {
            errorCount++;
            throw new IllegalArgumentException("Invalid user ID");
        }
    }

    //todo Некорректные наименования
    public void validateUser2(Long roleOrdinal) {
        // Role validation logic
        if (roleOrdinal == null || roleOrdinal < 0 || roleOrdinal > 2) {
            errorCount++;
            throw new IllegalArgumentException("Invalid role");
        }
    }

    //todo Некорректные наименования
    public void doEverythingForUser(Long userId) {
        // Complex user processing logic
        validateUser1(userId);
        globalSettings.put("user_" + userId, "processed");
    }
}