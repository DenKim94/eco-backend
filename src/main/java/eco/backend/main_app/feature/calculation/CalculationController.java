package eco.backend.main_app.feature.calculation;


import eco.backend.main_app.feature.calculation.dto.CalculationRequestDto;
import eco.backend.main_app.feature.calculation.dto.CalculationResultsDto;
import eco.backend.main_app.feature.calculation.model.CalculationEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/calculation")
public class CalculationController {

    private final CalculationService calculationService;

    public CalculationController(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    /**
     * PUT /api/calculation/run-and-save: Führt die Berechnung aus und speichert die Ergebnisse
     */
    @PostMapping("/run-and-save")
    public ResponseEntity<CalculationResultsDto> runCalculation(@AuthenticationPrincipal UserDetails userDetails, @RequestBody CalculationRequestDto dto) {
        CalculationResultsDto results = calculationService.runCalculation(userDetails.getUsername(), dto);
        calculationService.saveResultsInEntity(userDetails.getUsername(), results);

        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/calculation/get-results: Lädt die gespeicherten Daten
     */
    @GetMapping("/get-results")
    public ResponseEntity<?> loadSavedResults( @AuthenticationPrincipal UserDetails userDetails ) {
        List<CalculationEntity> history = calculationService.getHistory(userDetails.getUsername());

        if (history.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No data found.");
        }

        return ResponseEntity.ok(history);
    }
}

