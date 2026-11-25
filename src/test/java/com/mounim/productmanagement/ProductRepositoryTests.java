package com.mounim.productmanagement;

import com.mounim.productmanagement.entities.Product;
import com.mounim.productmanagement.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(showSql = false)
class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void insertProducts() {
        productRepository.saveAllAndFlush(List.of(
                new Product(null, "Ordinateur", 8500, 10),
                new Product(null, "Souris", 150, 50),
                new Product(null, "Clavier", 350, 30)));
    }

    @Test
    void findsProductsContainingANameFragment() {
        assertThat(productRepository.findByNameContaining("our"))
                .extracting(Product::getName).containsExactly("Souris");
    }

    @Test
    void findsProductsStrictlyAboveThePriceThreshold() {
        assertThat(productRepository.findByPriceGreaterThan(350))
                .extracting(Product::getName).containsExactly("Ordinateur");
    }
}
