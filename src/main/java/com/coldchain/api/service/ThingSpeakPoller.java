package com.coldchain.api.service;

import com.coldchain.api.dto.ThingSpeakResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ThingSpeakPoller {

    private final ThingSpeakService thingSpeakService;
    private final SensorService sensorService;

    public ThingSpeakPoller(ThingSpeakService thingSpeakService, SensorService sensorService) {
        this.thingSpeakService = thingSpeakService;
        this.sensorService = sensorService;
    }

    /**
     * Polls ThingSpeak dynamically with a 15-second delay between cycles.
     * FIXED: Swapped 'fixedRate' to 'fixedDelay' to prevent overlapping execution threads
     * if HTTP request handshakes delay due to network latency.
     */
    @Scheduled(fixedDelay = 15000)
    public void pollThingSpeak() {
        try {
            log.debug("Initiating ThingSpeak sync cycle...");
            ThingSpeakResponse response = thingSpeakService.fetchLatest();

            if (response != null && response.getFeeds() != null && !response.getFeeds().isEmpty()) {
                ThingSpeakResponse.Feed latestFeed = response.getFeeds().get(0);

                // Field 6 in your ThingSpeak setup contains the Database Device ID (e.g., 3)
                String deviceIdFromFeed = latestFeed.getField6();

                sensorService.saveReading(latestFeed);

                log.info("Successfully synced latest data for Device ID: {} at {}",
                        deviceIdFromFeed, latestFeed.getCreated_at());
            } else {
                log.warn("ThingSpeak sync check: No new data available in the current window.");
            }
        } catch (Exception e) {
            // Detailed error logging to help debug network or parsing issues
            log.error("Sync Interrupted: {}. Check internet connection or ThingSpeak API limits.", e.getMessage());
        }
    }
}