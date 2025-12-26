package com.example.couriermanagement.service;

import com.example.couriermanagement.dto.ProductDto;
import com.example.couriermanagement.dto.request.ProductRequest;

import java.util.List;

//todo Теоретическая общность
public interface ProductService {
    List<ProductDto> getAllProducts();
    ProductDto createProduct(ProductRequest productRequest);
    ProductDto updateProduct(Long id, ProductRequest productRequest);
    void deleteProduct(Long id);
}