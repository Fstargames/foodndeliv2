package com.example.foodndeliv.dto;

import com.example.foodndeliv.types.RiderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new Rider.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRiderRequestDTO {

    @NotBlank(message = "Rider name cannot be blank")
    @Size(min = 2, max = 100, message = "Rider name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 100, message = "Vehicle details must be less than 100 characters")
    private String vehicleDetails;

    @NotNull(message = "Rider status cannot be null")
    private RiderStatus status;
}
