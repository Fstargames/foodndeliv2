package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.MenuItem;
// No need to import Restaurant if you only use restaurantId
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // --- Methods for general menu management (e.g., for restaurant owners/admins) ---

    /**
     * Finds all menu items for a specific restaurant, regardless of availability.
     * @param restaurantId The ID of the restaurant.
     * @return A list of menu items.
     */
    List<MenuItem> findByRestaurantId(Long restaurantId);

    /**
     * Finds a menu item by its product name for a specific restaurant, ignoring case.
     * Useful for checking if an item name already exists or for fetching an item for editing.
     * @param restaurantId The ID of the restaurant.
     * @param productName The name of the product (case-insensitive).
     * @return An Optional containing the menu item if found.
     */
    Optional<MenuItem> findByRestaurantIdAndProductNameIgnoreCase(Long restaurantId, String productName);


    // --- Methods primarily for customer-facing operations (e.g., placing an order) ---

    /**
     * Finds all *available* menu items for a specific restaurant.
     * @param restaurantId The ID of the restaurant.
     * @return A list of available menu items.
     */
    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    /**
     * Finds a specific *available* menu item by its product name for a specific restaurant, ignoring case.
     * This is the primary method to use when validating and fetching item details for a new order.
     * @param restaurantId The ID of the restaurant.
     * @param productName The name of the product (case-insensitive).
     * @return An Optional containing the available menu item if found.
     */
    Optional<MenuItem> findByRestaurantIdAndProductNameIgnoreCaseAndIsAvailableTrue(Long restaurantId, String productName);


    // If you still find a need for querying by the Restaurant object directly in some specific scenarios,
    // you could keep methods like your original ones, but the ID-based ones are generally more versatile for services.
    // Example:
    // List<MenuItem> findByRestaurantAndIsAvailableTrue(Restaurant restaurant);
}