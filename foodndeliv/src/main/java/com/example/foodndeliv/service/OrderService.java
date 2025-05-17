package com.example.foodndeliv.service;

import com.example.foodndeliv.repository.*;
import com.example.foodndeliv.dto.*;
import com.example.foodndeliv.entity.*;
import com.example.foodndeliv.types.OrderState;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        logger.info("Creating order for customer ID: {} at restaurant ID: {}", orderRequestDTO.getCustomerId(), orderRequestDTO.getRestaurantId());
        Order order = new Order();

        Customer customer = customerRepository.findById(orderRequestDTO.getCustomerId())
                .orElseThrow(() -> {
                    logger.warn("Customer not found with ID: {}", orderRequestDTO.getCustomerId());
                    return new NoSuchElementException("Customer not found with ID: " + orderRequestDTO.getCustomerId());
                });
        Restaurant restaurant = restaurantRepository.findById(orderRequestDTO.getRestaurantId())
                .orElseThrow(() -> {
                    logger.warn("Restaurant not found with ID: {}", orderRequestDTO.getRestaurantId());
                    return new NoSuchElementException("Restaurant not found with ID: " + orderRequestDTO.getRestaurantId());
                });

        order.setCustomer(customer);
        order.setRestaurant(restaurant);

        if (orderRequestDTO.getState() != null) {
            order.setState(orderRequestDTO.getState());
        } else {
            order.setState(OrderState.OPEN);
        }

        List<OrderLine> processedOrderLines = new ArrayList<>();
        double calculatedTotalPrice = 0.0;

        if (orderRequestDTO.getOrderLines() == null || orderRequestDTO.getOrderLines().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one order line.");
        }

        for (OrderLineDTO orderLineDto : orderRequestDTO.getOrderLines()) {
            if (orderLineDto.getProductName() == null || orderLineDto.getProductName().trim().isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty in an order line.");
            }
            if (orderLineDto.getQuantity() == null || orderLineDto.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be a positive number for product: " + orderLineDto.getProductName());
            }

            MenuItem menuItem = menuItemRepository
                .findByRestaurantIdAndProductNameIgnoreCaseAndIsAvailableTrue(restaurant.getId(), orderLineDto.getProductName())
                .orElseThrow(() -> {
                    logger.warn("Product '{}' not found/available at restaurant '{}' (ID: {})", orderLineDto.getProductName(), restaurant.getName(), restaurant.getId());
                    return new NoSuchElementException("Product '" + orderLineDto.getProductName() +
                                                        "' not found or not available at restaurant '" + restaurant.getName() + "'.");
                });

            OrderLine orderLineEntity = new OrderLine();
            orderLineEntity.setProductName(menuItem.getProductName());
            orderLineEntity.setQuantity(orderLineDto.getQuantity());
            orderLineEntity.setPrice(menuItem.getPrice()); // SERVER-SIDE PRICE
            orderLineEntity.setOrder(order);

            processedOrderLines.add(orderLineEntity);
            calculatedTotalPrice += (orderLineEntity.getQuantity() * orderLineEntity.getPrice());
        }

        order.setOrderLines(processedOrderLines);
        // to store totalPrice in the Order entity, uncomment:
        // order.setTotalPrice(calculatedTotalPrice); 

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", savedOrder.getId());

        OrderResponseDTO responseDTO = modelMapper.map(savedOrder, OrderResponseDTO.class);
        responseDTO.setTotalPrice(calculatedTotalPrice); // Ensure DTO has the calculated total

        return responseDTO;
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        logger.info("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(order -> {
            OrderResponseDTO dto = modelMapper.map(order, OrderResponseDTO.class);
            double orderTotalPrice = 0.0;
            if (order.getOrderLines() != null) {
                orderTotalPrice = order.getOrderLines().stream()
                                    .mapToDouble(line -> line.getPrice() * line.getQuantity())
                                    .sum();
            }
            dto.setTotalPrice(orderTotalPrice);
            return dto;
        }).collect(Collectors.toList());
    }
}