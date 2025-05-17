package com.example.foodndeliv.dto;

import com.example.foodndeliv.types.RestaurantState; 
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRestaurantRequestDTO {

    @NotBlank(message = "Restaurant name cannot be blank")
    @Size(max = 255, message = "Restaurant name must be less than 255 characters")
    private String name;

    @Size(max = 500, message = "Address must be less than 500 characters")
    private String address; // Assuming address is a simple string

    @NotNull(message = "Restaurant state cannot be null")
    private RestaurantState state; // e.g., OPEN, CLOSED
}