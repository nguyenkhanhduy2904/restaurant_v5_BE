package com.nkduy.restaurant_application.auth.repository;

import com.nkduy.restaurant_application.auth.entity.LocalCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface LocalCredentialRepository extends JpaRepository<LocalCredential, String> {
    Optional<LocalCredential> findByEmail(String email);
    boolean existsByEmail(String email);
}
