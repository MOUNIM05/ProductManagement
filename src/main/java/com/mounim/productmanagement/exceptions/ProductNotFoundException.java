package com.mounim.productmanagement.exceptions;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Le produit " + id + " n'existe pas.");
    }
}
