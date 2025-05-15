package com.example.foodndeliv.dto;

import lombok.Data;

@Data
public class OrderLineDTO {
    private String productName;
    private Integer quantity;
    private Double price;
}
