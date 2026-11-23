package com.nkduy.restaurant_application.branch.repository;

import com.nkduy.restaurant_application.branch.entity.Branch;
import com.nkduy.restaurant_application.branch.entity.BranchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, String> {
//    List<String> findAllByStatus(BranchStatus status);
    Page<Branch> findAllByStatus(BranchStatus status, Pageable pageable);

    // BranchRepository
    @Query("select b.id from Branch b where b.status = :status")
    List<String> findIdsByStatus(@Param("status") BranchStatus status);
}

