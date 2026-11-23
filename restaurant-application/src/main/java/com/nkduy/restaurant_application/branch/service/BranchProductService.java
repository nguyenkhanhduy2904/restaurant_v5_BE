package com.nkduy.restaurant_application.branch.service;

import com.nkduy.restaurant_application.branch.entity.Branch;
import com.nkduy.restaurant_application.branch.entity.BranchProduct;
import com.nkduy.restaurant_application.branch.entity.BranchProductStatus;
import com.nkduy.restaurant_application.branch.repository.BranchProductRepository;
import com.nkduy.restaurant_application.branch.repository.BranchRepository;
import com.nkduy.restaurant_application.product.entity.Product;
import com.nkduy.restaurant_application.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchProductService {
    private final BranchRepository branchRepository;
    private final BranchProductRepository branchProductRepository;
    private final ProductRepository productRepository;


    @Transactional
    public void initializeBranchProducts(Branch branch) {
        List<Product> products = productRepository.findAll();

        List<BranchProduct> branchProducts = products.stream()
                .map(product -> BranchProduct.builder()
                        .branch(branch)
                        .product(product)
                        .build())
                .toList();

        branchProductRepository.saveAll(branchProducts);
    }

    @Transactional
    public void initializeBranchProductsWithExcluded(
            Branch branch,
            Set<String> excludedProductIds
    ) {
        List<Product> products = productRepository.findAll();

        List<BranchProduct> branchProducts = products.stream()
                .map(product -> BranchProduct.builder()
                        .branch(branch)
                        .product(product)
                        .status(excludedProductIds.contains(product.getId())
                                ? BranchProductStatus.UNAVAILABLE
                                : BranchProductStatus.AVAILABLE)
                        .build())
                .toList();

        branchProductRepository.saveAll(branchProducts);
    }

    @Transactional
    public void initializeBranchProducts(Product product) {
        List<Branch> branches = branchRepository.findAll();
        List<BranchProduct> branchProducts = branches.stream()
                .map(branch -> BranchProduct.builder()
                        .branch(branch)
                        .product(product)
                        .build())
                .toList();
        branchProductRepository.saveAll(branchProducts);
    }

    @Transactional
    public void initializeBranchProductsWithExcluded(Product product, Set<String> excludedBranchIds) {
        List<Branch> branches = branchRepository.findAll();
        List<BranchProduct> branchProducts = branches.stream()
                .map(branch -> BranchProduct.builder()
                        .branch(branch)
                        .product(product)
                        .status(excludedBranchIds.contains(branch.getId())
                                ? BranchProductStatus.UNAVAILABLE
                                : BranchProductStatus.AVAILABLE)
                        .build())
                .toList();
        branchProductRepository.saveAll(branchProducts);

    }


    @Transactional
    public void syncForBranch(Branch branch, boolean haveEveryProduct, Set<String> excludedProductIds){
        Set<String> excludedIds = resolveExcluded(haveEveryProduct, excludedProductIds);

        Map<String, BranchProduct> existingRowsByProductId = existingRowsByProductId(branch);

        List<BranchProduct> toSave = new ArrayList<>();

        for (Product product : productRepository.findAll()) {
            BranchProductStatus wantedStatus = excludedIds.contains(product.getId())
                    ? BranchProductStatus.UNAVAILABLE
                    : BranchProductStatus.AVAILABLE;

            BranchProduct row = existingRowsByProductId.get(product.getId());

            if (row == null) {
                // no row yet: create one
                toSave.add(BranchProduct.builder()
                        .branch(branch)
                        .product(product)
                        .status(wantedStatus)
                        .build());
            } else if (row.getStatus() != wantedStatus) {
                // row exists but the status is wrong: fix it
                row.setStatus(wantedStatus);
                toSave.add(row);
            }
            // else: row exists and is already right, so do nothing
        }

        branchProductRepository.saveAll(toSave);
    }

    @Transactional
    public void syncForProduct(Product product, boolean availableAtEveryBranch, Set<String> excludedBranchIds) {
        Set<String> excludedIds = resolveExcluded(availableAtEveryBranch, excludedBranchIds);

        Map<String, BranchProduct> existingRowsByBranchId = existingRowsByBranchId(product);

        List<BranchProduct> toSave = new ArrayList<>();

        for (Branch branch : branchRepository.findAll()) {
            BranchProductStatus wantedStatus = excludedIds.contains(branch.getId())
                    ? BranchProductStatus.UNAVAILABLE
                    : BranchProductStatus.AVAILABLE;

            BranchProduct row = existingRowsByBranchId.get(branch.getId());

            if (row == null) {
                // no row yet: create one
                toSave.add(BranchProduct.builder()
                        .branch(branch)
                        .product(product)
                        .status(wantedStatus)
                        .build());
            } else if (row.getStatus() != wantedStatus) {
                // row exists but the status is wrong: fix it
                row.setStatus(wantedStatus);
                toSave.add(row);
            }
            // else: already right, do nothing
        }

        branchProductRepository.saveAll(toSave);
    }

    private Map<String, BranchProduct> existingRowsByBranchId(Product product) {
        List<BranchProduct> rows = branchProductRepository.findByProductId(product.getId());

        Map<String, BranchProduct> byBranchId = new HashMap<>();
        for (BranchProduct row : rows) {
            byBranchId.put(row.getBranch().getId(), row);
        }
        return byBranchId;
    }


    private Map<String, BranchProduct> existingRowsByProductId(Branch branch) {
        List<BranchProduct> rows = branchProductRepository.findByBranchId(branch.getId());

        Map<String, BranchProduct> branchProductByProductId = new HashMap<>();
        for (BranchProduct row : rows) {
            branchProductByProductId.put(row.getProduct().getId(), row);
        }
        return branchProductByProductId;
    }



    private Set<String> resolveExcluded(boolean haveEvery, Set<String> excludedIds) {
        if (haveEvery) {
            return Set.of(); // everything available; any list sent is ignored
        }
        if (excludedIds == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "An excluded list is required when not every item is available");
        }
        return excludedIds;
    }
}
