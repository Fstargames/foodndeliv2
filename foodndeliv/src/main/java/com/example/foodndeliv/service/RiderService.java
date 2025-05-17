package com.example.foodndeliv.service;

import com.example.foodndeliv.dto.CreateRiderRequestDTO;
import com.example.foodndeliv.dto.RiderResponseDTO;
import com.example.foodndeliv.dto.UpdateRiderRequestDTO;
import com.example.foodndeliv.entity.Rider;
import com.example.foodndeliv.repository.RiderRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service class for managing Rider entities.
 */
@Service
public class RiderService {

    private static final Logger logger = LoggerFactory.getLogger(RiderService.class);

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Creates a new rider.
     * @param requestDTO DTO containing new rider data.
     * @return DTO of the created rider.
     * @throws IllegalArgumentException if phone number already exists.
     */
    @Transactional
    public RiderResponseDTO createRider(CreateRiderRequestDTO requestDTO) {
        logger.info("Attempting to create new rider with phone number: {}", requestDTO.getPhoneNumber());

        riderRepository.findByPhoneNumber(requestDTO.getPhoneNumber()).ifPresent(existingRider -> {
            logger.warn("Attempt to create rider with existing phone number: {}", requestDTO.getPhoneNumber());
            throw new IllegalArgumentException("Rider with phone number '" + requestDTO.getPhoneNumber() + "' already exists.");
        });

        Rider rider = modelMapper.map(requestDTO, Rider.class);
        Rider savedRider = riderRepository.save(rider);
        logger.info("Rider created successfully with ID: {}", savedRider.getId());

        return modelMapper.map(savedRider, RiderResponseDTO.class);
    }

    /**
     * Retrieves a rider by their ID.
     * @param riderId The ID of the rider to retrieve.
     * @return DTO of the found rider.
     * @throws NoSuchElementException if no rider is found with the given ID.
     */
    @Transactional(readOnly = true)
    public RiderResponseDTO getRiderById(Long riderId) {
        logger.info("Fetching rider with ID: {}", riderId);
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> {
                    logger.warn("Rider not found with ID: {}", riderId);
                    return new NoSuchElementException("Rider not found with ID: " + riderId);
                });
        return modelMapper.map(rider, RiderResponseDTO.class);
    }

    /**
     * Retrieves all riders.
     * @return A list of RiderResponseDTOs.
     */
    @Transactional(readOnly = true)
    public List<RiderResponseDTO> getAllRiders() {
        logger.info("Fetching all riders");
        return riderRepository.findAll().stream()
                .map(rider -> modelMapper.map(rider, RiderResponseDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing rider.
     * @param riderId The ID of the rider to update.
     * @param requestDTO DTO containing updated rider data. Fields that are null will be ignored.
     * @return DTO of the updated rider.
     * @throws NoSuchElementException if no rider is found with the given ID.
     * @throws IllegalArgumentException if trying to update to an existing phone number (of another rider).
     */
    @Transactional
    public RiderResponseDTO updateRider(Long riderId, UpdateRiderRequestDTO requestDTO) {
        logger.info("Attempting to update rider with ID: {}", riderId);
        Rider rider = riderRepository.findById(riderId)
                .orElseThrow(() -> {
                    logger.warn("Rider not found with ID: {} for update.", riderId);
                    return new NoSuchElementException("Rider not found with ID: " + riderId);
                });

        // Check for phone number conflict if phone number is being changed
        if (requestDTO.getPhoneNumber() != null && !requestDTO.getPhoneNumber().equals(rider.getPhoneNumber())) {
            riderRepository.findByPhoneNumber(requestDTO.getPhoneNumber()).ifPresent(existingRider -> {
                if (!existingRider.getId().equals(riderId)) { // If it's a different rider
                    logger.warn("Attempt to update rider ID {} with phone number {} which already exists for rider ID {}",
                            riderId, requestDTO.getPhoneNumber(), existingRider.getId());
                    throw new IllegalArgumentException("Phone number '" + requestDTO.getPhoneNumber() + "' is already in use by another rider.");
                }
            });
            rider.setPhoneNumber(requestDTO.getPhoneNumber());
        }

        if (requestDTO.getName() != null) {
            rider.setName(requestDTO.getName());
        }
        if (requestDTO.getVehicleDetails() != null) {
            rider.setVehicleDetails(requestDTO.getVehicleDetails());
        }
        if (requestDTO.getStatus() != null) {
            rider.setStatus(requestDTO.getStatus());
        }

        Rider updatedRider = riderRepository.save(rider);
        logger.info("Rider with ID: {} updated successfully.", updatedRider.getId());
        return modelMapper.map(updatedRider, RiderResponseDTO.class);
    }

    /**
     * Deletes a rider by their ID.
     * @param riderId The ID of the rider to delete.
     * @throws NoSuchElementException if no rider is found with the given ID.
     */
    @Transactional
    public void deleteRider(Long riderId) {
        logger.info("Attempting to delete rider with ID: {}", riderId);
        if (!riderRepository.existsById(riderId)) {
            logger.warn("Rider with ID: {} not found for deletion.", riderId);
            throw new NoSuchElementException("Rider not found with ID: " + riderId);
        }

        // hard delete
        riderRepository.deleteById(riderId);
        logger.info("Rider with ID: {} deleted successfully.", riderId);
    }
}
