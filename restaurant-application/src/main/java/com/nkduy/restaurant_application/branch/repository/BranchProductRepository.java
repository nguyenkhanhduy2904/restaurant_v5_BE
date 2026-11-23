package com.nkduy.restaurant_application.branch.repository;

import com.nkduy.restaurant_application.branch.entity.BranchProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchProductRepository extends JpaRepository<BranchProduct, String> {
    boolean existsByBranchIdAndProductId(String branchId, String productId);
    Optional<BranchProduct> findByBranchIdAndProductId(String branchId, String productId);

    List<BranchProduct> findByBranchId(String id);

    List<BranchProduct> findByProductId(String id);
}
