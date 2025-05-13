package com.example.foodndeliv.dto;

import lombok.Data;
import java.util.List;

import com.example.foodndeliv.types.OrderState;

@Data
public class OrderRequestDTO {

    private Long customerId;
    private Long restaurantId;
    private List<OrderLineDTO> orderLines;
    private OrderState state;
}
