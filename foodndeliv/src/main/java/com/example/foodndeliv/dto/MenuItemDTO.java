package com.example.foodndeliv.dto;

import lombok.Data;

@Data
public class MenuItemDTO {
    private Long id;
    private Long restaurantId; // To know which restaurant it belongs to in response
    private String productName;
    private Double price;
    private boolean isAvailable;
}