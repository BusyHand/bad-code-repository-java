package com.example.couriermanagement.repository;

import com.example.couriermanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("""
                SELECT p FROM Product p 
                WHERE p.weight <= :maxWeight
            """)
    List<Product> findByMaxWeight(@Param("maxWeight") BigDecimal maxWeight);

    @Query("""
                SELECT p FROM Product p 
                WHERE (p.length * p.width * p.height / 1000000) <= :maxVolume
            """)
    List<Product> findByMaxVolume(@Param("maxVolume") BigDecimal maxVolume);

    @Query("""
                SELECT p FROM Product p 
                WHERE p.weight <= :maxWeight 
                AND (p.length * p.width * p.height / 1000000) <= :maxVolume
            """)
    List<Product> findByMaxWeightAndVolume(
            @Param("maxWeight") BigDecimal maxWeight,
            @Param("maxVolume") BigDecimal maxVolume
    );
}