package eco.backend.main_app.feature.configurations.dto;

public record ConfigDto(
        Double basePrice,
        Double energyPrice,
        Double energyTax,
        Double vatRate,
        Double monthlyAdvance,
        Double additionalCredit,
        String meterIdentifier
) {}
