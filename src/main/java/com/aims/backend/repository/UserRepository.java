package com.aims.backend.repository;

import com.aims.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findTopByOrderByEmpNoDesc(); // Added to get the user with the highest empNo
}
