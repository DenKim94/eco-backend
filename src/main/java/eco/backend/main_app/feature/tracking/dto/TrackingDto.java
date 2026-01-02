package eco.backend.main_app.feature.tracking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import eco.backend.main_app.utils.AppConstants;

public record TrackingDto(
        @JsonProperty("value_kWh") Double value_kWh,

        @JsonProperty("date")
        @JsonFormat(pattern = AppConstants.JSON_DATE_PATTERN)
        String date
) {}
