package com.nkduy.restaurant_application.ingredient.entity;

import com.nkduy.restaurant_application.branch.entity.Branch;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
@Builder
@Getter
@Setter
@Entity
@Table(name = "branch_ingredient")
@NoArgsConstructor
@AllArgsConstructor
public class BranchIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ingredient_id", nullable = false)
    private Ingredient ingredient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @NotNull
    @Column(name = "quantity", nullable = false, precision = 10)
    private BigDecimal quantity;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private StockStatus status;


    //
    public void recalculateStatus() {
        this.status = quantity.signum() > 0 ? StockStatus.IN_STOCK : StockStatus.OUT_OF_STOCK;
        // TODO: LOW_STOCK when quantity <= some threshold
    }

}