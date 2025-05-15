package com.example.foodndeliv.dto;

import com.example.foodndeliv.types.CustomerState;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for representing a customer in API responses.
 * Includes the customer's ID.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private CustomerState state;
}