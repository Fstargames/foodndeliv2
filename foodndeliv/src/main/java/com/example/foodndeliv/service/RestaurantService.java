package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.CreateRestaurantRequestDTO;
import com.example.foodndeliv.dto.RestaurantDTO;
import com.example.foodndeliv.entity.Restaurant;
import com.example.foodndeliv.repository.MenuItemRepository; // Import for checking menu items
import com.example.foodndeliv.repository.OrderRepository; // Import for checking orders
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

    // Possibly needed for checking menu items and orders before deletion
    @Autowired(required = false) 
    private MenuItemRepository menuItemRepository;

    @Autowired(required = false) 
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

    // *** METHOD TO DELETE A RESTAURANT ***
    @Transactional
    public void deleteRestaurant(Long restaurantId) {
        logger.info("Attempting to delete restaurant with ID: {}", restaurantId);
        if (!restaurantRepository.existsById(restaurantId)) {
            logger.warn("Restaurant with ID: {} not found for deletion.", restaurantId);
            throw new NoSuchElementException("Restaurant not found with ID: " + restaurantId + ", cannot delete.");
        }

        restaurantRepository.deleteById(restaurantId);
        logger.info("Restaurant with ID: {} deleted successfully.", restaurantId);
    }
}