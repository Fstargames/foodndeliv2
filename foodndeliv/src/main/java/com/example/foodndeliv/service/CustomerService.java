package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.CreateCustomerRequestDTO;
import com.example.foodndeliv.dto.CustomerDTO;
import com.example.foodndeliv.entity.Customer;
import com.example.foodndeliv.repository.CustomerRepository;
import com.example.foodndeliv.types.CustomerState; // Make sure this import is present

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Required for @Value
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.core.Response; // For Keycloak Admin Client Response
import java.util.Collections;
import java.util.HashMap; // For attributes
import java.util.List;
import java.util.Map; // For attributes
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service class for managing Customer entities.
 * Handles business logic for creating, retrieving, and deleting customers.
 */
@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ModelMapper modelMapper;

    // --- START: Added for Keycloak Integration ---
    @Autowired
    private Keycloak keycloakAdminClient; // Inject the Keycloak admin client bean

    @Value("${keycloak.target-realm}") // Inject the target realm from application.properties
    private String targetRealm; // This should be "fnd"
    // --- END: Added for Keycloak Integration ---

    /**
     * Creates a new customer and a corresponding Keycloak user.
     * Checks for duplicate email and name before saving.
     * @param requestDTO DTO containing new customer data.
     * @return DTO of the created customer, including its generated ID.
     * @throws IllegalArgumentException if email or name already exists.
     */
    @Transactional
    public CustomerDTO createCustomer(CreateCustomerRequestDTO requestDTO) {
        logger.info("Attempting to create new customer with name: {} and email: {}", requestDTO.getName(), requestDTO.getEmail());

        customerRepository.findByEmail(requestDTO.getEmail()).ifPresent(existingCustomer -> {
            logger.warn("Attempt to create customer with existing email: {}", requestDTO.getEmail());
            throw new IllegalArgumentException("Customer with email '" + requestDTO.getEmail() + "' already exists.");
        });

        customerRepository.findByName(requestDTO.getName()).ifPresent(existingCustomer -> {
            logger.warn("Attempt to create customer with existing name: {}", requestDTO.getName());
            throw new IllegalArgumentException("Customer with name '" + requestDTO.getName() + "' already exists.");
        });

        Customer customerToSave = modelMapper.map(requestDTO, Customer.class);
        if (customerToSave.getState() == null) {
            // Use state from DTO if available, otherwise default to ACTIVE
            customerToSave.setState(requestDTO.getState() != null ? requestDTO.getState() : CustomerState.ACTIVE);
        }


        Customer savedCustomer = customerRepository.save(customerToSave);
        logger.info("Customer created successfully in DB with ID: {}", savedCustomer.getId());

        // --- START: Keycloak User Creation Logic ---
        try {
            RealmResource realmResource = keycloakAdminClient.realm(targetRealm); // Use injected targetRealm (e.g., "fnd")
            UsersResource usersResource = realmResource.users();

            UserRepresentation keycloakUser = new UserRepresentation();
            keycloakUser.setUsername(savedCustomer.getName()); // Consider using email or a generated unique username
            keycloakUser.setEmail(savedCustomer.getEmail());
            keycloakUser.setEnabled(true);
            keycloakUser.setEmailVerified(true); // Set as per your policy, can be false initially

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setTemporary(true); // User will be forced to change password on first login
            credential.setValue("Welcome123!"); // TODO: Use a more secure/configurable temporary password
            keycloakUser.setCredentials(Collections.singletonList(credential));

            Response response = usersResource.create(keycloakUser);

            if (response.getStatus() == 201) { // HTTP 201 Created
                String keycloakUserId = CreatedResponseUtil.getCreatedId(response);
                logger.info("Keycloak user created successfully with ID: {} for customer: {}", keycloakUserId, savedCustomer.getName());

                // Assign 'customer' role
                try {
                    RoleRepresentation customerRoleRepresentation = realmResource.roles().get("customer").toRepresentation();
                    if (customerRoleRepresentation != null) {
                        usersResource.get(keycloakUserId).roles().realmLevel().add(Collections.singletonList(customerRoleRepresentation));
                        logger.info("Assigned 'customer' role to Keycloak user ID: {}", keycloakUserId);
                    } else {
                        logger.warn("'customer' role not found in Keycloak realm: {}", targetRealm);
                    }
                } catch (Exception e) {
                     logger.error("Error assigning 'customer' role to Keycloak user ID {}: {}", keycloakUserId, e.getMessage());
                }

                // Set 'custid' attribute (application-specific customer ID)
                try {
                    UserResource userResource = usersResource.get(keycloakUserId);
                    UserRepresentation userToUpdate = userResource.toRepresentation(); // Get current representation
                    Map<String, List<String>> attributes = userToUpdate.getAttributes() == null ? new HashMap<>() : new HashMap<>(userToUpdate.getAttributes());
                    attributes.put("custid", Collections.singletonList(savedCustomer.getId().toString()));
                    userToUpdate.setAttributes(attributes);
                    userResource.update(userToUpdate); // Update the user with new attributes
                    logger.info("Set 'custid' attribute for Keycloak user ID: {}", keycloakUserId);
                } catch (Exception e) {
                    logger.error("Error setting 'custid' attribute for Keycloak user ID {}: {}", keycloakUserId, e.getMessage());
                }

            } else {
                String errorReason = response.getStatusInfo() != null ? response.getStatusInfo().getReasonPhrase() : "Unknown reason";
                String errorDetails = "No details";
                if (response.hasEntity()) {
                    try {
                        errorDetails = response.readEntity(String.class);
                    } catch (Exception e) {
                        logger.warn("Could not read error entity from Keycloak response", e);
                    }
                }
                logger.error("Failed to create Keycloak user for customer: {}. Status: {}. Reason: {}. Details: {}",
                             savedCustomer.getName(), response.getStatus(), errorReason, errorDetails);
            }
            response.close(); // Important: always close the Keycloak response
        } catch (Exception e) {
            logger.error("Exception during Keycloak user creation process for customer {}: {}", savedCustomer.getName(), e.getMessage(), e);
        }
        // --- END: Keycloak User Creation Logic ---

        return modelMapper.map(savedCustomer, CustomerDTO.class);
    }

    /**
     * Retrieves a customer by their ID.
     * @param customerId The ID of the customer to retrieve.
     * @return CustomerDTO if found.
     * @throws NoSuchElementException if customer with the given ID is not found.
     */
    public CustomerDTO getCustomerById(Long customerId) {
        logger.info("Attempting to retrieve customer with ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.warn("Customer with ID: {} not found.", customerId);
                    return new NoSuchElementException("Customer not found with ID: " + customerId);
                });
        logger.info("Customer found with ID: {}", customerId);
        return modelMapper.map(customer, CustomerDTO.class);
    }

    /**
     * Retrieves all customers.
     * @return List of CustomerDTOs.
     */
    public List<CustomerDTO> getAllCustomers() {
        logger.info("Attempting to retrieve all customers.");
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            logger.info("No customers found in the database.");
        } else {
            logger.info("Retrieved {} customers.", customers.size());
        }
        return customers.stream()
                .map(customer -> modelMapper.map(customer, CustomerDTO.class))
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteCustomer(Long customerId) {
        logger.info("Attempting to delete customer with ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> {
                logger.warn("Customer with ID: {} not found for deletion.", customerId);
                return new NoSuchElementException("Customer not found with ID: " + customerId + ", cannot delete.");
            });

        // --- START: Keycloak User Deletion Logic (Optional but good practice) ---
        try {
            RealmResource realmResource = keycloakAdminClient.realm(targetRealm);
            UsersResource usersResource = realmResource.users();
            // Assuming customer name is unique and used as Keycloak username
            // A more robust way would be to store the Keycloak User ID in your Customer entity
            // or search by email if that's guaranteed to be the Keycloak username.
            List<UserRepresentation> keycloakUsers = usersResource.searchByUsername(customer.getName(), true);
            if (keycloakUsers != null && !keycloakUsers.isEmpty()) {
                if (keycloakUsers.size() > 1) {
                    logger.warn("Multiple Keycloak users found for username: {}. Attempting to find by email or 'custid' attribute if possible.", customer.getName());
                    // Add logic here to disambiguate if necessary, e.g., by email or the 'custid' attribute
                    // For now, we'll proceed with the first one, but this is a potential issue.
                }
                String keycloakUserId = keycloakUsers.get(0).getId(); // Be cautious if multiple users can have the same username
                usersResource.get(keycloakUserId).remove();
                logger.info("Keycloak user for customer {} (Keycloak ID: {}) deleted successfully.", customer.getName(), keycloakUserId);
            } else {
                logger.warn("Could not find Keycloak user for customer name: {} to delete. Attempting to search by email.", customer.getName());
                keycloakUsers = usersResource.searchByEmail(customer.getEmail(), true);
                 if (keycloakUsers != null && !keycloakUsers.isEmpty()) {
                    String keycloakUserId = keycloakUsers.get(0).getId();
                    usersResource.get(keycloakUserId).remove();
                    logger.info("Keycloak user for customer email {} (Keycloak ID: {}) deleted successfully.", customer.getEmail(), keycloakUserId);
                } else {
                     logger.warn("Could not find Keycloak user for customer email: {} to delete.", customer.getEmail());
                }
            }
        } catch (Exception e) {
            logger.error("Error deleting Keycloak user for customer {}: {}", customer.getName(), e.getMessage(), e);
        }
        // --- END: Keycloak User Deletion Logic ---
        
        customerRepository.deleteById(customerId);
        logger.info("Customer with ID: {} deleted successfully from DB.", customerId);
    }
}