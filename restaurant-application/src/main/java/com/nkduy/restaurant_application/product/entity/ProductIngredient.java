package com.nkduy.restaurant_application.product.entity;

import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
@Builder
@Getter
@Setter
@Entity
@Table(name = "product_ingredient")
@NoArgsConstructor
@AllArgsConstructor
public class ProductIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(name = "quantity", nullable = false, precision = 10)
    private BigDecimal quantity;


}