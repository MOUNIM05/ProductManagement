package com.mounim.productmanagement.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProductRequest(
        @NotBlank(message = "Le nom est obligatoire.")
        @Size(max = 120, message = "Le nom ne doit pas dépasser 120 caractères.")
        String name,

        @NotNull(message = "Le prix est obligatoire.")
        @PositiveOrZero(message = "Le prix doit être positif ou nul.")
        @DecimalMax(value = "999999999.99", message = "Le prix est trop élevé.")
        Double price,

        @NotNull(message = "La quantité est obligatoire.")
        @PositiveOrZero(message = "La quantité doit être positive ou nulle.")
        Integer quantity) {

    public ProductRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}
