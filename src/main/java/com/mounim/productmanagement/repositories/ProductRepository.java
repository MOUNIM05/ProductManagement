package com.mounim.productmanagement.repositories;

import com.mounim.productmanagement.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContaining(String name);

    List<Product> findByPriceGreaterThan(double price);

    List<Product> findByNameContainingIgnoreCaseAndPriceGreaterThanEqualOrderByIdAsc(
            String name, double minPrice);
}
