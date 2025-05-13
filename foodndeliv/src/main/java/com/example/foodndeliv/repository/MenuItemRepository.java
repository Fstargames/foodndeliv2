package com.example.foodndeliv.repository;

import com.example.foodndeliv.entity.MenuItem;
import com.example.foodndeliv.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Optional, but good practice

import java.util.List;
import java.util.Optional;

@Repository // Optional for JpaRepository, but good for clarity
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Find menu items by restaurant
    List<MenuItem> findByRestaurant(Restaurant restaurant);

    // Find a specific menu item by restaurant and product name
    Optional<MenuItem> findByRestaurantAndProductNameAndIsAvailableTrue(Restaurant restaurant, String productName);
    // Added IsAvailableTrue to ensure we only get items currently offered.
}