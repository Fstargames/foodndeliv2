package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Rider entities.
 */
@Repository
public interface RiderRepository extends JpaRepository<Rider, Long> {

    /**
     * Finds a rider by their phone number.
     * Useful for checking if a phone number is already registered.
     * @param phoneNumber The phone number to search for.
     * @return An Optional containing the rider if found, or empty otherwise.
     */
    Optional<Rider> findByPhoneNumber(String phoneNumber);
}
