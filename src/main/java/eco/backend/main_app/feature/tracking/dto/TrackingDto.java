package eco.backend.main_app.feature.tracking.dto;

import java.time.LocalDateTime;

public record TrackingDto(Double value_kWh, LocalDateTime date) {}
