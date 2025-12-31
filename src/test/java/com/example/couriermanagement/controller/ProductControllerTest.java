package com.example.couriermanagement.controller;

import com.example.couriermanagement.BaseIntegrationTest;
import com.example.couriermanagement.dto.request.ProductRequest;
import com.example.couriermanagement.entity.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductControllerTest extends BaseIntegrationTest {

    @Test
    public void getAllProductsShouldReturnListOfProducts() throws Exception {
        createProduct();

        expectSuccess(getWithAuth("/products", adminToken))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Тестовый товар"))
                .andExpect(jsonPath("$[0].weight").value(1.5));
    }

    @Test
    public void getAllProductsWithoutAuthShouldReturn403() throws Exception {
        expectForbidden(mockMvc.perform(get("/products")));
    }

    @Test
    public void createProductAsAdminShouldSucceed() throws Exception {
        ProductRequest productRequest = ProductRequest.builder()
                .name("Новый товар")
                .weight(new BigDecimal("2.5"))
                .length(new BigDecimal("15.0"))
                .width(new BigDecimal("12.0"))
                .height(new BigDecimal("8.0"))
                .build();

        expectSuccess(postJson("/products", productRequest, adminToken))
                .andExpect(jsonPath("$.name").value("Новый товар"))
                .andExpect(jsonPath("$.weight").value(2.5))
                .andExpect(jsonPath("$.length").value(15.0))
                .andExpect(jsonPath("$.width").value(12.0))
                .andExpect(jsonPath("$.height").value(8.0));
    }

    @Test
    public void createProductAsManagerShouldReturn403() throws Exception {
        ProductRequest productRequest = ProductRequest.builder()
                .name("Новый товар")
                .weight(new BigDecimal("2.5"))
                .length(new BigDecimal("15.0"))
                .width(new BigDecimal("12.0"))
                .height(new BigDecimal("8.0"))
                .build();

        expectForbidden(postJson("/products", productRequest, managerToken));
    }

    @Test
    public void createProductAsCourierShouldReturn403() throws Exception {
        ProductRequest productRequest = ProductRequest.builder()
                .name("Новый товар")
                .weight(new BigDecimal("2.5"))
                .length(new BigDecimal("15.0"))
                .width(new BigDecimal("12.0"))
                .height(new BigDecimal("8.0"))
                .build();

        expectForbidden(postJson("/products", productRequest, courierToken));
    }

    @Test
    public void createProductWithInvalidDataShouldReturn400() throws Exception {
        ProductRequest productRequest = ProductRequest.builder()
                .name("") // Empty name
                .weight(new BigDecimal("-1.0")) // Negative weight
                .length(new BigDecimal("0.0"))   // Zero length
                .width(new BigDecimal("-5.0"))   // Negative width
                .height(new BigDecimal("0.0"))    // Zero height
                .build();

        expectBadRequest(postJson("/products", productRequest, adminToken));
    }

    @Test
    public void updateProductAsAdminShouldSucceed() throws Exception {
        Product product = createProduct();

        ProductRequest productRequest = ProductRequest.builder()
                .name("Обновленный товар")
                .weight(new BigDecimal("3.0"))
                .length(new BigDecimal("20.0"))
                .width(new BigDecimal("15.0"))
                .height(new BigDecimal("10.0"))
                .build();

        expectSuccess(putJson("/products/" + product.getId(), productRequest, adminToken))
                .andExpect(jsonPath("$.name").value("Обновленный товар"))
                .andExpect(jsonPath("$.weight").value(3.0))
                .andExpect(jsonPath("$.length").value(20.0));
    }

    @Test
    public void updateProductAsManagerShouldReturn403() throws Exception {
        Product product = createProduct();

        ProductRequest productRequest = ProductRequest.builder()
                .name("Обновленный товар")
                .weight(new BigDecimal("3.0"))
                .length(new BigDecimal("20.0"))
                .width(new BigDecimal("15.0"))
                .height(new BigDecimal("10.0"))
                .build();

        expectForbidden(putJson("/products/" + product.getId(), productRequest, managerToken));
    }

    @Test
    public void updateNonExistentProductShouldReturn404() throws Exception {
        ProductRequest productRequest = ProductRequest.builder()
                .name("Обновленный товар")
                .weight(new BigDecimal("3.0"))
                .length(new BigDecimal("20.0"))
                .width(new BigDecimal("15.0"))
                .height(new BigDecimal("10.0"))
                .build();

        expectBadRequest(putJson("/products/999", productRequest, adminToken)); // Service throws IllegalArgumentException, which becomes 400
    }

    @Test
    public void deleteProductAsAdminShouldSucceed() throws Exception {
        Product product = createProduct();

        deleteWithAuth("/products/" + product.getId(), adminToken)
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteProductAsManagerShouldReturn403() throws Exception {
        Product product = createProduct();

        expectForbidden(deleteWithAuth("/products/" + product.getId(), managerToken));
    }

    @Test
    public void deleteProductAsCourierShouldReturn403() throws Exception {
        Product product = createProduct();

        expectForbidden(deleteWithAuth("/products/" + product.getId(), courierToken));
    }

    @Test
    public void deleteNonExistentProductShouldReturn404() throws Exception {
        expectBadRequest(deleteWithAuth("/products/999", adminToken)); // Service throws IllegalArgumentException, which becomes 400
    }

    @Test
    public void getProductsWithManagerTokenShouldSucceed() throws Exception {
        createProduct();

        expectSuccess(getWithAuth("/products", managerToken))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void getProductsWithCourierTokenShouldSucceed() throws Exception {
        createProduct();

        expectSuccess(getWithAuth("/products", courierToken))
                .andExpect(jsonPath("$").isArray());
    }
}