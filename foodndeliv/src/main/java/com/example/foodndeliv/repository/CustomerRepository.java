package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.Optional;

/**
 * Repository interface for Customer entities.
 * Extends JpaRepository for standard CRUD operations.
 * Optionally includes findByEmail and findByName for duplicate checks.
 */
@RepositoryRestResource(path = "customers", collectionResourceRel = "customers", itemResourceRel = "customer")
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds a customer by their email address.
     * Used for checking if an email already exists.
     * @param email The email to search for.
     * @return An Optional containing the customer if found, or empty otherwise.
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Finds a customer by their name.
     * Used for checking if a name already exists (if names should be unique).
     * @param name The name to search for.
     * @return An Optional containing the customer if found, or empty otherwise.
     */
    Optional<Customer> findByName(String name); // Your Customer entity has unique=true on name

    
}