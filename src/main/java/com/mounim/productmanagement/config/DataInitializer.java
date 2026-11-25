package com.mounim.productmanagement.config;

import com.mounim.productmanagement.entities.Product;
import com.mounim.productmanagement.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner initializeProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                        new Product(null, "Ordinateur", 8500, 10),
                        new Product(null, "Souris", 150, 50),
                        new Product(null, "Clavier", 350, 30)));
            }

            LOGGER.info("Tous les produits : {}", productRepository.findAll());
            LOGGER.info("Produits dont le nom contient 'o' : {}",
                    productRepository.findByNameContaining("o"));
            LOGGER.info("Produits dont le prix dépasse 500 : {}",
                    productRepository.findByPriceGreaterThan(500));
        };
    }
}
