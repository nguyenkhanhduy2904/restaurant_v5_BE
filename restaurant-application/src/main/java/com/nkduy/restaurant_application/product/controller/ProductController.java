package com.nkduy.restaurant_application.product.controller;

import com.nkduy.restaurant_application.auth.dto.ApiResponse;
import com.nkduy.restaurant_application.product.dto.request.CreateProductRequest;
import com.nkduy.restaurant_application.product.dto.request.CreateTagRequest;
import com.nkduy.restaurant_application.product.dto.request.UpdateProductRequest;
import com.nkduy.restaurant_application.product.dto.response.ProductResponse;
import com.nkduy.restaurant_application.product.dto.response.ProductSummaryResponse;
import com.nkduy.restaurant_application.product.dto.response.TagResponse;
import com.nkduy.restaurant_application.product.dto.request.UpdateTagRequest;
import com.nkduy.restaurant_application.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;


    @PostMapping("/create-product")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request){
        return ApiResponse.ok(productService.createProduct(request));

    }
    @PostMapping("/create-tag")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request){
        return ApiResponse.ok(productService.createTag(request));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<ProductSummaryResponse>> getProductList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Set<String> tagIds,
            @RequestParam(defaultValue = "false") boolean matchAllTags,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ApiResponse.ok(productService.getProductList(name, tagIds, matchAllTags, pageable));
    }


    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable String id){
        return ApiResponse.ok(productService.getProductById(id));
    }


    @GetMapping("/tag")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<TagResponse>> getAllTags(
            @RequestParam(required = false) String name,
            @PageableDefault(
                    size = 20,
                    sort = "name",
                    direction = Sort.Direction.ASC)
            Pageable pageable

    ){
        return ApiResponse.ok(productService.getAllTags(name, pageable));
    }

    @PatchMapping("/tag/{id}")
    public ApiResponse<TagResponse> updateTag(@PathVariable String id, @Valid @RequestBody UpdateTagRequest request){
        return ApiResponse.ok(productService.updateTag(id, request));
    }

    @PatchMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable String id, @Valid @RequestBody UpdateProductRequest request){

        return ApiResponse.ok(productService.updateProduct(id, request));
    }



}
