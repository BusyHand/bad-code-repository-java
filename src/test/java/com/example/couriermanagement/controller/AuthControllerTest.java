package com.example.couriermanagement.controller;

import com.example.couriermanagement.BaseIntegrationTest;
import com.example.couriermanagement.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthControllerTest extends BaseIntegrationTest {

    @Test
    public void loginWithValidCredentialsShouldReturnTokenAndUserInfo() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .login("admin")
                .password("admin123")
                .build();

        expectSuccess(postJson("/auth/login", loginRequest))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.id").value(adminUser.getId()))
                .andExpect(jsonPath("$.user.login").value("admin"))
                .andExpect(jsonPath("$.user.name").value("Системный администратор"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    public void loginWithInvalidLoginShouldReturn400() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .login("nonexistent")
                .password("password")
                .build();

        expectBadRequest(postJson("/auth/login", loginRequest));
    }

    @Test
    public void loginWithInvalidPasswordShouldReturn400() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .login("admin")
                .password("wrongpassword")
                .build();

        expectBadRequest(postJson("/auth/login", loginRequest));
    }

    @Test
    public void loginWithEmptyLoginShouldReturn400() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .login("")
                .password("admin123")
                .build();

        expectBadRequest(postJson("/auth/login", loginRequest));
    }

    @Test
    public void loginWithEmptyPasswordShouldReturn400() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .login("admin")
                .password("")
                .build();

        expectBadRequest(postJson("/auth/login", loginRequest));
    }

    @Test
    public void loginWithManagerCredentialsShouldReturnManagerToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .login("manager")
                .password("password")
                .build();

        expectSuccess(postJson("/auth/login", loginRequest))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.role").value("MANAGER"));
    }

    @Test
    public void loginWithCourierCredentialsShouldReturnCourierToken() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .login("courier")
                .password("password")
                .build();

        expectSuccess(postJson("/auth/login", loginRequest))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.role").value("COURIER"));
    }
}