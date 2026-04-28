package com.coldchain.api.service;

import com.coldchain.api.dto.ThingSpeakResponse;
import com.coldchain.api.model.Device;
import com.coldchain.api.model.SensorReading;
import com.coldchain.api.repository.DeviceRepository;
import com.coldchain.api.repository.SensorReadingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Slf4j
@Service
public class SensorService {

    @Autowired
    private SensorReadingRepository sensorReadingRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    /**
     * Processes a single feed from ThingSpeak, checks for duplicates,
     * and persists it to the database.
     */
    @Transactional
    public void saveReading(ThingSpeakResponse.Feed feed) {
        // 1. Extract and Validate Device
        Integer deviceId = parseSafeInt(feed.getField6());
        if (deviceId == null) {
            log.warn("Skipping feed: Device ID is missing or invalid in field6");
            return;
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device with ID " + deviceId + " not found in database"));

        // 2. Parse Timestamp and check for duplicates
        LocalDateTime recordedAt = OffsetDateTime.parse(feed.getCreated_at()).toLocalDateTime();

        if (sensorReadingRepository.existsByRecordedAtAndDevice(recordedAt, device)) {
            log.info("Duplicate record detected for device {} at {}. Skipping.", deviceId, recordedAt);
            return;
        }

        // 3. Map Feed to Entity
        SensorReading reading = new SensorReading();
        reading.setDevice(device);
        reading.setRecordedAt(recordedAt);

        // Map numeric fields
        reading.setTemperature(parseSafeDecimal(feed.getField1()));
        reading.setHumidity(parseSafeDecimal(feed.getField2()));
        reading.setLatitude(parseSafeDecimal(feed.getField3()));
        reading.setLongitude(parseSafeDecimal(feed.getField4()));
        reading.setDistance(parseSafeDecimal(feed.getField5()));

        // Handle Alert Status (Field 7)
        reading.setAlertStatus(parseSafeShort(feed.getField7(), (short) 0));

        // ✅ NEW: Map Fan Status (Field 8) to the Entity
        reading.setFanStatus(parseSafeShort(feed.getField8(), (short) 0));

        // 4. Persist
        sensorReadingRepository.save(reading);
        log.info("Successfully saved reading for Device ID: {} (Fan: {}) at {}",
                deviceId, feed.getField8(), recordedAt);
    }

    // --- Helper Methods ---

    private BigDecimal parseSafeDecimal(String value) {
        try {
            return (value != null && !value.isBlank()) ? new BigDecimal(value) : null;
        } catch (NumberFormatException e) {
            log.error("Failed to parse BigDecimal from value: {}", value);
            return null;
        }
    }

    private Integer parseSafeInt(String value) {
        try {
            return (value != null && !value.isBlank()) ? Integer.parseInt(value.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Short parseSafeShort(String value, short defaultValue) {
        try {
            return (value != null && !value.isBlank()) ? Short.parseShort(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}