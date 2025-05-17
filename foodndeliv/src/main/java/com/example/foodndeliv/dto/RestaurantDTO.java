package com.example.foodndeliv.dto;

import com.example.foodndeliv.types.RestaurantState;
import lombok.Data;

@Data
public class RestaurantDTO { 
    private Long id;
    private String name;
    private String address;
    private RestaurantState state;
}