package com.example.foodndeliv.types;

/**
 * Represents the possible statuses of a Rider.
 */
public enum RiderStatus {
    AVAILABLE,      // Rider is available for new deliveries
    ON_DELIVERY,    // Rider is currently on a delivery
    OFFLINE,        // Rider is not working or unavailable
    UNAVAILABLE     // Rider is temporarily unavailable (e.g., break)
}
