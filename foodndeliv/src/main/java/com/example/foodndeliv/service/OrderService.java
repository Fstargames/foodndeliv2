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

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO) {
        Order order = new Order();

        Customer customer = customerRepository.findById(orderRequestDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Restaurant restaurant = restaurantRepository.findById(orderRequestDTO.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        
        order.setCustomer(customer);
        order.setRestaurant(restaurant);
        order.setState(orderRequestDTO.getState());

        List<OrderLine> orderLines = new ArrayList<>();

        for(OrderLineDTO orderlinedto : orderRequestDTO.getOrderLines())
        {
                orderLines.add(modelMapper.map(orderlinedto, OrderLine.class));
        }

        orderLines.forEach(orderLine -> orderLine.setOrder(order));
        order.setOrderLines(orderLines);

        orderRepository.save(order);

        return modelMapper.map(order, OrderResponseDTO.class);
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
