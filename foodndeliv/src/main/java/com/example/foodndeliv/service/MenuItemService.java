package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.MenuItemRequestDTO;
import com.example.foodndeliv.dto.MenuItemResponseDTO;
import com.example.foodndeliv.entity.MenuItem;
import com.example.foodndeliv.entity.Restaurant;
import com.example.foodndeliv.repository.MenuItemRepository;
import com.example.foodndeliv.repository.RestaurantRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ModelMapper modelMapper; // Ensure ModelMapperConfig.java exists and is a @Configuration

    @Transactional
    public MenuItemResponseDTO addMenuItemToRestaurant(Long restaurantId, MenuItemRequestDTO menuItemRequestDTO) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new NoSuchElementException("Restaurant not found with ID: " + restaurantId));

        // Check if a menu item with the same name already exists for this restaurant
        menuItemRepository.findByRestaurantIdAndProductNameIgnoreCase(restaurantId, menuItemRequestDTO.getProductName())
            .ifPresent(item -> {
                throw new IllegalArgumentException("Menu item with name '" + menuItemRequestDTO.getProductName() + "' already exists for this restaurant.");
            });

        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(restaurant);
        menuItem.setProductName(menuItemRequestDTO.getProductName());
        menuItem.setPrice(menuItemRequestDTO.getPrice());

        if (menuItemRequestDTO.getIsAvailable() != null) {
            menuItem.setAvailable(menuItemRequestDTO.getIsAvailable());
        } else {
            menuItem.setAvailable(true); // Default to true
        }

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);

        MenuItemResponseDTO responseDTO = modelMapper.map(savedMenuItem, MenuItemResponseDTO.class);
        if (savedMenuItem.getRestaurant() != null) { // Ensure restaurantId is set in DTO
            responseDTO.setRestaurantId(savedMenuItem.getRestaurant().getId());
        }
        return responseDTO;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponseDTO> getMenuItemsByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new NoSuchElementException("Restaurant not found with ID: " + restaurantId);
        }
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(menuItem -> {
                    MenuItemResponseDTO dto = modelMapper.map(menuItem, MenuItemResponseDTO.class);
                    if (menuItem.getRestaurant() != null) {
                        dto.setRestaurantId(menuItem.getRestaurant().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuItemResponseDTO getMenuItemById(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new NoSuchElementException("Menu item not found with ID: " + menuItemId));
        MenuItemResponseDTO dto = modelMapper.map(menuItem, MenuItemResponseDTO.class);
        if (menuItem.getRestaurant() != null) {
            dto.setRestaurantId(menuItem.getRestaurant().getId());
        }
        return dto;
    }

    @Transactional
    public MenuItemResponseDTO updateMenuItem(Long menuItemId, MenuItemRequestDTO menuItemRequestDTO) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new NoSuchElementException("Menu item not found with ID: " + menuItemId));

        // Optional: Check if renaming conflicts with another item in the same restaurant
        if (!menuItem.getProductName().equalsIgnoreCase(menuItemRequestDTO.getProductName())) {
            menuItemRepository.findByRestaurantIdAndProductNameIgnoreCase(menuItem.getRestaurant().getId(), menuItemRequestDTO.getProductName())
                .ifPresent(existingItemWithNewName -> {
                    if (!existingItemWithNewName.getId().equals(menuItemId)) { // If it's a *different* item
                        throw new IllegalArgumentException("Another menu item with name '" + menuItemRequestDTO.getProductName() + "' already exists for this restaurant.");
                    }
                });
        }
        
        menuItem.setProductName(menuItemRequestDTO.getProductName());
        menuItem.setPrice(menuItemRequestDTO.getPrice());
        if (menuItemRequestDTO.getIsAvailable() != null) {
            menuItem.setAvailable(menuItemRequestDTO.getIsAvailable());
        }

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        MenuItemResponseDTO responseDTO = modelMapper.map(updatedMenuItem, MenuItemResponseDTO.class);
        if (updatedMenuItem.getRestaurant() != null) {
            responseDTO.setRestaurantId(updatedMenuItem.getRestaurant().getId());
        }
        return responseDTO;
    }

    @Transactional
    public void deleteMenuItem(Long menuItemId) {
        if (!menuItemRepository.existsById(menuItemId)) {
            throw new NoSuchElementException("Menu item not found with ID: " + menuItemId);
        }
        
        // hard delete
        menuItemRepository.deleteById(menuItemId);
    }
}