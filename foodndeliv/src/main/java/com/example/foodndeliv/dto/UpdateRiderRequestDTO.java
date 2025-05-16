package com.example.foodndeliv.dto;

import com.example.foodndeliv.types.RiderStatus;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing Rider.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRiderRequestDTO {

    @Size(min = 2, max = 100, message = "Rider name must be between 2 and 100 characters")
    private String name;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 100, message = "Vehicle details must be less than 100 characters")
    private String vehicleDetails;

    private RiderStatus status;
}
