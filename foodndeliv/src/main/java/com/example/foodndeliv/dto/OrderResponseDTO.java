package com.example.foodndeliv.dto;

import lombok.Data;

import java.util.List;

import com.example.foodndeliv.types.OrderState;

@Data
public class OrderResponseDTO {
    private Long id;
    private CustomerDTO customer;
    private RestaurantDTO restaurant;
    private List<OrderLineDTO> orderLines;
    private OrderState state;
    private Double totalPrice;
}

