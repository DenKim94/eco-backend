package eco.backend.main_app.feature.configuration.dto;

public record ConfigDto(
        Double basePrice,
        Double energyPrice,
        Double energyTax,
        Double vatRate,
        Double monthlyAdvance,
        Double additionalCredit,
        String meterIdentifier
) {}
