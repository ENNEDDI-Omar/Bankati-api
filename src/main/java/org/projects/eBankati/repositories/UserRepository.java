package org.projects.eBankati.repositories;

import org.projects.eBankati.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
{
    //Optional<User> findByEmail(String email);
    //boolean existsByEmail(String email);
    //Optional<User> findByIdentityNumber(String identityNumber);
}

