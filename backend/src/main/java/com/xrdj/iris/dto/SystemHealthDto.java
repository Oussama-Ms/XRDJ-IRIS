package com.xrdj.iris.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemHealthDto {
    private int activeEngines;
    private int queueCount;
    private int totalErrorsToday;
}
