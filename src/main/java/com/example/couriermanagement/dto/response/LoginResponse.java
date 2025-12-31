package com.example.couriermanagement.dto.response;

import com.example.couriermanagement.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@Schema(description = "Ответ при успешной авторизации")
public class LoginResponse {
    @Schema(description = "JWT токен", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Информация о пользователе")
    private UserDto user;

    public LoginResponse() {
    }

    public LoginResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginResponse that = (LoginResponse) o;
        return Objects.equals(token, that.token) &&
                Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, user);
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "user=" + user +
                '}';
    }
}