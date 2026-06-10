package com.coldchain.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SensorReadingDto {
    private Integer id;
    private String deviceName;
    private Double temperature;
    private Double humidity;
    private Double latitude;
    private Double longitude;
    private Double distance;
    private Short alertStatus;
    private Short fanStatus;
    private String recordedAt;
    private String boxStatus;
}