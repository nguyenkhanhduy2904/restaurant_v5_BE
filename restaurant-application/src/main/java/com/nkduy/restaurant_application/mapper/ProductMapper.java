package com.nkduy.restaurant_application.mapper;

import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import com.nkduy.restaurant_application.product.dto.response.ProductResponse;
import com.nkduy.restaurant_application.product.dto.response.ProductSummaryResponse;
import com.nkduy.restaurant_application.product.dto.response.TagResponse;
import com.nkduy.restaurant_application.product.entity.Product;
import com.nkduy.restaurant_application.product.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public TagResponse toTagResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getStatus());
    }

    public ProductResponse toProductResponse(Product product, Map<Integer, String> images, Map<String, Double> ingredientsMap) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getDescription(),
                product.getStatus(),
                product.getTags().stream()
                        .map(Tag::getId)
                        .collect(Collectors.toSet()),
                images,
                ingredientsMap
        );
    }

    public ProductSummaryResponse toProductSummaryResponse(Product product, String thumbnailImg){
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getUnitPrice(),
                product.getStatus(),
                product.getTags().stream()
                        .map(Tag::getId)
                        .collect(Collectors.toSet()),
                thumbnailImg
        );
    }

//    public ProductResponse toProductResponseSimplified(Product product) {
//
//    }
}
