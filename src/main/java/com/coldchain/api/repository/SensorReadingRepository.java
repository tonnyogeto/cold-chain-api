package com.coldchain.api.repository;

import com.coldchain.api.model.Device;
import com.coldchain.api.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    /**
     * Efficiently fetches all readings while pre-loading the associated Device objects.
     * This ensures 'device_name' is available immediately without extra queries.
     */
    @Query("SELECT s FROM SensorReading s JOIN FETCH s.device ORDER BY s.recordedAt DESC")
    List<SensorReading> findAllWithDevices();

    /**
     * Custom Native Query for Fleet Status.
     * Keeps the 'Distinct On' logic for PostgreSQL to get the latest row per device.
     */
    @Query(value = "SELECT DISTINCT ON (device_id) * FROM sensor_readings ORDER BY device_id, recorded_at DESC", nativeQuery = true)
    List<SensorReading> findLatestReadingsForAllDevices();

    SensorReading findTopByOrderByRecordedAtDesc();

    boolean existsByRecordedAtAndDevice(LocalDateTime recordedAt, Device device);
}