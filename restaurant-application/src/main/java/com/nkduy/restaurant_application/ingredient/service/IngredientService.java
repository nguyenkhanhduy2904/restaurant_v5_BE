package com.nkduy.restaurant_application.ingredient.service;

import com.nkduy.restaurant_application.branch.entity.Branch;
import com.nkduy.restaurant_application.ingredient.dto.model.IngredientDto;
import com.nkduy.restaurant_application.ingredient.dto.request.CreateIngredientRequest;
import com.nkduy.restaurant_application.ingredient.dto.request.IngredientListRequest;
import com.nkduy.restaurant_application.ingredient.dto.request.UpdateIngredientRequest;
import com.nkduy.restaurant_application.ingredient.dto.response.CreateIngredientResponse;
import com.nkduy.restaurant_application.ingredient.dto.response.UpdateIngredientResponse;
import com.nkduy.restaurant_application.ingredient.entity.BranchIngredient;
import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import com.nkduy.restaurant_application.ingredient.repository.BranchIngredientRepository;
import com.nkduy.restaurant_application.ingredient.repository.IngredientRepository;
import com.nkduy.restaurant_application.mapper.IngredientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientRepository ingredientRepository;

    private final BranchIngredientService branchIngredientService;
    private final IngredientMapper ingredientMapper;


    public Page<IngredientDto> getIngredientsList(String name, Pageable pageable) {
        Page<Ingredient> ls;
        if(name == null || name.isBlank()){
            ls = ingredientRepository.findAll(pageable);
        }
        else{
            ls = ingredientRepository.findByNameContainingIgnoreCase(name, pageable);
        }
        return ls.map(ingredientMapper::toIngredientDto);
    }




    @Transactional
    public CreateIngredientResponse createIngredient(CreateIngredientRequest request) {
        if(ingredientRepository.existsByNameIgnoreCase(request.name())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ingredient already exists");
        }
        Ingredient newIngred = Ingredient.builder()

                .name(request.name())
                .description(request.description())
                .unit(request.unit())

                .build();
        Ingredient saved = ingredientRepository.save(newIngred);

        branchIngredientService.addIngredientToAllBranch(saved.getId());

        return new CreateIngredientResponse(ingredientMapper.toIngredientDto(saved));
    }


    public UpdateIngredientResponse updateIngredient(String id, UpdateIngredientRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ingredient not found"));

        if(request.name() != null && !request.name().isBlank()){
            ingredient.setName(request.name());
        }
        if(request.description() != null && !request.description().isBlank()){
            ingredient.setDescription(request.description());
        }
        if(request.unit() != null && !request.unit().isBlank()){
            ingredient.setUnit(request.unit());
        }
        if(request.status() != null){
            ingredient.setStatus(request.status());
        }
        return new UpdateIngredientResponse(ingredientMapper.toIngredientDto(ingredientRepository.save(ingredient)));

    }

    public List<IngredientDto> getList(IngredientListRequest request) {
        return ingredientRepository.findAllById(request.ingredientIds())
                .stream()
                .map(ingredientMapper::toIngredientDto)
                .toList();
    }
}
