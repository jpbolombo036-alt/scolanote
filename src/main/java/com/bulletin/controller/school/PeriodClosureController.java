package com.bulletin.controller.school;

import com.bulletin.service.PeriodClosureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/periodes")
@RequiredArgsConstructor
@Tag(name = "Clôture des périodes", description = "Verrouillage et déverrouillage des périodes")
public class PeriodClosureController {

    private final PeriodClosureService periodClosureService;

    @PostMapping("/{id}/verrouiller")
    @Operation(summary = "Verrouiller une période", description = "Verrouille une période pour empêcher les modifications")
    public ResponseEntity<Void> verrouiller(@PathVariable Long id) {
        periodClosureService.verrouillerPeriode(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/deverrouiller")
    @Operation(summary = "Déverrouiller une période", description = "Déverrouille une période pour permettre les modifications")
    public ResponseEntity<Void> deverrouiller(@PathVariable Long id) {
        periodClosureService.deverrouillerPeriode(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
