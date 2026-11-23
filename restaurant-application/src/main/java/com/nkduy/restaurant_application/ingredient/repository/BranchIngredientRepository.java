package com.nkduy.restaurant_application.ingredient.repository;

import com.nkduy.restaurant_application.ingredient.entity.BranchIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Set;

public interface BranchIngredientRepository extends JpaRepository<BranchIngredient, String> {
    // BranchIngredientRepository
    @Query("select bi.branch.id from BranchIngredient bi where bi.ingredient.id = :ingredientId")
    Set<String> findBranchIdsByIngredientId(@Param("ingredientId") String ingredientId);

    @Query("select bi.ingredient.id from BranchIngredient bi where bi.branch.id = :branchId")
    Set<String> findIngredientIdsByBranchId(@Param("branchId") String branchId);

    BranchIngredient findByBranchIdAndIngredientId(String branchId, String ingredientId);

    @Modifying
    @Query("update BranchIngredient b set b.quantity = b.quantity + :amount, " +
            "b.status = com.nkduy.restaurant_application.ingredient.entity.StockStatus.IN_STOCK, " +
            "b.updatedAt = CURRENT_TIMESTAMP " +
            "where b.branch.id = :branchId and b.ingredient.id = :ingredientId")
    int addQuantity(@Param("branchId") String branchId,
                    @Param("ingredientId") String ingredientId,
                    @Param("amount") BigDecimal amount);
}
