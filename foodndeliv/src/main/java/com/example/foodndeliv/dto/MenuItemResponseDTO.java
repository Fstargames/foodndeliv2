package com.example.foodndeliv.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponseDTO {

    private Long id;
    private Long restaurantId; // To show which restaurant it belongs to
    private String productName;
    private Double price;
    private boolean isAvailable;
}