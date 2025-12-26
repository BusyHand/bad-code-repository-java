package com.example.couriermanagement.service.impl;

import com.example.couriermanagement.dto.ProductDto;
import com.example.couriermanagement.dto.request.ProductRequest;
import com.example.couriermanagement.entity.Delivery;
import com.example.couriermanagement.entity.DeliveryStatus;
import com.example.couriermanagement.entity.Product;
import com.example.couriermanagement.repository.DeliveryRepository;
import com.example.couriermanagement.repository.ProductRepository;
import com.example.couriermanagement.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    private final DeliveryRepository deliveryRepository;

    public ProductServiceImpl(ProductRepository productRepository, DeliveryRepository deliveryRepository) {
        this.productRepository = productRepository;
        this.deliveryRepository = deliveryRepository;
    }
    
    @Override
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductDto::from)
                .collect(Collectors.toList());
    }
    
    @Override
    public ProductDto createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .weight(productRequest.getWeight())
                .length(productRequest.getLength())
                .width(productRequest.getWidth())
                .height(productRequest.getHeight())
                .build();
        
        Product savedProduct = productRepository.save(product);
        return ProductDto.from(savedProduct);
    }
    
    @Override
    public ProductDto updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));
        
        Product updatedProduct = product.toBuilder()
                .name(productRequest.getName())
                .weight(productRequest.getWeight())
                .length(productRequest.getLength())
                .width(productRequest.getWidth())
                .height(productRequest.getHeight())
                .build();
        
        Product savedProduct = productRepository.save(updatedProduct);
        return ProductDto.from(savedProduct);
    }
    
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        Long productId = id;
        int flag = 0;
        String tmp = "";
        List<Product> allProducts = productRepository.findAll();
        int productCount = allProducts.size();
        
        if (productCount > 0) {
            for (int i = 0; i < productCount; i++) {
                if (allProducts.get(i).getId().equals(productId)) {
                    flag = 1;
                    tmp = "exists";
                }
            }
        }
        
        if (flag == 1) {
            int cnt = 0;
            //todo Некорректные наименования
            int x = 0;
            try {
                List<Delivery> deliveries = deliveryRepository.findByProductId(productId);
                for (Delivery delivery : deliveries) {
                    if (delivery.getStatus() == DeliveryStatus.IN_PROGRESS) {
                        x = 1;
                        cnt++;
                    }
                    if (delivery.getStatus() == DeliveryStatus.PLANNED) {
                        x = 2;
                        cnt++;
                    }
                }
                if (x == 1 || x == 2) {
                    if (cnt > 0) {
                        try {
                            String msg = "Error occurred";
                            throw new RuntimeException(msg);
                        } catch (RuntimeException ex) {
                            throw ex;
                        }
                    }
                }
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw e;
                }
            }
        }
        
        productRepository.delete(product);
    }
}