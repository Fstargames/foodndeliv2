package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /**
     * Finds all menu items associated with a specific restaurant ID,
     * regardless of their availability.
     */
    List<MenuItem> findByRestaurantId(Long restaurantId);

    /**
     * Finds a menu item by its restaurant ID and product name, ignoring case.
     * Useful for checking existence or retrieving for updates, regardless of availability.
     */
    Optional<MenuItem> findByRestaurantIdAndProductNameIgnoreCase(Long restaurantId, String productName);

    /**
     * Finds all *available* menu items for a specific restaurant ID.
     */
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    /**
     * Finds a specific *available* menu item by its restaurant ID and product name, ignoring case.
     * Crucial for adding items to an order.
     */
    Optional<MenuItem> findByRestaurantIdAndProductNameIgnoreCaseAndIsAvailableTrue(Long restaurantId, String productName);
}