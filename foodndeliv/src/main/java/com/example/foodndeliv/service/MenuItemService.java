package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.CreateMenuItemRequestDTO;
import com.example.foodndeliv.dto.MenuItemDTO;
import com.example.foodndeliv.entity.MenuItem;
import com.example.foodndeliv.entity.Restaurant;
import com.example.foodndeliv.repository.MenuItemRepository;
import com.example.foodndeliv.repository.RestaurantRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository; // To fetch the restaurant

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public MenuItemDTO addMenuItemToRestaurant(Long restaurantId, CreateMenuItemRequestDTO requestDTO) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + restaurantId));

        // Check for existing menu item with the same name for this restaurant (optional, based on unique constraint)
        menuItemRepository.findByRestaurantIdAndProductNameIgnoreCase(restaurantId, requestDTO.getProductName())
            .ifPresent(item -> {
                throw new RuntimeException("Menu item '" + requestDTO.getProductName() + "' already exists for this restaurant.");
            });

        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(restaurant);
        menuItem.setProductName(requestDTO.getProductName());
        menuItem.setPrice(requestDTO.getPrice());
        if (requestDTO.getIsAvailable() != null) {
            menuItem.setAvailable(requestDTO.getIsAvailable());
        } else {
            menuItem.setAvailable(true); // Default to available
        }

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return modelMapper.map(savedMenuItem, MenuItemDTO.class);
    }

    public List<MenuItemDTO> getMenuItemsByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RuntimeException("Restaurant not found with id: " + restaurantId);
        }
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(menuItem -> modelMapper.map(menuItem, MenuItemDTO.class))
                .collect(Collectors.toList());
    }

    public MenuItemDTO getMenuItemById(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + menuItemId));
        MenuItemDTO dto = modelMapper.map(menuItem, MenuItemDTO.class);
        // Manually set restaurantId in DTO if not automatically mapped by ModelMapper
        // if (menuItem.getRestaurant() != null) {
        //     dto.setRestaurantId(menuItem.getRestaurant().getId());
        // }
        return dto;
    }

    @Transactional
    public MenuItemDTO updateMenuItem(Long menuItemId, CreateMenuItemRequestDTO requestDTO) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + menuItemId));

        // Optional: Check if the new product name conflicts with another item in the same restaurant
        if (!menuItem.getProductName().equalsIgnoreCase(requestDTO.getProductName())) {
            menuItemRepository.findByRestaurantIdAndProductNameIgnoreCase(menuItem.getRestaurant().getId(), requestDTO.getProductName())
                .ifPresent(item -> {
                    if (!item.getId().equals(menuItemId)) { // If it's a different item
                        throw new RuntimeException("Another menu item with name '" + requestDTO.getProductName() + "' already exists for this restaurant.");
                    }
                });
        }

        menuItem.setProductName(requestDTO.getProductName());
        menuItem.setPrice(requestDTO.getPrice());
        if (requestDTO.getIsAvailable() != null) {
            menuItem.setAvailable(requestDTO.getIsAvailable());
        }

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return modelMapper.map(updatedMenuItem, MenuItemDTO.class);
    }

    @Transactional
    public void deleteMenuItem(Long menuItemId) {
        if (!menuItemRepository.existsById(menuItemId)) {
            throw new RuntimeException("Menu item not found with id: " + menuItemId);
        }
        // Consider implications: what if this item is in past orders? Usually, just mark as unavailable.
        // For this assignment, delete might be acceptable.
        menuItemRepository.deleteById(menuItemId);
    }
}