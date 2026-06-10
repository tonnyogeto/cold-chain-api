package com.coldchain.core.repository;

import com.coldchain.core.model.Device;
import com.coldchain.core.model.SensorReading;
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
     * OPTIMIZED JPQL QUERY:
     * Pulls the absolute freshest entries by tracking the maximum auto-incrementing serial ID
     * grouped by each individual device. Using JOIN FETCH eliminates N+1 query overhead,
     * cleanly hydrates the child object graph, and resolves identical timestamp sorting bugs.
     */
    @Query("SELECT s FROM SensorReading s JOIN FETCH s.device WHERE s.id IN " +
            "(SELECT MAX(r.id) FROM SensorReading r GROUP BY r.device.id)")
    List<SensorReading> findLatestReadingsForAllDevices();

    SensorReading findTopByOrderByRecordedAtDesc();

    boolean existsByRecordedAtAndDevice(LocalDateTime recordedAt, Device device);
}