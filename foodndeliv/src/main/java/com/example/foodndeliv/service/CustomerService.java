package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.CreateCustomerRequestDTO;
import com.example.foodndeliv.dto.CustomerDTO;
import com.example.foodndeliv.entity.Customer;
import com.example.foodndeliv.repository.CustomerRepository;
// import com.example.foodndeliv.repository.OrderRepository; // If checking orders before delete
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    private ModelMapper modelMapper; // Ensure ModelMapperConfig.java exists and is a @Configuration

    // Example: For checking orders before deleting a customer
    // @Autowired(required = false) // Make it optional if not always used
    // private OrderRepository orderRepository;

    /**
     * Creates a new customer.
     * Checks for duplicate email and name before saving.
     * @param requestDTO DTO containing new customer data.
     * @return DTO of the created customer, including its generated ID.
     * @throws IllegalArgumentException if email or name already exists.
     */
    @Transactional
    public CustomerDTO createCustomer(CreateCustomerRequestDTO requestDTO) {
        logger.info("Attempting to create new customer with name: {} and email: {}", requestDTO.getName(), requestDTO.getEmail());

        // Check for existing customer with the same email
        customerRepository.findByEmail(requestDTO.getEmail()).ifPresent(existingCustomer -> {
            logger.warn("Attempt to create customer with existing email: {}", requestDTO.getEmail());
            throw new IllegalArgumentException("Customer with email '" + requestDTO.getEmail() + "' already exists.");
        });

        // Check for existing customer with the same name (since your entity has unique=true on name)
        customerRepository.findByName(requestDTO.getName()).ifPresent(existingCustomer -> {
            logger.warn("Attempt to create customer with existing name: {}", requestDTO.getName());
            throw new IllegalArgumentException("Customer with name '" + requestDTO.getName() + "' already exists.");
        });

        Customer customer = modelMapper.map(requestDTO, Customer.class);
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer created successfully with ID: {}", savedCustomer.getId());

        return modelMapper.map(savedCustomer, CustomerDTO.class);
    }

    /**
     * Retrieves a customer by their ID.
     * @param customerId The ID of the customer to retrieve.
     * @return DTO of the found customer.
     * @throws NoSuchElementException if no customer is found with the given ID.
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long customerId) {
        logger.info("Fetching customer with ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    logger.warn("Customer not found with ID: {}", customerId);
                    return new NoSuchElementException("Customer not found with ID: " + customerId);
                });
        return modelMapper.map(customer, CustomerDTO.class);
    }

    /**
     * Retrieves all customers.
     * @return A list of CustomerDTOs.
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        logger.info("Fetching all customers");
        return customerRepository.findAll().stream()
                .map(customer -> modelMapper.map(customer, CustomerDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Deletes a customer by their ID.
     * @param customerId The ID of the customer to delete.
     * @throws NoSuchElementException if no customer is found with the given ID.
     * @throws IllegalStateException if business rules prevent deletion (e.g., customer has active orders - example commented out).
     */
    @Transactional
    public void deleteCustomer(Long customerId) {
        logger.info("Attempting to delete customer with ID: {}", customerId);
        if (!customerRepository.existsById(customerId)) {
            logger.warn("Customer with ID: {} not found for deletion.", customerId);
            throw new NoSuchElementException("Customer not found with ID: " + customerId + ", cannot delete.");
        }

        
        customerRepository.deleteById(customerId);
        logger.info("Customer with ID: {} deleted successfully.", customerId);
    }
}