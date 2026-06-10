package com.coldchain.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    // ✅ UPDATED: Added FetchType.EAGER to ensure the Device relationship
    // is fully populated during native SQL execution for the fleet overview.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="device_id")
    @JsonIgnore
    private Device device;

    @Column(name="temperature")
    private BigDecimal temperature;

    @Column(name="latitude")
    private BigDecimal latitude;

    @Column(name="longitude")
    private BigDecimal longitude;

    @Column(name="alert_status")
    private Short alertStatus;

    @Column(name="fan_status")
    private Short fanStatus;

    @Column(name="recorded_at")
    private LocalDateTime recordedAt;

    @Column(name="inserted_at", insertable = false, updatable = false)
    private LocalDateTime insertedAt;

    @Column(name="humidity")
    private BigDecimal humidity;

    @Column(name="distance")
    private BigDecimal distance;
}