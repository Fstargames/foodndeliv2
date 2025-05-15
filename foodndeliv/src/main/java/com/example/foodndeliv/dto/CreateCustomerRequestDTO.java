package com.example.foodndeliv.dto;

import com.example.foodndeliv.types.CustomerState;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new customer.
 * Contains validation constraints for the input fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequestDTO {

    @NotBlank(message = "Customer name cannot be blank")
    @Size(max = 255, message = "Customer name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @NotNull(message = "Customer state cannot be null")
    private CustomerState state; // e.g., ACTIVE, INACTIVE, BLOCKED
}