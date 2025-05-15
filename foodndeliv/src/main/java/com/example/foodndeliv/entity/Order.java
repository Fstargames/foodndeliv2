package com.example.foodndeliv.entity;

import com.example.foodndeliv.types.OrderState;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "order_details", length = 500) // You might want to populate or remove this
    private String orderDetails;

    // Many-to-One with Restaurant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonBackReference("restaurant-orders") // Use a unique name for multiple back references if needed
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Restaurant restaurant;

    // Many-to-One with Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference("customer-orders") // Use a unique name
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    // One-to-Many with OrderLine
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference("order-orderLines") // Order "manages" its orderLines
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<OrderLine> orderLines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderState state;

    // Decide if you want to persist totalPrice. If so:
    // @Column(name = "total_price")
    // private Double totalPrice;
}