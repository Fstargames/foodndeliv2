package com.example.foodndeliv.controller;

import com.example.foodndeliv.dto.CreateCustomerRequestDTO;
import com.example.foodndeliv.dto.CustomerDTO;
import com.example.foodndeliv.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * REST Controller for managing Customer resources.
 * Provides endpoints for CRUD operations on Customers.
 */
@RestController
@RequestMapping("/api/customers") // Base path for all customer-related endpoints
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    /**
     * Creates a new customer.
     * Endpoint: POST /api/customers
     * @param requestDTO DTO containing the data for the new customer.
     * @return ResponseEntity with status 201 (Created), location header, and created customer DTO.
     */
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CreateCustomerRequestDTO requestDTO) {
        logger.info("CustomerController: Received request to create customer with name: {}", requestDTO.getName());
        CustomerDTO createdCustomer = customerService.createCustomer(requestDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest() // Builds from /api/customers
                .path("/{id}")
                .buildAndExpand(createdCustomer.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdCustomer);
    }

    /**
     * Retrieves a customer by their ID.
     * Endpoint: GET /api/customers/{customerId}
     * @param customerId The ID of the customer to retrieve.
     * @return ResponseEntity with status 200 (OK) and the customer DTO if found.
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long customerId) {
        logger.info("CustomerController: Received request to get customer by ID: {}", customerId);
        CustomerDTO customer = customerService.getCustomerById(customerId);
        return ResponseEntity.ok(customer);
    }

    /**
     * Retrieves all customers.
     * Endpoint: GET /api/customers
     * @return ResponseEntity with status 200 (OK) and a list of customer DTOs.
     */
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        logger.info("CustomerController: Received request to get all customers");
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Deletes a customer by their ID.
     * Endpoint: DELETE /api/customers/{customerId}
     * @param customerId The ID of the customer to delete.
     * @return ResponseEntity with status 204 (No Content) if successful.
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
        logger.info("CustomerController: Received request to delete customer with ID: {}", customerId);
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }

    // --- Exception Handlers for this Controller ---

   // @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    //@ExceptionHandler(IllegalArgumentException.class) // Catches duplicate email/name from service
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("Invalid argument or duplicate resource: {}", ex.getMessage());
        // Using 409 Conflict for duplicates is often more appropriate than 400 Bad Request
        if (ex.getMessage().contains("already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    //@ExceptionHandler(IllegalStateException.class) // For other business logic violations
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        logger.warn("Business logic violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    //@ExceptionHandler(Exception.class) // Generic catch-all for unexpected server errors
    public ResponseEntity<String> handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred in CustomerController: {}", ex.getMessage(), ex); // Log stack trace
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected internal error occurred.");
    }
}
