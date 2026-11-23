package com.nkduy.restaurant_application.user.repository;

import com.nkduy.restaurant_application.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
