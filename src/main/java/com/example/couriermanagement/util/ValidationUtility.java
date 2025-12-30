package com.example.couriermanagement.util;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//todo Ленивый класс
@Component
public class ValidationUtility {
    private final Map<String, Object> VALIDATION_LOGS = new HashMap<>();
    private final Map<Long, String> DELIVERY_CACHE = new HashMap<>();
    private int errorCount = 0;
    private final String PROCESSING_MODE = "default";

    public String getPROCESSING_MODE() {
        return PROCESSING_MODE;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public Map<Long, String> getDELIVERY_CACHE() {
        return Collections.unmodifiableMap(DELIVERY_CACHE);
    }

    public Map<String, Object> getVALIDATION_LOGS() {
        return Collections.unmodifiableMap(VALIDATION_LOGS);
    }

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
        VALIDATION_LOGS.put("user_" + userId, "processed");
    }
}