package com.fitness.userservice.repository;

import com.fitness.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
//    that String here represents the data type of primary key
    Boolean existsByEmail(String email);
}