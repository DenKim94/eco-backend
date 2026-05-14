package eco.backend.main_app.feature.calculation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import eco.backend.main_app.utils.AppConstants;

public record CalculationRequestDto(
        @JsonFormat(pattern = AppConstants.JSON_DATE_PATTERN)
        String endDate
){}