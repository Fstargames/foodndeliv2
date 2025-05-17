package com.example.foodndeliv.controller;

import com.example.foodndeliv.dto.CreateRestaurantRequestDTO;
import com.example.foodndeliv.dto.RestaurantDTO;
import com.example.foodndeliv.service.RestaurantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantController.class);

    @Autowired
    private RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<RestaurantDTO> createRestaurant(@Valid @RequestBody CreateRestaurantRequestDTO requestDTO) {
        logger.info("RestaurantController: Received request to create restaurant: {}", requestDTO.getName());
        RestaurantDTO createdRestaurant = restaurantService.createRestaurant(requestDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRestaurant.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdRestaurant);
    }

    @GetMapping
    public ResponseEntity<List<RestaurantDTO>> getAllRestaurants() {
        logger.info("Received request to fetch all restaurants");
        List<RestaurantDTO> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDTO> getRestaurantById(@PathVariable Long restaurantId) {
        logger.info("Received request to fetch restaurant with ID: {}", restaurantId);
        RestaurantDTO restaurant = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurant);
    }

    // *** METHOD TO HANDLE DELETE REQUESTS ***
    @DeleteMapping("/{restaurantId}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long restaurantId) {
        logger.info("RestaurantController: Received request to delete restaurant with ID: {}", restaurantId);
        restaurantService.deleteRestaurant(restaurantId);
        return ResponseEntity.noContent().build(); // Standard response for successful DELETE
    }


    // Exception Handlers
    //@ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //@ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    //@ExceptionHandler(IllegalStateException.class) // For business logic violations like "cannot delete with active orders"
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        logger.warn("Business logic violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage()); // 409 Conflict is often suitable
    }

   // @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred in RestaurantController", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
    }
}