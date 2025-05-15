package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.CreateRestaurantRequestDTO;
import com.example.foodndeliv.dto.RestaurantDTO;
import com.example.foodndeliv.entity.Restaurant;
import com.example.foodndeliv.repository.MenuItemRepository; // Import if checking menu items
import com.example.foodndeliv.repository.OrderRepository; // Import if checking orders
import com.example.foodndeliv.repository.RestaurantRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Assuming you might have these for more complex delete logic later
    @Autowired(required = false) // Make them optional if not strictly needed for basic delete
    private MenuItemRepository menuItemRepository;

    @Autowired(required = false) // Make them optional
    private OrderRepository orderRepository;


    @Transactional
    public RestaurantDTO createRestaurant(CreateRestaurantRequestDTO requestDTO) {
        logger.info("Creating new restaurant with name: {}", requestDTO.getName());
        Restaurant restaurant = modelMapper.map(requestDTO, Restaurant.class);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        logger.info("Restaurant created successfully with ID: {}", savedRestaurant.getId());
        return modelMapper.map(savedRestaurant, RestaurantDTO.class);
    }

    @Transactional(readOnly = true)
    public List<RestaurantDTO> getAllRestaurants() {
        logger.info("Fetching all restaurants");
        return restaurantRepository.findAll().stream()
                .map(restaurant -> modelMapper.map(restaurant, RestaurantDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RestaurantDTO getRestaurantById(Long restaurantId) {
        logger.info("Fetching restaurant with ID: {}", restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found with ID: {}", restaurantId);
                    return new NoSuchElementException("Restaurant not found with ID: " + restaurantId);
                });
        return modelMapper.map(restaurant, RestaurantDTO.class);
    }

    // *** NEW METHOD TO DELETE A RESTAURANT ***
    @Transactional
    public void deleteRestaurant(Long restaurantId) {
        logger.info("Attempting to delete restaurant with ID: {}", restaurantId);
        if (!restaurantRepository.existsById(restaurantId)) {
            logger.warn("Restaurant with ID: {} not found for deletion.", restaurantId);
            throw new NoSuchElementException("Restaurant not found with ID: " + restaurantId + ", cannot delete.");
        }

        // Business Logic Considerations (Important for a real application):
        // 1. What happens to existing MenuItems for this restaurant?
        //    - If Restaurant.menuItems has `orphanRemoval = true` and `CascadeType.ALL` (or REMOVE),
        //      deleting the restaurant will also delete its associated menu items. This is often desired.
        // 2. What happens to existing Orders for this restaurant?
        //    - If Restaurant.orders has `CascadeType.ALL` (or REMOVE), deleting the restaurant
        //      could delete its orders. This is usually NOT desired. Orders are historical records.
        //    - You might want to prevent deletion if there are non-finalized orders,
        //      or nullify the restaurant_id in orders (if your DB schema allows and it makes sense).
        //    - For this assignment, a simple delete might be acceptable, but be aware of cascading effects.
        //      Let's assume for now that cascading will handle related entities as configured in your JPA mappings.

        // Example: Check for active orders (simplified) - you'd expand this
        // List<Order> activeOrders = orderRepository.findByRestaurantIdAndStateNotIn(restaurantId, List.of(OrderState.DELIVERED, OrderState.CANCELLED));
        // if (!activeOrders.isEmpty()) {
        //    logger.warn("Cannot delete restaurant ID: {} as it has active orders.", restaurantId);
        //    throw new IllegalStateException("Cannot delete restaurant with active orders.");
        // }

        restaurantRepository.deleteById(restaurantId);
        logger.info("Restaurant with ID: {} deleted successfully.", restaurantId);
    }
}