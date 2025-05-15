package com.example.foodndeliv.entity;

import com.fasterxml.jackson.annotation.JsonBackReference; // Important for MenuItem -> Restaurant
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "menu_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"restaurant_id", "product_name"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relationship to Restaurant (Many-to-One) - THIS IS THE KEY PART
    @ManyToOne(fetch = FetchType.LAZY) // LAZY is good for performance
    @JoinColumn(name = "restaurant_id", nullable = false)
    @JsonBackReference // When serializing a MenuItem, don't re-serialize the full Restaurant here
    @ToString.Exclude // Avoid issues with Lombok's toString
    @EqualsAndHashCode.Exclude // Avoid issues with Lombok's equals/hashCode
    private Restaurant restaurant;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(nullable = false)
    private Double price;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    // Constructor for creating new instances (ID is auto-generated)
    public MenuItem(Restaurant restaurant, String productName, Double price, boolean isAvailable) {
        this.restaurant = restaurant;
        this.productName = productName;
        this.price = price;
        this.isAvailable = isAvailable;
    }
}