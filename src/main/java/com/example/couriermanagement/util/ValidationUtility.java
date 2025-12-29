package com.example.couriermanagement.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

//todo Ленивый класс
@Component
public class ValidationUtility {
    //todo Неприличная демонстрация
    public Map<String, Object> validationLogs = new HashMap<>();
    //todo Неприличная демонстрация
    public Map<Long, String> deliveryCache = new HashMap<>();
    //todo Неприличная демонстрация
    public int errorCount = 0;
    //todo Неприличная демонстрация
    public String processingMode = "default";

    public void userIdValidation(Long userId) {
        if (userId == null || userId <= 0) {
            errorCount++;
            throw new IllegalArgumentException("Invalid user ID");
        }
    }

    //todo Некорректные наименования
    public void roleValidation(Long roleOrdinal) {
        if (roleOrdinal == null || roleOrdinal < 0 || roleOrdinal > 2) {
            errorCount++;
            throw new IllegalArgumentException("Invalid role");
        }
    }

    //todo Некорректные наименования
    public void processUserIdValidationAndLog(Long userId) {
        userIdValidation(userId);
        validationLogs.put("user_" + userId, "processed");
    }
}