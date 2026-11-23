package com.nkduy.restaurant_application.product.service;

import com.nkduy.restaurant_application.branch.service.BranchProductService;
import com.nkduy.restaurant_application.image.entity.Upload;
import com.nkduy.restaurant_application.image.entity.UploadStatus;
import com.nkduy.restaurant_application.image.repository.UploadRepository;
import com.nkduy.restaurant_application.image.service.StorageUrlService;
import com.nkduy.restaurant_application.image.service.UploadImageService;
import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import com.nkduy.restaurant_application.ingredient.repository.IngredientRepository;
import com.nkduy.restaurant_application.mapper.ProductMapper;
import com.nkduy.restaurant_application.product.dto.request.CreateProductRequest;
import com.nkduy.restaurant_application.product.dto.request.CreateTagRequest;
import com.nkduy.restaurant_application.product.dto.request.UpdateProductRequest;
import com.nkduy.restaurant_application.product.dto.request.UpdateTagRequest;
import com.nkduy.restaurant_application.product.dto.response.ProductResponse;
import com.nkduy.restaurant_application.product.dto.response.ProductSummaryResponse;
import com.nkduy.restaurant_application.product.dto.response.TagResponse;
import com.nkduy.restaurant_application.product.entity.Product;
import com.nkduy.restaurant_application.product.entity.ProductImage;
import com.nkduy.restaurant_application.product.entity.ProductIngredient;
import com.nkduy.restaurant_application.product.entity.Tag;
import com.nkduy.restaurant_application.product.repository.ProductImageRepository;
import com.nkduy.restaurant_application.product.repository.ProductIngredientRepository;
import com.nkduy.restaurant_application.product.repository.ProductRepository;
import com.nkduy.restaurant_application.product.repository.TagRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final ProductImageRepository productImageRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductMapper productMapper;
    private final ProductIngredientRepository productIngredientRepository;

    private final BranchProductService branchProductService;
    private final ProductIngredientService productIngredientService;

    private final UploadImageService uploadImageService;
    private final StorageUrlService storageUrlService;


    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        //validate tags
        List<Tag> foundTags = tagRepository.findAllById(request.tagIds());

        if(foundTags.size() != request.tagIds().size()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more tags not found");
        }

        //validate ingredients
        List<Ingredient> ingredients = ingredientRepository.findAllById(request.ingredientMap().keySet());
        if(ingredients.size() != request.ingredientMap().size()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more ingredients not found");
        }

        //create product
        Product newProduct = Product.builder()
                .name(request.name())
                .unitPrice(request.unitPrice())
                .description(request.description())
                .tags(new HashSet<>(foundTags))
                .build();
        Product savedProduct = productRepository.save(newProduct);



        //create product ingredients
        List<ProductIngredient> rs = new ArrayList<>();

        for(Ingredient i : ingredients ){
            Double qty = request.ingredientMap().get(i.getId());
            ProductIngredient productIngredient = ProductIngredient.builder()

                    .ingredient(i)
                    .product(savedProduct)
                    .quantity(BigDecimal.valueOf(qty))
                    .build();

            rs.add(productIngredient);

        }
        productIngredientRepository.saveAll(rs);


        //create product images
        List<ProductImage> images = new ArrayList<>();
        if(request.images() != null){

            List<Upload> uploads = uploadImageService.getUploads(
                    request.images().values().stream().toList());

            Map<String, Upload> uploadMap = uploads.stream()
                    .collect(Collectors.toMap(Upload::getId, Function.identity()));

            for (Map.Entry<Integer, String> entry : request.images().entrySet()) {

                Upload upload = uploadMap.get(entry.getValue());

                ProductImage productImage = ProductImage.builder()
                        .objectKey(upload.getObjectKey())
                        .displayOrder(Long.valueOf(entry.getKey()))
                        .product(savedProduct)
                        .build();

                images.add(productImage);

                // Change upload status
                uploadImageService.changeUploadStatus(upload, UploadStatus.ATTACHED);


            }
            productImageRepository.saveAll(images);
        }
        return productMapper.toProductResponse(savedProduct, request.images(), productIngredientService.getProductIngredients(savedProduct));


    }

    @Transactional
    public TagResponse createTag(CreateTagRequest request) {

        String name = request.name().toLowerCase(Locale.ROOT);

        if(tagRepository.existsByName(name)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag already exists");
        }
        Tag newTag = Tag.builder()
                .name(name)
                .build();
        return productMapper.toTagResponse(tagRepository.save(newTag));
    }

    public Page<TagResponse> getAllTags(String name, Pageable pageable) {
        Page<Tag> ls;
        if(name==null|| name.isBlank()){
            ls = tagRepository.findAll(pageable);
        }
        else{
            ls = tagRepository.findByNameContainingIgnoreCase(name, pageable);
        }
        return ls.map(productMapper::toTagResponse);


    }

    @Transactional
    public TagResponse updateTag(String id, UpdateTagRequest request) {
        Tag tag = tagRepository.findById(id).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
        if(request.name() != null && !request.name().isBlank()){
            tag.setName(request.name().toLowerCase(Locale.ROOT));
        }
        if (request.status() != null) {
            tag.setStatus(request.status());
        }
        return productMapper.toTagResponse(tagRepository.save(tag));
    }


    public Page<ProductSummaryResponse> getProductList(
            String name,
            Set<String> tagIds,
            boolean matchAllTags,
            Pageable pageable
    ) {
        if (name != null && name.isBlank()) {
            name = null;
        }

        if (tagIds != null && tagIds.isEmpty()) {
            tagIds = null;
        }

        Page<Product> products;

        if (matchAllTags) {
            products = productRepository.findProductsMatchingAllTags(
                    name, tagIds, tagIds == null ? 0 : tagIds.size(), pageable
            );
        } else {
            products = productRepository.findProductsMatchingAnyTag(
                    name, tagIds, pageable
            );
        }
        return products.map(product -> {
            String thumbnailUrl = productImageRepository
                    .findFirstByProductIdOrderByDisplayOrderAsc(product.getId())
                    .map(ProductImage::getObjectKey)
                    .map(storageUrlService::getPublicUrl)
                    .orElse(null);

            return productMapper.toProductSummaryResponse(
                    product,
                    thumbnailUrl
            );
        });


    }

    @Transactional
    public ProductResponse updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));


        if (request.name() != null && !request.name().isBlank()) {
            product.setName(request.name().trim());
        }
        if (request.description() != null) {
            product.setDescription(request.description().trim()); // allows clearing it
        }
        if (request.unitPrice() != null) {
            product.setUnitPrice(request.unitPrice());
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        if(request.availableAtEveryBranch() != null || request.excludedBranchIds() != null){
            if (request.availableAtEveryBranch() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "availableAtEveryBranch is required when excludedBranchIds is sent");
            }
            branchProductService.syncForProduct(product, request.availableAtEveryBranch(), request.excludedBranchIds());

        }

        if (request.tagIds() != null) {
            List<Tag> tags = tagRepository.findAllById(request.tagIds());
            if (tags.size() != request.tagIds().size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more tags not found");
            }
            product.setTags(new HashSet<>(tags));
        }

        if (request.ingredientMap() != null) {
           productIngredientService.syncProductIngredients(product, request.ingredientMap());
        }

        Product saved = productRepository.save(product);



        return productMapper.toProductResponse(saved, null, productIngredientService.getProductIngredients(saved));
    }


    public ProductResponse getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Map<Integer, String> imageUrls = productImageRepository
                .findAllByProductIdOrderByDisplayOrderAsc(product.getId())
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getDisplayOrder().intValue(),
                        image -> storageUrlService.getPublicUrl(image.getObjectKey()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return productMapper.toProductResponse(product, imageUrls, productIngredientService.getProductIngredients(product));
    }


}
