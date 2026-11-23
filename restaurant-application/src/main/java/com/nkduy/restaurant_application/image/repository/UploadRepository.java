package com.nkduy.restaurant_application.image.repository;

import com.nkduy.restaurant_application.image.entity.Upload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadRepository extends JpaRepository<Upload, String> {
}
