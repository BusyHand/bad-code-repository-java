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

    //todo под тест
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Товар не найден"));

        Long productId = id;
        boolean isFoundProuduct = false;
        String tmp = "";
        List<Product> allProducts = productRepository.findAll();
        int productCount = allProducts.size();

        if (productCount > 0) {
            for (int i = 0; i < productCount; i++) {
                if (allProducts.get(i).getId().equals(productId)) {
                    isFoundProuduct = true;
                    tmp = "exists";
                }
            }
        }

        if (isFoundProuduct) {
            int inProgressAndPlannedStatusDeliveriesCount = 0;
            DeliveryStatus productDelivaryStatus = null;
            try {
                List<Delivery> deliveries = deliveryRepository.findByProductId(productId);
                for (Delivery delivery : deliveries) {
                    if (delivery.getStatus() == DeliveryStatus.IN_PROGRESS) {
                        productDelivaryStatus = DeliveryStatus.IN_PROGRESS;
                        inProgressAndPlannedStatusDeliveriesCount++;
                    }
                    if (delivery.getStatus() == DeliveryStatus.PLANNED) {
                        productDelivaryStatus = DeliveryStatus.PLANNED;
                        inProgressAndPlannedStatusDeliveriesCount++;
                    }
                }
                if (productDelivaryStatus == DeliveryStatus.PLANNED || productDelivaryStatus == DeliveryStatus.IN_PROGRESS) {
                    if (inProgressAndPlannedStatusDeliveriesCount > 0) {
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