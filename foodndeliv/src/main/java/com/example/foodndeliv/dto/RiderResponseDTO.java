package com.example.foodndeliv.dto;

import com.example.foodndeliv.types.RiderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * DTO for API responses for a Rider.
 * Extends RepresentationModel to support HATEOAS links.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true) // Important for RepresentationModel subclasses
@Relation(collectionRelation = "riders", itemRelation = "rider") // For HAL link relations
public class RiderResponseDTO extends RepresentationModel<RiderResponseDTO> {

    private Long id;
    private String name;
    private String phoneNumber;
    private String vehicleDetails;
    private RiderStatus status;
}
