package com.example.foodndeliv.dto;

import com.example.foodndeliv.types.RestaurantState;
import lombok.Data;
// Potentially import List<MenuItemDTO> if you want to include menu items in some responses
// import java.util.List;

@Data
public class RestaurantDTO { // This might be your RestaurantResponseDTO
    private Long id;
    private String name;
    private String address;
    private RestaurantState state;
    // If you want to include menu items when fetching a restaurant:
    // private List<MenuItemDTO> menuItems;
    // If you want to include orders (less common for general restaurant info):
    // private List<OrderResponseDTO> orders;
}