package com.example.foodndeliv.service;

import com.example.foodndeliv.repository.*;
import com.example.foodndeliv.dto.*;
import com.example.foodndeliv.entity.*;
import com.example.foodndeliv.types.OrderState; // Make sure this is imported
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // Make sure this is imported
import java.util.List;      // Make sure this is imported
import java.util.stream.Collectors; // Make sure this is imported

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository; // You've correctly added this

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        Order order = new Order();

        Customer customer = customerRepository.findById(orderRequestDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + orderRequestDTO.getCustomerId()));
        Restaurant restaurant = restaurantRepository.findById(orderRequestDTO.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + orderRequestDTO.getRestaurantId()));

        order.setCustomer(customer);
        order.setRestaurant(restaurant);

        // Set initial state: use client's state if provided, otherwise default to OPEN
        if (orderRequestDTO.getState() != null) {
            order.setState(orderRequestDTO.getState());
        } else {
            order.setState(OrderState.OPEN); // Default initial state
        }

        List<OrderLine> processedOrderLines = new ArrayList<>();
        double calculatedTotalPrice = 0.0;

        if (orderRequestDTO.getOrderLines() == null || orderRequestDTO.getOrderLines().isEmpty()) {
            // Consider using a more specific exception type if you have custom exception handling
            throw new IllegalArgumentException("Order must have at least one order line.");
        }

        for (OrderLineDTO orderLineDto : orderRequestDTO.getOrderLines()) {
            // Basic validation for individual order lines
            if (orderLineDto.getProductName() == null || orderLineDto.getProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty in an order line.");
            }
            if (orderLineDto.getQuantity() == null || orderLineDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be a positive number for product: " + orderLineDto.getProductName());
            }

            // Fetch the menu item from the specific restaurant's available price list,
            // using restaurantId and ignoring case for product name.
            MenuItem menuItem = menuItemRepository
                .findByRestaurantIdAndProductNameIgnoreCaseAndIsAvailableTrue(restaurant.getId(), orderLineDto.getProductName()) // MODIFIED LINE
                .orElseThrow(() -> new RuntimeException("Product '" + orderLineDto.getProductName() +
                                                        "' not found or not available at restaurant '" + restaurant.getName() + "'."));

            OrderLine orderLineEntity = new OrderLine();
            orderLineEntity.setProductName(menuItem.getProductName()); // Use name from menu item for consistency
            orderLineEntity.setQuantity(orderLineDto.getQuantity());
            orderLineEntity.setPrice(menuItem.getPrice()); // SERVER-SIDE PRICE SET HERE
            orderLineEntity.setOrder(order); // Link back to the order

            processedOrderLines.add(orderLineEntity);
            calculatedTotalPrice += (orderLineEntity.getQuantity() * orderLineEntity.getPrice());
        }

        order.setOrderLines(processedOrderLines);

        // Assuming your Order entity has a totalPrice field.
        // If not, this line would be removed, and totalPrice would only be set in the DTO.
        // For Task 1a.iii ("automatic computation of Order totals"), setting it here is fine.
        // order.setTotalPrice(calculatedTotalPrice); // Your existing line - ensure Order entity has this field

        Order savedOrder = orderRepository.save(order); // Save order (and cascaded order lines)

        // Map to OrderResponseDTO
        OrderResponseDTO responseDTO = modelMapper.map(savedOrder, OrderResponseDTO.class);

        // Explicitly set the calculated total price in the DTO to ensure accuracy,
        // especially if Order entity doesn't persist totalPrice or if mapping needs confirmation.
        responseDTO.setTotalPrice(calculatedTotalPrice);

        // ModelMapper should handle mapping of nested CustomerDTO, RestaurantDTO, and List<OrderLineDTO>
        // within OrderResponseDTO if field names align or custom converters are set up.
        // If OrderResponseDTO.orderLines is List<OrderLineDTO>, ensure OrderLine entities map to OrderLineDTO correctly.

        return responseDTO;
    }

    public List<OrderResponseDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(order -> {
            OrderResponseDTO dto = modelMapper.map(order, OrderResponseDTO.class);

            // Calculate total price for each order DTO
            double orderTotalPrice = 0.0;
            if (order.getOrderLines() != null) {
                for (OrderLine line : order.getOrderLines()) {
                    orderTotalPrice += (line.getPrice() * line.getQuantity());
                }
            }
            dto.setTotalPrice(orderTotalPrice); // Ensure DTO has correct total
            
            // Ensure nested DTOs (like CustomerDTO, RestaurantDTO, and List<OrderLineDTO>) are mapped correctly
            // ModelMapper should handle this based on your OrderResponseDTO structure.
            // For example, if OrderResponseDTO has List<OrderLineDTO> orderLines, and OrderLineDTO has a price field.

            return dto;
        }).collect(Collectors.toList());
    }
}