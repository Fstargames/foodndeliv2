package com.example.foodndeliv.dto;

import lombok.Data;

@Data
public class CreateMenuItemRequestDTO {
    // restaurantId will be a path variable, so not needed here
    private String productName;
    private Double price;
    private Boolean isAvailable; // Use Boolean to allow null if not provided, then default in service/entity
}