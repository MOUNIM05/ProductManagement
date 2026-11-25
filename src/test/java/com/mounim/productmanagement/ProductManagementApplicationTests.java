package com.mounim.productmanagement;

import com.mounim.productmanagement.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:products-startup-tests",
        "spring.jpa.show-sql=false"})
class ProductManagementApplicationTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommandLineRunner initializeProducts;

    @Test
    void startsWithDemoProductsWithoutDuplicatingThem() throws Exception {
        assertThat(productRepository.findAll())
                .extracting(product -> product.getName())
                .containsExactlyInAnyOrder("Ordinateur", "Souris", "Clavier");

        initializeProducts.run();

        assertThat(productRepository.count()).isEqualTo(3);
    }

}
