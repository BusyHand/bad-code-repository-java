package com.example.couriermanagement.controller;

import com.example.couriermanagement.BaseIntegrationTest;
import com.example.couriermanagement.dto.request.UserRequest;
import com.example.couriermanagement.dto.request.UserUpdateRequest;
import com.example.couriermanagement.entity.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends BaseIntegrationTest {

    @Test
    public void getAllUsersAsAdminShouldSucceed() throws Exception {
        expectSuccess(getWithAuth("/users", adminToken))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3)); // admin, manager, courier
    }

    @Test
    public void getAllUsersAsManagerShouldReturn403() throws Exception {
        expectForbidden(getWithAuth("/users", managerToken));
    }

    @Test
    public void getAllUsersAsCourierShouldReturn403() throws Exception {
        expectForbidden(getWithAuth("/users", courierToken));
    }

    @Test
    public void getAllUsersWithoutAuthShouldReturn403() throws Exception {
        expectForbidden(mockMvc.perform(get("/users")));
    }

    @Test
    public void getUsersFilteredByRoleShouldReturnFilteredResults() throws Exception {
        expectSuccess(getWithAuth("/users?role=COURIER", adminToken))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].role").value("COURIER"));
    }

    @Test
    public void createUserAsAdminShouldSucceed() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .login("newcourier")
                .password("password123")
                .name("Новый Курьер")
                .role(UserRole.COURIER)
                .build();

        expectSuccess(postJson("/users", userRequest, adminToken))
                .andExpect(jsonPath("$.login").value("newcourier"))
                .andExpect(jsonPath("$.name").value("Новый Курьер"))
                .andExpect(jsonPath("$.role").value("COURIER"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void createUserAsManagerShouldReturn403() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .login("newcourier")
                .password("password123")
                .name("Новый Курьер")
                .role(UserRole.COURIER)
                .build();

        expectForbidden(postJson("/users", userRequest, managerToken));
    }

    @Test
    public void createUserWithDuplicateLoginShouldReturn400() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .login("admin") // Already exists
                .password("password123")
                .name("Другой Админ")
                .role(UserRole.ADMIN)
                .build();

        expectBadRequest(postJson("/users", userRequest, adminToken));
    }

    @Test
    public void createUserWithInvalidDataShouldReturn400() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .login("") // Empty login
                .password("") // Empty password
                .name("") // Empty name
                .role(UserRole.COURIER)
                .build();

        expectBadRequest(postJson("/users", userRequest, adminToken));
    }

    @Test
    public void createManagerUserShouldSucceed() throws Exception {
        UserRequest userRequest = UserRequest.builder()
                .login("newmanager")
                .password("password123")
                .name("Новый Менеджер")
                .role(UserRole.MANAGER)
                .build();

        expectSuccess(postJson("/users", userRequest, adminToken))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    public void updateUserAsAdminShouldSucceed() throws Exception {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .name("Обновленное Имя")
                .login("updatedcourier")
                .role(UserRole.MANAGER)
                .password("newpassword")
                .build();

        expectSuccess(putJson("/users/" + courierUser.getId(), updateRequest, adminToken))
                .andExpect(jsonPath("$.name").value("Обновленное Имя"))
                .andExpect(jsonPath("$.login").value("updatedcourier"))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    public void updateUserAsManagerShouldReturn403() throws Exception {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .name("Обновленное Имя")
                .password("Обновленное Имя")
                .login("Обновленное Имя")
                .role(UserRole.COURIER)
                .build();

        expectForbidden(putJson("/users/" + courierUser.getId(), updateRequest, managerToken));
    }

    @Test
    public void updateUserWithDuplicateLoginShouldReturn400() throws Exception {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .login("admin") // Already exists
                .name("Обновленное Имя")
                .password("Обновленное Имя")
                .role(UserRole.COURIER)
                .build();

        expectBadRequest(putJson("/users/" + courierUser.getId(), updateRequest, adminToken));
    }

    @Test
    public void updateNonExistentUserShouldReturn404() throws Exception {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .name("Обновленное Имя")
                .build();

        expectBadRequest(putJson("/users/999", updateRequest, adminToken)); // Service throws IllegalArgumentException, which becomes 400
    }

    @Test
    public void updateUserPartialDataShouldSucceed() throws Exception {
        UserUpdateRequest updateRequest = UserUpdateRequest.builder()
                .name("Только Новое Имя")
                .login("Только Новое Имя")
                .password("Только Новое Имя")
                .role(UserRole.COURIER)
                .build();

        expectSuccess(putJson("/users/" + courierUser.getId(), updateRequest, adminToken))
                .andExpect(jsonPath("$.name").value("Только Новое Имя"))
                .andExpect(jsonPath("$.login").value("Только Новое Имя"))
                .andExpect(jsonPath("$.role").value("COURIER"));
    }

    @Test
    public void deleteUserAsAdminShouldSucceed() throws Exception {
        // Create a user to delete
        UserRequest userRequest = UserRequest.builder()
                .login("todelete")
                .password("password123")
                .name("Для Удаления")
                .role(UserRole.COURIER)
                .build();

        MvcResult createResponse = expectSuccess(postJson("/users", userRequest, adminToken))
                .andReturn();

        JsonNode responseJson = objectMapper.readTree(createResponse.getResponse().getContentAsString());
        Long createdUserId = responseJson.get("id").asLong();

        deleteWithAuth("/users/" + createdUserId, adminToken)
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteUserAsManagerShouldReturn403() throws Exception {
        expectForbidden(deleteWithAuth("/users/" + courierUser.getId(), managerToken));
    }

    @Test
    public void deleteNonExistentUserShouldReturn404() throws Exception {
        expectBadRequest(deleteWithAuth("/users/999", adminToken)); // Service throws IllegalArgumentException, which becomes 400
    }
}