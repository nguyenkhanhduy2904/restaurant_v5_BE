package com.nkduy.restaurant_application.ingredient.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
@Builder
@Getter
@Setter
@Entity
@Table(name = "ingredient")
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Size(max = 255)
    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Size(max = 50)
    @NotNull
    @Column(name = "unit", nullable = false, length = 50)
    private String unit;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private IngredientStatus status = IngredientStatus.ACTIVE;


}