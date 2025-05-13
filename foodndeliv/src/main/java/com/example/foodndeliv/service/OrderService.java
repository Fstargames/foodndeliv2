package com.example.foodndeliv.service;

import com.example.foodndeliv.repository.*;
import com.example.foodndeliv.dto.*;
import com.example.foodndeliv.entity.*;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired // ADDED
    private MenuItemRepository menuItemRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) { // MODIFIED Task 1a
        Order order = new Order();

        Customer customer = customerRepository.findById(orderRequestDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + orderRequestDTO.getCustomerId()));
        Restaurant restaurant = restaurantRepository.findById(orderRequestDTO.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found with ID: " + orderRequestDTO.getRestaurantId()));

        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setState(orderRequestDTO.getState()); // Or set a default initial state like OrderState.OPEN

        List<OrderLine> orderLines = new ArrayList<>();
        double calculatedTotalPrice = 0.0;

        if (orderRequestDTO.getOrderLines() == null || orderRequestDTO.getOrderLines().isEmpty()) {
                throw new IllegalArgumentException("Order must have at least one order line.");
        }

        for (OrderLineDTO orderLineDTO : orderRequestDTO.getOrderLines()) {
                // Fetch the menu item from the specific restaurant's price list
                MenuItem menuItem = menuItemRepository
                        .findByRestaurantAndProductNameAndIsAvailableTrue(restaurant, orderLineDTO.getProductName())
                        .orElseThrow(() -> new RuntimeException("Product '" + orderLineDTO.getProductName() +
                                "' not found or not available at restaurant '" + restaurant.getName() + "'."));

                OrderLine orderLine = new OrderLine();
                orderLine.setProductName(menuItem.getProductName()); // Use name from menu item for consistency
                orderLine.setQuantity(orderLineDTO.getQuantity());
                orderLine.setPrice(menuItem.getPrice()); // <<< SERVER-SIDE PRICE SET HERE
                orderLine.setOrder(order); // Link back to the order

                orderLines.add(orderLine);
                calculatedTotalPrice += (orderLine.getQuantity() * orderLine.getPrice());
        }

        order.setOrderLines(orderLines);
        order.setTotalPrice(calculatedTotalPrice); // <<< SET CALCULATED TOTAL PRICE HERE

        Order savedOrder = orderRepository.save(order); // Save order (and cascaded order lines)

        return modelMapper.map(savedOrder, OrderResponseDTO.class);
}

    public List<OrderResponseDTO> getAllOrders() {

        List<OrderResponseDTO> retOrders = new ArrayList<>();

        for(Order order :orderRepository.findAll())
        {
                retOrders.add(modelMapper.map(order, OrderResponseDTO.class));
        }
        return retOrders;
    }
}
