package com.bulletin.controller.tracking;

import com.bulletin.dto.tracking.AttendanceRequest;
import com.bulletin.dto.tracking.AttendanceResponse;
import com.bulletin.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name = "Présences", description = "Suivi des absences et retards")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @Operation(summary = "Créer une présence", description = "Enregistre une présence/absence/retard")
    public ResponseEntity<AttendanceResponse> createAttendance(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.createAttendance(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Présence par ID", description = "Retourne une présence par son ID")
    public ResponseEntity<AttendanceResponse> getAttendance(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendance(id));
    }

    @GetMapping
    @Operation(summary = "Liste des présences", description = "Retourne toutes les présences")
    public ResponseEntity<List<AttendanceResponse>> getAllAttendances() {
        return ResponseEntity.ok(attendanceService.getAllAttendances());
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Présences par élève", description = "Retourne les présences d'un élève")
    public ResponseEntity<List<AttendanceResponse>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getByStudent(studentId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une présence", description = "Modifie une présence")
    public ResponseEntity<AttendanceResponse> updateAttendance(@PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.updateAttendance(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une présence", description = "Supprime une présence")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.noContent().build();
    }
}
