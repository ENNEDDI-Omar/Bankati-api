package org.projects.eBankati.repositories;

import org.projects.eBankati.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long>
{

    Optional<User> findByUsername(String username);
    //Optional<User> findByEmail(String email);
    //boolean existsByEmail(String email);
    //List<User> findByAgeGreaterThanEqual(Integer age);
    //List<User> findByCreditScoreGreaterThanEqual(Integer creditScore);
}

