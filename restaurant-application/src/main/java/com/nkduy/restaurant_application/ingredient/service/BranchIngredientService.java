package com.nkduy.restaurant_application.ingredient.service;

import com.nkduy.restaurant_application.branch.entity.Branch;
import com.nkduy.restaurant_application.branch.entity.BranchStatus;
import com.nkduy.restaurant_application.branch.service.BranchService;
import com.nkduy.restaurant_application.ingredient.entity.BranchIngredient;
import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import com.nkduy.restaurant_application.ingredient.entity.StockStatus;
import com.nkduy.restaurant_application.ingredient.repository.BranchIngredientRepository;
import com.nkduy.restaurant_application.ingredient.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BranchIngredientService {
    private final IngredientRepository ingredientRepository;
    private final BranchService branchService;
    private final BranchIngredientRepository branchIngredientRepository;



    public void addIngredientToAllBranch(String ingredientId){
        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow();
        List<String> branchIds = branchService.getAllBranchByStatus(BranchStatus.ACTIVE);

        Set<String> alreadyLinked = branchIngredientRepository.findBranchIdsByIngredientId(ingredientId);

        List<BranchIngredient> rows = new ArrayList<>();
        for(String id : branchIds){
            if(alreadyLinked.contains(id)){
                continue;//skip if already linked
            }
            BranchIngredient row = BranchIngredient.builder()
                    .branch(branchService.getBranchById(id))
                    .ingredient(ingredient)
                    .quantity(BigDecimal.ZERO)
                    .status(StockStatus.OUT_OF_STOCK)   // adjust to your enum
                    .build();
            rows.add(row);
        }
        if(!rows.isEmpty()){
            branchIngredientRepository.saveAll(rows);
        }

    }

    @Transactional
    public void importIngredient(String ingredientId, String branchId, BigDecimal quantity){

        if (quantity == null || quantity.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
        }

        int update = branchIngredientRepository.addQuantity(branchId, ingredientId, quantity);

        if(update == 0){
            Ingredient ingredient = ingredientRepository.findById(ingredientId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));
            branchIngredientRepository.save(BranchIngredient.builder()
                    .branch(branchService.getBranchById(branchId))
                    .ingredient(ingredient)
                    .quantity(quantity)
                    .status(StockStatus.IN_STOCK)
                    .build());
        }

    }





}
