package com.example.foodndeliv.controller;

import com.example.foodndeliv.dto.MenuItemRequestDTO;
import com.example.foodndeliv.dto.MenuItemResponseDTO;
import com.example.foodndeliv.service.MenuItemService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api") // Base path for this controller
public class MenuItemController {

    private static final Logger logger = LoggerFactory.getLogger(MenuItemController.class);

    @Autowired
    private MenuItemService menuItemService;

    // Add a menu item to a specific restaurant
    // POST /api/restaurants/{restaurantId}/menu-items
    @PostMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<MenuItemResponseDTO> addMenuItemToRestaurant(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuItemRequestDTO menuItemRequestDTO) {
        logger.info("Attempting to add menu item to restaurant ID: {}", restaurantId);
        MenuItemResponseDTO createdMenuItem = menuItemService.addMenuItemToRestaurant(restaurantId, menuItemRequestDTO);
        return new ResponseEntity<>(createdMenuItem, HttpStatus.CREATED);
    }

    // Get all menu items for a specific restaurant
    // GET /api/restaurants/{restaurantId}/menu-items
    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItemResponseDTO>> getMenuItemsByRestaurant(@PathVariable Long restaurantId) {
        logger.info("Fetching menu items for restaurant ID: {}", restaurantId);
        List<MenuItemResponseDTO> menuItems = menuItemService.getMenuItemsByRestaurant(restaurantId);
        return ResponseEntity.ok(menuItems);
    }

    // Get a specific menu item by its ID
    // GET /api/menu-items/{menuItemId}  (Note: 'menu-items' not 'menuitems')
    @GetMapping("/menu-items/{menuItemId}")
    public ResponseEntity<MenuItemResponseDTO> getMenuItemById(@PathVariable Long menuItemId) {
        logger.info("Fetching menu item by ID: {}", menuItemId);
        MenuItemResponseDTO menuItem = menuItemService.getMenuItemById(menuItemId);
        return ResponseEntity.ok(menuItem);
    }

    // Update a specific menu item
    // PUT /api/menu-items/{menuItemId}
    @PutMapping("/menu-items/{menuItemId}")
    public ResponseEntity<MenuItemResponseDTO> updateMenuItem(
            @PathVariable Long menuItemId,
            @Valid @RequestBody MenuItemRequestDTO menuItemRequestDTO) {
        logger.info("Updating menu item ID: {}", menuItemId);
        MenuItemResponseDTO updatedMenuItem = menuItemService.updateMenuItem(menuItemId, menuItemRequestDTO);
        return ResponseEntity.ok(updatedMenuItem);
    }

    // Delete a specific menu item
    // DELETE /api/menu-items/{menuItemId}
    @DeleteMapping("/menu-items/{menuItemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long menuItemId) {
        logger.info("Deleting menu item ID: {}", menuItemId);
        menuItemService.deleteMenuItem(menuItemId);
        return ResponseEntity.noContent().build(); // Standard for successful DELETE
    }

    // Basic Exception Handlers for this controller
    
    //@ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        logger.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //@ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Catch-all for other runtime exceptions specific to this controller's operations
    //@ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex); // Log stack trace for unexpected errors
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred. Please try again later.");
    }
}