package com.mounim.productmanagement.controllers;

import com.mounim.productmanagement.dto.ProductRequest;
import com.mounim.productmanagement.entities.Product;
import com.mounim.productmanagement.services.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> findAll(
            @RequestParam(defaultValue = "") @Size(max = 120) String name,
            @RequestParam(defaultValue = "0") @PositiveOrZero
            @DecimalMax("999999999.99") double minPrice) {
        return productService.findAll(name, minPrice);
    }

    @GetMapping("/{id}")
    public Product findById(@PathVariable @Positive Long id) {
        return productService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        Product product = productService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(product.getId()).toUri();
        return ResponseEntity.created(location).body(product);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable @Positive Long id,
                          @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
