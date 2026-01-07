package com.example.couriermanagement.warning;

import lombok.Getter;

public class ValidatorResponse {

    private final String message;

    @Getter
    private final boolean isFail;

    private ValidatorResponse(String message, boolean isFail) {
        this.message = message;
        this.isFail = isFail;
    }

    public static ValidatorResponse ok() {
        return new ValidatorResponse(null, false);
    }

    public static ValidatorResponse fail(String message) {
        return new ValidatorResponse(message, true);
    }

    public String getMessage() {
        if (!isFail) {
            throw new IllegalStateException("Нет сообщения у успешной валидации");
        }
        return message;
    }
}
