package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.CreateRiderRequestDTO;
import com.example.foodndeliv.dto.RiderResponseDTO;
import com.example.foodndeliv.dto.UpdateRiderRequestDTO;
import com.example.foodndeliv.entity.Rider;
import com.example.foodndeliv.repository.RiderRepository;
import com.example.foodndeliv.types.RiderStatus; // Ensure this import is present

// Keycloak Imports
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import jakarta.ws.rs.core.Response; // For Keycloak Admin Client Response

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class RiderService {

    private static final Logger logger = LoggerFactory.getLogger(RiderService.class);

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private Keycloak keycloakAdminClient;

    @Value("${keycloak.target-realm}")
    private String targetRealm; // This should be "fnd"

    @Transactional
    public RiderResponseDTO createRider(CreateRiderRequestDTO requestDTO) {
        logger.info("Attempting to create new rider with name: {} and phone number: {}", requestDTO.getName(), requestDTO.getPhoneNumber());

        riderRepository.findByPhoneNumber(requestDTO.getPhoneNumber()).ifPresent(existingRider -> {
            logger.warn("Attempt to create rider with existing phone number: {}", requestDTO.getPhoneNumber());
            throw new IllegalArgumentException("Rider with phone number '" + requestDTO.getPhoneNumber() + "' already exists.");
        });

        Rider rider = modelMapper.map(requestDTO, Rider.class);
        if (rider.getStatus() == null) {
             rider.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : RiderStatus.AVAILABLE); // Default
        }

        Rider savedRider = riderRepository.save(rider);
        logger.info("Rider created successfully in DB with ID: {}", savedRider.getId());

        // --- START: Keycloak User Creation Logic for Rider ---
        Response response = null; // Initialize to null
        try {
            RealmResource realmResource = keycloakAdminClient.realm(targetRealm);
            UsersResource usersResource = realmResource.users();

            UserRepresentation keycloakUser = new UserRepresentation();
    
            String riderName = savedRider.getName();
            // MODIFICATION: Sanitize name and append ID for uniqueness & Keycloak compatibility
            String keycloakUsername = riderName.replaceAll("\\s+", "_").toLowerCase() + "_" + savedRider.getId();

            logger.info("Attempting to set Keycloak username to: {}", keycloakUsername);
            keycloakUser.setUsername(keycloakUsername);

            // Set first/last name if possible (Keycloak likes these for its UI)
            String[] nameParts = riderName.split("\\s+", 2);
            keycloakUser.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                keycloakUser.setLastName(nameParts[1]);
            } else {
                keycloakUser.setLastName("Rider"); // Default if only one name part
            }
            
            // IMPORTANT: If Riders are supposed to have emails for login or notifications,
            // you need to add an 'email' field to CreateRiderRequestDTO and Rider entity.
            // Then uncomment and adapt the following:
            // if (requestDTO.getEmail() != null && !requestDTO.getEmail().isEmpty()) { // Assuming DTO has email
            //    keycloakUser.setEmail(requestDTO.getEmail());
            //    keycloakUser.setEmailVerified(false); // Or true if you have a verification flow
            // } else {
            //    // If email is mandatory in Keycloak realm settings and not provided, creation might fail.
            //    // Consider a placeholder or generated email if riders don't have emails.
            //    // For example, if Keycloak requires an email:
            //    keycloakUser.setEmail(keycloakUsername + "@default.rider.local"); // Placeholder
            //    keycloakUser.setEmailVerified(false);
            //    logger.warn("Rider {} has no email provided; using placeholder for Keycloak user creation.", riderName);
            // }

            keycloakUser.setEnabled(true);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setTemporary(true);
            credential.setValue("TempRiderPass123!"); // TODO: Make this configurable or randomly generated
            keycloakUser.setCredentials(Collections.singletonList(credential));

            response = usersResource.create(keycloakUser); // Assign to response

            if (response.getStatus() == 201) { // HTTP 201 Created
                String keycloakUserId = CreatedResponseUtil.getCreatedId(response);
                logger.info("Keycloak user created successfully with ID: {} for rider: {} (Keycloak Username: {})", keycloakUserId, savedRider.getName(), keycloakUsername);

                // Assign 'rider' role
                try {
                    RoleRepresentation riderRoleRepresentation = realmResource.roles().get("rider").toRepresentation();
                    if (riderRoleRepresentation != null) {
                        usersResource.get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(riderRoleRepresentation));
                        logger.info("Assigned 'rider' role to Keycloak user ID: {}", keycloakUserId);
                    } else {
                        logger.warn("'rider' role not found in Keycloak realm: {}", targetRealm);
                    }
                } catch (Exception e) {
                     logger.error("Error assigning 'rider' role to Keycloak user ID {}: {}", keycloakUserId, e.getMessage(), e);
                }

                // Set 'rider_id' attribute
                try {
                    UserResource userResource = usersResource.get(keycloakUserId);
                    UserRepresentation userToUpdate = userResource.toRepresentation();
                    Map<String, List<String>> attributes = userToUpdate.getAttributes() == null ? new HashMap<>() : new HashMap<>(userToUpdate.getAttributes());
                    attributes.put("rider_id", Collections.singletonList(savedRider.getId().toString()));
                    userToUpdate.setAttributes(attributes);
                    userResource.update(userToUpdate);
                    logger.info("Set 'rider_id' attribute for Keycloak user ID: {}", keycloakUserId);
                } catch (Exception e) {
                    logger.error("Error setting 'rider_id' attribute for Keycloak user ID {}: {}", keycloakUserId, e.getMessage(), e);
                }

            } else {
                String errorReason = response.getStatusInfo() != null ? response.getStatusInfo().getReasonPhrase() : "Unknown reason";
                String errorDetails = "No details";
                 if (response.hasEntity()) {
                    try {
                        errorDetails = response.readEntity(String.class);
                    } catch (Exception e) {
                        logger.warn("Could not read error entity from Keycloak response for rider {}: {}", savedRider.getName(), e);
                    }
                }
                logger.error("Failed to create Keycloak user for rider: {}. Username attempted: {}. Status: {}. Reason: {}. Details: {}",
                             savedRider.getName(), keycloakUsername, response.getStatus(), errorReason, errorDetails);
            }
        } catch (Exception e) {
            logger.error("Overall exception during Keycloak user creation process for rider {}: {}", savedRider.getName(), e.getMessage(), e);
        } finally {
            if (response != null) {
                response.close(); // Ensure response is always closed
            }
        }
        // --- END: Keycloak User Creation Logic for Rider ---

        return modelMapper.map(savedRider, RiderResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public RiderResponseDTO getRiderById(Long riderId) {
        logger.info("Fetching rider with ID: {}", riderId);
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> {
                    logger.warn("Rider not found with ID: {}", riderId);
                    return new NoSuchElementException("Rider not found with ID: " + riderId);
                });
        return modelMapper.map(rider, RiderResponseDTO.class);
    }

    @Transactional(readOnly = true)
    public List<RiderResponseDTO> getAllRiders() {
        logger.info("Fetching all riders");
        return riderRepository.findAll().stream()
                .map(rider -> modelMapper.map(rider, RiderResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public RiderResponseDTO updateRider(Long riderId, UpdateRiderRequestDTO requestDTO) {
        logger.info("Attempting to update rider with ID: {}", riderId);
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> {
                    logger.warn("Rider not found with ID: {} for update.", riderId);
                    return new NoSuchElementException("Rider not found with ID: " + riderId);
                });

        if (requestDTO.getPhoneNumber() != null && !requestDTO.getPhoneNumber().equals(rider.getPhoneNumber())) {
            riderRepository.findByPhoneNumber(requestDTO.getPhoneNumber()).ifPresent(existingRider -> {
                if (!existingRider.getId().equals(riderId)) { 
                    logger.warn("Attempt to update rider ID {} with phone number {} which already exists for rider ID {}",
                            riderId, requestDTO.getPhoneNumber(), existingRider.getId());
                    throw new IllegalArgumentException("Phone number '" + requestDTO.getPhoneNumber() + "' is already in use by another rider.");
                }
            });
            rider.setPhoneNumber(requestDTO.getPhoneNumber());
        }

        if (requestDTO.getName() != null) {
            // If name is used in username and is changed, Keycloak username might need an update too.
            // This is complex. For now, we assume username is set at creation and not changed.
            rider.setName(requestDTO.getName());
        }
        if (requestDTO.getVehicleDetails() != null) {
            rider.setVehicleDetails(requestDTO.getVehicleDetails());
        }
        if (requestDTO.getStatus() != null) {
            rider.setStatus(requestDTO.getStatus());
        }

        Rider updatedRider = riderRepository.save(rider);
        logger.info("Rider with ID: {} updated successfully.", updatedRider.getId());
        return modelMapper.map(updatedRider, RiderResponseDTO.class);
    }

    @Transactional
    public void deleteRider(Long riderId) {
        logger.info("Attempting to delete rider with ID: {}", riderId);
        Rider rider = riderRepository.findById(riderId)
            .orElseThrow(() -> {
                logger.warn("Rider with ID: {} not found for deletion.", riderId);
                return new NoSuchElementException("Rider not found with ID: " + riderId);
            });

        try {
            RealmResource realmResource = keycloakAdminClient.realm(targetRealm);
            UsersResource usersResource = realmResource.users();
            
            // Option 1: Find by 'rider_id' attribute (more robust)
            List<UserRepresentation> keycloakUsersByAttribute = usersResource.searchByAttributes("rider_id:" + rider.getId().toString());
            if (keycloakUsersByAttribute != null && !keycloakUsersByAttribute.isEmpty()) {
                for (UserRepresentation ku : keycloakUsersByAttribute) {
                    usersResource.get(ku.getId()).remove();
                    logger.info("Keycloak user for rider {} (Keycloak ID: {}, Attribute rider_id: {}) deleted successfully.", 
                                rider.getName(), ku.getId(), rider.getId().toString());
                }
            } else {
                // Option 2: Fallback to finding by constructed username (less robust if username format changes or wasn't unique)
                String keycloakUsernameToDelete = rider.getName().replaceAll("\\s+", "_").toLowerCase() + "_" + rider.getId();
                List<UserRepresentation> keycloakUsersByUsername = usersResource.searchByUsername(keycloakUsernameToDelete, true);
                 if (keycloakUsersByUsername != null && !keycloakUsersByUsername.isEmpty()) {
                    usersResource.get(keycloakUsersByUsername.get(0).getId()).remove(); // Assuming first match is correct
                     logger.info("Keycloak user for rider {} (Keycloak Username: {}) deleted successfully (fallback by username).", rider.getName(), keycloakUsernameToDelete);
                } else {
                    logger.warn("Could not find Keycloak user for rider ID: {} or username: {} to delete.", rider.getId(), keycloakUsernameToDelete);
                }
            }
        } catch (Exception e) {
            logger.error("Error deleting Keycloak user for rider {}: {}", rider.getName(), e.getMessage(), e);
        }
        
        riderRepository.deleteById(riderId);
        logger.info("Rider with ID: {} deleted successfully from DB.", riderId);
    }
}