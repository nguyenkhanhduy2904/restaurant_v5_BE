package com.nkduy.restaurant_application.product.repository;

import com.nkduy.restaurant_application.product.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, String> {
    boolean existsByName(String name);
    Optional<Tag> findByName(String name);
    Optional<Tag> findById(String id);

    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
