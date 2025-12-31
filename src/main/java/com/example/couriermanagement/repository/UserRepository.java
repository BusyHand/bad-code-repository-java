package com.example.couriermanagement.repository;

import com.example.couriermanagement.entity.User;
import com.example.couriermanagement.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);

    List<User> findByRole(UserRole role);

    @Query("SELECT u FROM User u WHERE u.role = 'courier'")
    List<User> findAllCouriers();

    @Query("SELECT u FROM User u WHERE u.role = 'manager'")
    List<User> findAllManagers();

    boolean existsByLogin(String login);
}