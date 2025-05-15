package com.example.foodndeliv.controller;

import com.example.foodndeliv.dto.CreateMenuItemRequestDTO;
import com.example.foodndeliv.dto.MenuItemDTO;
import com.example.foodndeliv.service.MenuItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Grouping all menu item related operations under /api
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    // Add a menu item to a specific restaurant
    @PostMapping("/api/restaurants/{restaurantId}/menuitems")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemDTO addMenuItemToRestaurant(@PathVariable Long restaurantId, @RequestBody CreateMenuItemRequestDTO menuItemRequestDTO) {
        return menuItemService.addMenuItemToRestaurant(restaurantId, menuItemRequestDTO);
    }

    // Get all menu items for a specific restaurant
    @GetMapping("/api/restaurants/{restaurantId}/menuitems")
    public List<MenuItemDTO> getMenuItemsByRestaurant(@PathVariable Long restaurantId) {
        return menuItemService.getMenuItemsByRestaurant(restaurantId);
    }

    // Get a specific menu item by its ID
    @GetMapping("/api/menuitems/{menuItemId}")
    public MenuItemDTO getMenuItemById(@PathVariable Long menuItemId) {
        return menuItemService.getMenuItemById(menuItemId);
    }

    // Update a specific menu item
    @PutMapping("/api/menuitems/{menuItemId}")
    public MenuItemDTO updateMenuItem(@PathVariable Long menuItemId, @RequestBody CreateMenuItemRequestDTO menuItemRequestDTO) {
        return menuItemService.updateMenuItem(menuItemId, menuItemRequestDTO);
    }

    // Delete a specific menu item
    @DeleteMapping("/api/menuitems/{menuItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMenuItem(@PathVariable Long menuItemId) {
        menuItemService.deleteMenuItem(menuItemId);
    }

    // Exception Handler (basic example, you might want a global one @ControllerAdvice)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        // Log the exception ex.getMessage()
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}