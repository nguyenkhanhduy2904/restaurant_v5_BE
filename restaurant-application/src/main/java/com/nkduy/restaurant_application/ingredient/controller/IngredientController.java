package com.nkduy.restaurant_application.ingredient.controller;

import com.nkduy.restaurant_application.auth.dto.ApiResponse;
import com.nkduy.restaurant_application.ingredient.dto.model.IngredientDto;
import com.nkduy.restaurant_application.ingredient.dto.request.CreateIngredientRequest;
import com.nkduy.restaurant_application.ingredient.dto.request.IngredientListRequest;
import com.nkduy.restaurant_application.ingredient.dto.request.UpdateIngredientRequest;
import com.nkduy.restaurant_application.ingredient.dto.response.CreateIngredientResponse;
import com.nkduy.restaurant_application.ingredient.dto.response.UpdateIngredientResponse;
import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import com.nkduy.restaurant_application.ingredient.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredient")
@RequiredArgsConstructor
public class IngredientController {
    private final IngredientService ingredientService;


    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<IngredientDto>> getIngredientsList(
            @RequestParam(required = false) String name,
            @PageableDefault(
            size = 20,
            sort = "name",
            direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        return ApiResponse.ok(ingredientService.getIngredientsList(name, pageable));

    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CreateIngredientResponse> createIngredient(@Valid @RequestBody CreateIngredientRequest request){
        return ApiResponse.ok(ingredientService.createIngredient(request));

    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UpdateIngredientResponse> updateIngredient(@PathVariable String id, @Valid @RequestBody UpdateIngredientRequest request){
        return ApiResponse.ok(ingredientService.updateIngredient(id, request));
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<IngredientDto>> getIngredientsList(@RequestBody IngredientListRequest request){
        return ApiResponse.ok(ingredientService.getList(request));
    }





}
