package com.example.foodndeliv.controller;

import com.example.foodndeliv.dto.CreateRiderRequestDTO;
import com.example.foodndeliv.dto.RiderResponseDTO;
import com.example.foodndeliv.dto.UpdateRiderRequestDTO;
import com.example.foodndeliv.service.RiderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for managing Rider resources.
 * Provides a hypermedia-driven CRUD API.
 */
@RestController
@RequestMapping("/api/riders") // Base path for all rider-related endpoints
public class RiderController {

    private static final Logger logger = LoggerFactory.getLogger(RiderController.class);

    @Autowired
    private RiderService riderService;

    /**
     * Creates a new rider.
     * @param requestDTO DTO containing data for the new rider.
     * @return ResponseEntity with status 201 (Created), location header, and HATEOAS-enriched created rider DTO.
     */
    @PostMapping
    public ResponseEntity<EntityModel<RiderResponseDTO>> createRider(@Valid @RequestBody CreateRiderRequestDTO requestDTO) {
        logger.info("RiderController: Received request to create rider with phone: {}", requestDTO.getPhoneNumber());
        RiderResponseDTO createdRiderDTO = riderService.createRider(requestDTO);

        // HATEOAS links
        EntityModel<RiderResponseDTO> entityModel = EntityModel.of(createdRiderDTO,
                linkTo(methodOn(RiderController.class).getRiderById(createdRiderDTO.getId())).withSelfRel(),
                linkTo(methodOn(RiderController.class).getAllRiders()).withRel("riders"));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRiderDTO.getId())
                .toUri();

        return ResponseEntity.created(location).body(entityModel);
    }

    /**
     * Retrieves a rider by their ID.
     * @param riderId The ID of the rider to retrieve.
     * @return ResponseEntity with HATEOAS-enriched rider DTO if found.
     */
    @GetMapping("/{riderId}")
    public ResponseEntity<EntityModel<RiderResponseDTO>> getRiderById(@PathVariable Long riderId) {
        logger.info("RiderController: Received request to get rider by ID: {}", riderId);
        RiderResponseDTO riderDTO = riderService.getRiderById(riderId);

        // HATEOAS links
        EntityModel<RiderResponseDTO> entityModel = EntityModel.of(riderDTO,
                linkTo(methodOn(RiderController.class).getRiderById(riderId)).withSelfRel(),
                linkTo(methodOn(RiderController.class).getAllRiders()).withRel("riders"));
        // Add update and delete links conditionally or always
        entityModel.add(linkTo(methodOn(RiderController.class).updateRider(riderId, null)).withRel("update")); // null as placeholder
        entityModel.add(linkTo(methodOn(RiderController.class).deleteRider(riderId)).withRel("delete"));


        return ResponseEntity.ok(entityModel);
    }

    /**
     * Retrieves all riders.
     * @return ResponseEntity with a collection of HATEOAS-enriched rider DTOs.
     */
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<RiderResponseDTO>>> getAllRiders() {
        logger.info("RiderController: Received request to get all riders");
        List<RiderResponseDTO> riderDTOs = riderService.getAllRiders();

        List<EntityModel<RiderResponseDTO>> ridersWithLinks = riderDTOs.stream()
                .map(rider -> EntityModel.of(rider,
                        linkTo(methodOn(RiderController.class).getRiderById(rider.getId())).withSelfRel()))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<RiderResponseDTO>> collectionModel = CollectionModel.of(ridersWithLinks,
                linkTo(methodOn(RiderController.class).getAllRiders()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    /**
     * Updates an existing rider.
     * @param riderId The ID of the rider to update.
     * @param requestDTO DTO containing updated data.
     * @return ResponseEntity with HATEOAS-enriched updated rider DTO.
     */
    @PutMapping("/{riderId}")
    public ResponseEntity<EntityModel<RiderResponseDTO>> updateRider(
            @PathVariable Long riderId,
            @Valid @RequestBody UpdateRiderRequestDTO requestDTO) {
        logger.info("RiderController: Received request to update rider with ID: {}", riderId);
        RiderResponseDTO updatedRiderDTO = riderService.updateRider(riderId, requestDTO);

        EntityModel<RiderResponseDTO> entityModel = EntityModel.of(updatedRiderDTO,
                linkTo(methodOn(RiderController.class).getRiderById(updatedRiderDTO.getId())).withSelfRel(),
                linkTo(methodOn(RiderController.class).getAllRiders()).withRel("riders"));

        return ResponseEntity.ok(entityModel);
    }

    /**
     * Deletes a rider by their ID.
     * @param riderId The ID of the rider to delete.
     * @return ResponseEntity with status 204 (No Content).
     */
    @DeleteMapping("/{riderId}")
    public ResponseEntity<Void> deleteRider(@PathVariable Long riderId) {
        logger.info("RiderController: Received request to delete rider with ID: {}", riderId);
        riderService.deleteRider(riderId);
        return ResponseEntity.noContent().build();
    }

    // --- Exception Handlers specific to this Controller ---

    //@ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //@ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument or duplicate resource: {}", ex.getMessage());
        // Using 409 Conflict for duplicates is often more appropriate than 400 Bad Request
        if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Generic catch-all for unexpected server errors in this controller
    //@ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred in RiderController: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected internal error occurred.");
    }
}
