package com.bulletin.controller.admin;

import com.bulletin.service.admin.AdminDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Rapports & Données: rapports, exports, imports, sauvegardes")
public class AdminDataController {

    private final AdminDataService adminDataService;

    @GetMapping("/rapports")
    @Operation(summary = "Liste des rapports disponibles")
    public ResponseEntity<List<Map<String, Object>>> listReports() {
        return ResponseEntity.ok(adminDataService.listReports());
    }

    @GetMapping("/rapports/{reportKey}")
    @Operation(summary = "Générer un rapport")
    public ResponseEntity<byte[]> generateReport(@PathVariable String reportKey) {
        var file = adminDataService.generateReport(reportKey);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType(file.contentType));
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename(file.filename).build());
        headers.setContentLength(file.content.length);
        return ResponseEntity.ok().headers(headers).body(file.content);
    }

    @PostMapping("/exports")
    @Operation(summary = "Lancer un export de données")
    public ResponseEntity<Map<String, Object>> startExport(@RequestBody Map<String, Object> params) {
        return ResponseEntity.status(201).body(adminDataService.startExport(params));
    }

    @GetMapping("/exports")
    public ResponseEntity<List<Map<String, Object>>> listExports() {
        return ResponseEntity.ok(adminDataService.listExports());
    }

    @PostMapping("/imports")
    public ResponseEntity<Map<String, Object>> uploadImport(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(adminDataService.importData(file));
    }

    @PostMapping("/sauvegardes")
    public ResponseEntity<Map<String, Object>> createBackup() {
        return ResponseEntity.status(201).body(adminDataService.createBackup());
    }

    @GetMapping("/sauvegardes")
    public ResponseEntity<List<Map<String, Object>>> listBackups() {
        return ResponseEntity.ok(adminDataService.listBackups());
    }
}
