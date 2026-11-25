package com.mounim.productmanagement.services;

import com.mounim.productmanagement.dto.ProductRequest;
import com.mounim.productmanagement.entities.Product;
import com.mounim.productmanagement.exceptions.ProductNotFoundException;
import com.mounim.productmanagement.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll(String name, double minPrice) {
        return productRepository
                .findByNameContainingIgnoreCaseAndPriceGreaterThanEqualOrderByIdAsc(
                        name.strip(), minPrice);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional
    public Product create(ProductRequest request) {
        return productRepository.save(new Product(
                null, request.name(), request.price(), request.quantity()));
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = findById(id);
        product.setName(request.name());
        product.setPrice(request.price());
        product.setQuantity(request.quantity());
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(findById(id));
    }
}
