package com.example.foodndeliv.entity;

import com.example.foodndeliv.types.RestaurantState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Important for Restaurant -> MenuItem
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList; // Good practice to initialize collections
import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true) // Consider if name should be unique
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private RestaurantState state;

    // Relationship to Orders (One-to-Many)
    // Orders are often a separate concern when fetching restaurant details,
    // so @JsonIgnore is usually appropriate here to avoid fetching all orders by default.
    // If you need them, fetch them through a specific order endpoint or a dedicated DTO.
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Keeping this as it was, usually good for this side
    @ToString.Exclude // Avoid issues with Lombok's toString
    @EqualsAndHashCode.Exclude // Avoid issues with Lombok's equals/hashCode
    private List<Order> orders = new ArrayList<>();

    // Relationship to MenuItems (One-to-Many) - THIS IS THE KEY PART
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference // Restaurant "manages" its menuItems; menuItems will be serialized when Restaurant is.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MenuItem> menuItems = new ArrayList<>(); // Initialize to avoid NullPointerExceptions
}