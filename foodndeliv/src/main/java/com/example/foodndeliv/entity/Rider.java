package com.example.foodndeliv.entity;

import com.example.foodndeliv.types.RiderStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode; // Recommended for JPA entities
import lombok.ToString; // Recommended for JPA entities

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Rider in the system.
 */
@Entity
@Table(name = "riders")
@Data // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "vehicle_details")
    private String vehicleDetails; // e.g., "Scooter ABC-123"

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RiderStatus status;

    // Example of a relationship: A rider can be assigned to multiple orders.
    // This is a conceptual example; the exact relationship might depend on how assignments are managed.
    // If an Order has a 'rider' field, this would be the other side of a OneToMany.
    // For simplicity in this CRUD task, we might not fully implement order assignment logic here,
    // but the field can be present.
    // @OneToMany(mappedBy = "assignedRider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @ToString.Exclude // Avoid circular dependencies in toString
    // @EqualsAndHashCode.Exclude // Avoid circular dependencies in equals/hashCode
    // private List<Order> assignedOrders = new ArrayList<>();

    // Constructor for creating a new Rider (ID is auto-generated)
    public Rider(String name, String phoneNumber, String vehicleDetails, RiderStatus status) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.vehicleDetails = vehicleDetails;
        this.status = status;
    }
}
