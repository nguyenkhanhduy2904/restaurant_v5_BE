package com.nkduy.restaurant_application.branch.service;

import com.nkduy.restaurant_application.branch.dto.request.CreateBranchRequest;
import com.nkduy.restaurant_application.branch.dto.request.UpdateBranchRequest;
import com.nkduy.restaurant_application.branch.dto.response.BranchInfoResponse;
import com.nkduy.restaurant_application.branch.dto.response.CreateBranchResponse;
import com.nkduy.restaurant_application.branch.entity.Branch;
import com.nkduy.restaurant_application.branch.entity.BranchProduct;
import com.nkduy.restaurant_application.branch.entity.BranchStatus;
import com.nkduy.restaurant_application.branch.repository.BranchProductRepository;
import com.nkduy.restaurant_application.branch.repository.BranchRepository;
import com.nkduy.restaurant_application.ingredient.entity.BranchIngredient;
import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import com.nkduy.restaurant_application.ingredient.entity.StockStatus;
import com.nkduy.restaurant_application.ingredient.repository.BranchIngredientRepository;
import com.nkduy.restaurant_application.ingredient.repository.IngredientRepository;
import com.nkduy.restaurant_application.ingredient.service.IngredientService;
import com.nkduy.restaurant_application.mapper.BranchMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BranchService {
    private final BranchRepository branchRepository;
    private final BranchProductService branchProductService;
    private final BranchMapper branchMapper;
    private final IngredientRepository ingredientRepository;
    private final BranchIngredientRepository branchIngredientRepository;

    @Transactional
    public CreateBranchResponse createBranch(CreateBranchRequest request) {
        Branch saved = branchRepository.save(Branch.builder()
                .name(request.name())
                .address(request.address())
                .longitude(request.longitude())
                .latitude(request.latitude())
                .status(BranchStatus.ACTIVE)
                .build());
        addAllIngredientsToBranch(saved);

        branchProductService.syncForBranch(saved, request.haveEveryProduct(), request.excludedProductIds());

//        if(request.haveEveryProduct()){
//            branchProductService.initializeBranchProducts(saved);
//        }
//        else if(request.excludedProductIds() != null){
//            branchProductService.initializeBranchProductsWithExcluded(saved, request.excludedProductIds());
//        }
        return new CreateBranchResponse(branchMapper.toBranchInfoResponse(saved));
    }

    private void addAllIngredientsToBranch(Branch branch) {
        List<Ingredient> ingredients = ingredientRepository.findAll();
        Set<String> alreadyLinked = branchIngredientRepository.findIngredientIdsByBranchId(branch.getId());

        List<BranchIngredient> rows = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (alreadyLinked.contains(ingredient.getId())) {
                continue;
            }
            rows.add(BranchIngredient.builder()
                    .branch(branch)
                    .ingredient(ingredient)
                    .quantity(BigDecimal.ZERO)
                    .status(StockStatus.OUT_OF_STOCK)
                    .build());
        }
        branchIngredientRepository.saveAll(rows);   // an empty list is fine, no isEmpty check needed
    }


//    private void addAllIngredientToBranch(String brandId) {
//        List<String> ingredientIds = ingredientRepository.findAll().stream().map(Ingredient::getId).toList();
//        Set<String> alreadyLink = branchIngredientRepository.findIngredientIdsByBranchId(brandId);
//
//        List<BranchIngredient> rows = new ArrayList<>();
//        for(String id : ingredientIds){
//            if(alreadyLink.contains(id)){
//                continue;
//            }
//            Ingredient ingredient = ingredientRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));
//            BranchIngredient row = BranchIngredient.builder()
//                    .branch(getBranchById(brandId))
//                    .ingredient(ingredient)
//                    .quantity(BigDecimal.ZERO)
//                    .status(StockStatus.OUT_OF_STOCK)   // adjust to your enum
//                    .build();
//            rows.add(row);
//
//        }
//        if(!rows.isEmpty()){
//            branchIngredientRepository.saveAll(rows);
//        }
//    }


    public List<String> getAllBranchByStatus(BranchStatus status){
        return branchRepository.findIdsByStatus(status);

   }
    public Branch getBranchById(String id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
    }


    public Page<BranchInfoResponse> getAllBranchByStatus(BranchStatus status, Pageable pageable) {
        Page<Branch> ls;

        if(status == null){
            ls = branchRepository.findAll(pageable);
        }
        else{
            ls= branchRepository.findAllByStatus(status, pageable);
        }
        return ls.map(branchMapper::toBranchInfoResponse);
    }

    @Transactional
    public BranchInfoResponse updateBranch(String id, UpdateBranchRequest request) {
        Branch branch = getBranchById(id);

        if (request.name() != null && !request.name().isBlank()) {
            branch.setName(request.name());
        }
        if (request.address() != null && !request.address().isBlank()) {
            branch.setAddress(request.address());
        }
        if (request.longitude() != null) {
            branch.setLongitude(request.longitude());
        }
        if (request.latitude() != null) {
            branch.setLatitude(request.latitude());
        }
        if (request.status() != null) {
            branch.setStatus(request.status());
        }

        if (request.haveEveryProduct() != null || request.excludedProductIds() != null) {
            if (request.haveEveryProduct() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "haveEveryProduct is required when excludedProductIds is sent");
            }
            branchProductService.syncForBranch(
                    branch, request.haveEveryProduct(), request.excludedProductIds());
        }

        return branchMapper.toBranchInfoResponse(branchRepository.save(branch));
    }
}
