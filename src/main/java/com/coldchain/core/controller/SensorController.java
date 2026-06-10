package com.coldchain.core.controller;

import com.coldchain.core.dto.SensorReadingDto;
import com.coldchain.core.model.SensorReading;
import com.coldchain.core.repository.SensorReadingRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/readings")
@CrossOrigin(origins = "http://localhost:3000")
public class SensorController {

    private final SensorReadingRepository repository;

    public SensorController(SensorReadingRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/latest-fleet")
    public List<SensorReadingDto> getLatestFleetReadings() {
        return repository.findLatestReadingsForAllDevices()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/latest")
    public SensorReadingDto getLatestReading() {
        SensorReading reading = repository.findTopByOrderByRecordedAtDesc();
        return (reading != null) ? mapToDTO(reading) : null;
    }

    @GetMapping
    public List<SensorReadingDto> getAllReadings() {
        // ✅ Use the JOIN FETCH method to get Device names efficiently
        return repository.findAllWithDevices()
                .stream()
                .limit(100)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Logic to transform the Database Entity into a Frontend-friendly DTO
     * Now correctly maps the DB Device ID and Device Name
     */
    private SensorReadingDto mapToDTO(SensorReading r) {
        Double distance = r.getDistance() != null ? r.getDistance().doubleValue() : null;

        // Business Logic: Determine if the box is empty
        String boxStatus = "UNKNOWN";
        if (distance != null) {
            boxStatus = (distance <= 10.0) ? "ITEM_PRESENT" : "EMPTY";
        }

        String recordedAt = r.getRecordedAt() != null ? r.getRecordedAt().toString() : null;

        // ✅ Extract ID and Name from the associated Device model
        Integer dbDeviceId = (r.getDevice() != null) ? r.getDevice().getId() : null;
        String dbDeviceName = (r.getDevice() != null) ? r.getDevice().getDeviceName() : "Unknown Unit";

        return new SensorReadingDto(
                dbDeviceId,        // id (e.g., 3)
                dbDeviceName,      // deviceName (e.g., "ColdChainTracker_01")
                r.getTemperature() != null ? r.getTemperature().doubleValue() : null,
                r.getHumidity() != null ? r.getHumidity().doubleValue() : null,
                r.getLatitude() != null ? r.getLatitude().doubleValue() : null,
                r.getLongitude() != null ? r.getLongitude().doubleValue() : null,
                distance,
                r.getAlertStatus(),
                r.getFanStatus(),
                recordedAt,
                boxStatus
        );
    }
}