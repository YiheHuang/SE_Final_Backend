package com.javaee.se_final_backend.repository;

import com.javaee.se_final_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer>{
    User findByEmail(String email);
    List<User> findByFamilyId(Integer familyId);
}
