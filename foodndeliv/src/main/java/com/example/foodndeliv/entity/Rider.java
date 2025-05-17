package com.example.foodndeliv.entity;

import com.example.foodndeliv.types.RiderStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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


    // Constructor for creating a new Rider (ID is auto-generated)
    public Rider(String name, String phoneNumber, String vehicleDetails, RiderStatus status) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.vehicleDetails = vehicleDetails;
        this.status = status;
    }
}
