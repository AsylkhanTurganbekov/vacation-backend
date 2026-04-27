package com.company.vacation.dto.monitoring;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonitoringMapResponse {
    private List<MonitoringMapPointResponse> withCoordinates;
    private List<MonitoringMapPointResponse> withoutCoordinates;
}
