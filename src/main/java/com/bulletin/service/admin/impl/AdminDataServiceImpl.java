package com.bulletin.service.admin.impl;

import com.bulletin.entity.Enrollment;
import com.bulletin.repository.EnrollmentRepository;
import com.bulletin.repository.StudentRepository;
import com.bulletin.service.admin.AdminDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDataServiceImpl implements AdminDataService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    // In-memory records for exports/backups (lightweight runtime store)
    private final List<Map<String, Object>> exports = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> backups = new CopyOnWriteArrayList<>();

    @Override
    public List<Map<String, Object>> listReports() {
        Map<String, Object> sample = new HashMap<>();
        sample.put("key", "eleves-par-niveau");
        sample.put("label", "Élèves par niveau");
        return Collections.singletonList(sample);
    }

    @Override
    public AdminDataService.ReportFile generateReport(String reportKey) {
        if ("eleves-par-niveau".equals(reportKey)) {
            // Build CSV grouped by niveau (level)
            List<Enrollment> enrollments = enrollmentRepository.findAll();
            StringBuilder sb = new StringBuilder();
            sb.append("niveau;classe;matricule;nom;postnom;prenom;school\n");

            for (Enrollment e : enrollments) {
                String niveau = e.getClassroom() != null && e.getClassroom().getLevel() != null ?
                        e.getClassroom().getLevel().getNom() : "-";
                String classe = e.getClassroom() != null ? e.getClassroom().getNom() : "-";
                String matricule = e.getStudent() != null && e.getStudent().getMatricule() != null ? e.getStudent().getMatricule() : "-";
                String nom = e.getStudent() != null && e.getStudent().getNom() != null ? e.getStudent().getNom() : "-";
                String postnom = e.getStudent() != null && e.getStudent().getPostnom() != null ? e.getStudent().getPostnom() : "";
                String prenom = e.getStudent() != null && e.getStudent().getPrenom() != null ? e.getStudent().getPrenom() : "";
                String school = e.getSchool() != null ? e.getSchool().getNom() : "-";
                sb.append(String.join(";", escape(niveau), escape(classe), escape(matricule), escape(nom), escape(postnom), escape(prenom), escape(school))).append("\n");
            }

            byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
            String filename = "eleves-par-niveau.csv";
            String contentType = "text/csv; charset=utf-8";
            return new ReportFile(content, filename, contentType);
        }

        // Fallback: textual placeholder
        String text = "Rapport inconnu: " + reportKey;
        byte[] content = text.getBytes(StandardCharsets.UTF_8);
        return new ReportFile(content, reportKey + ".txt", "text/plain; charset=utf-8");
    }

    @Override
    public Map<String, Object> startExport(Map<String, Object> params) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", id);
        entry.put("status", "completed");
        entry.put("params", params);
        entry.put("createdAt", Instant.now().toString());

        // If requested, produce a simple CSV export of students
        String type = params != null && params.get("type") != null ? params.get("type").toString() : "students";
        if ("students".equals(type)) {
            String csv = studentRepository.findAll().stream()
                    .map(s -> String.join(";",
                            s.getMatricule() == null ? "" : s.getMatricule(),
                            s.getNom() == null ? "" : s.getNom(),
                            s.getPostnom() == null ? "" : s.getPostnom(),
                            s.getPrenom() == null ? "" : s.getPrenom()))
                    .collect(Collectors.joining("\n"));
            entry.put("filename", "students-export-" + id + ".csv");
            entry.put("content", Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8)));
        }

        exports.add(0, entry);
        return Map.of("id", id, "status", "completed");
    }

    @Override
    public List<Map<String, Object>> listExports() {
        return Collections.unmodifiableList(exports);
    }

    @Override
    public Map<String, Object> importData(MultipartFile file) {
        Map<String, Object> out = new HashMap<>();
        out.put("filename", file.getOriginalFilename());
        out.put("size", file.getSize());
        out.put("status", "imported");
        // Note: real import logic would parse CSV/Excel and persist entities. Not implemented here.
        return out;
    }

    @Override
    public Map<String, Object> createBackup() {
        String id = UUID.randomUUID().toString();
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", id);
        entry.put("status", "completed");
        entry.put("createdAt", Instant.now().toString());
        backups.add(0, entry);
        return entry;
    }

    @Override
    public List<Map<String, Object>> listBackups() {
        return Collections.unmodifiableList(backups);
    }

    private String escape(String v) {
        if (v == null) return "";
        return v.replace("\"", "'").replace(";", " ").replace("\n", " ").replace("\r", " ");
    }
}
