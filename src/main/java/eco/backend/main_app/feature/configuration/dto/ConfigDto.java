package eco.backend.main_app.feature.configuration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import eco.backend.main_app.utils.AppConstants;

public record ConfigDto(
        Double basePrice,
        Double energyPrice,
        Double energyTax,
        Double vatRate,
        Double monthlyAdvance,
        Double additionalCredit,
        Integer dueDate,
        Integer sepaProcessingDays,
        String meterIdentifier,
        @JsonProperty("reference_date")
        @JsonFormat(pattern = AppConstants.JSON_DATE_PATTERN)
        String referenceDate
) {}
