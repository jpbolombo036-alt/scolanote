package com.bulletin.service.admin.impl;

import com.bulletin.entity.Enrollment;
import com.bulletin.repository.EnrollmentRepository;
import com.bulletin.repository.StudentRepository;
import com.bulletin.repository.TeacherRepository;
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
    private final TeacherRepository teacherRepository;

    // In-memory records for exports/backups (lightweight runtime store)
    private final List<Map<String, Object>> exports = new CopyOnWriteArrayList<>();
    private final List<Map<String, Object>> backups = new CopyOnWriteArrayList<>();

    @Override
    public List<Map<String, Object>> listReports() {
        List<Map<String, Object>> reports = new ArrayList<>();
        reports.add(Map.of("key", "eleves-par-niveau", "label", "Élèves par niveau"));
        reports.add(Map.of("key", "eleves-par-classe", "label", "Élèves par classe"));
        reports.add(Map.of("key", "inscriptions", "label", "Inscriptions en cours"));
        return reports;
    }

    @Override
    public AdminDataService.ReportFile generateReport(String reportKey) {
        if ("eleves-par-niveau".equals(reportKey)) {
            return buildEnrollmentReport("niveau;classe;matricule;nom;postnom;prenom;school", reportKey, enrollment -> {
                String niveau = enrollment.getClassroom() != null && enrollment.getClassroom().getLevel() != null ?
                        enrollment.getClassroom().getLevel().getNom() : "-";
                String classe = enrollment.getClassroom() != null ? enrollment.getClassroom().getNom() : "-";
                return List.of(
                        escape(niveau),
                        escape(classe),
                        escape(getStudentField(enrollment, s -> s.getMatricule())),
                        escape(getStudentField(enrollment, s -> s.getNom())),
                        escape(getStudentField(enrollment, s -> s.getPostnom())),
                        escape(getStudentField(enrollment, s -> s.getPrenom())),
                        escape(enrollment.getSchool() != null ? enrollment.getSchool().getNom() : "-")
                );
            });
        }

        if ("eleves-par-classe".equals(reportKey)) {
            return buildEnrollmentReport("classe;nategorie;matricule;nom;postnom;prenom;school", reportKey, enrollment -> {
                String classe = enrollment.getClassroom() != null ? enrollment.getClassroom().getNom() : "-";
                String niveau = enrollment.getClassroom() != null && enrollment.getClassroom().getLevel() != null ?
                        enrollment.getClassroom().getLevel().getNom() : "-";
                return List.of(
                        escape(classe),
                        escape(niveau),
                        escape(getStudentField(enrollment, s -> s.getMatricule())),
                        escape(getStudentField(enrollment, s -> s.getNom())),
                        escape(getStudentField(enrollment, s -> s.getPostnom())),
                        escape(getStudentField(enrollment, s -> s.getPrenom())),
                        escape(enrollment.getSchool() != null ? enrollment.getSchool().getNom() : "-")
                );
            });
        }

        if ("inscriptions".equals(reportKey)) {
            return buildEnrollmentReport("date_inscription;classe;nom;prenom;etat", reportKey, enrollment -> {
                return List.of(
                        escape(enrollment.getDateInscription() != null ? enrollment.getDateInscription().toString() : "-"),
                        escape(enrollment.getClassroom() != null ? enrollment.getClassroom().getNom() : "-"),
                        escape(getStudentField(enrollment, s -> s.getNom())),
                        escape(getStudentField(enrollment, s -> s.getPrenom())),
                        escape(enrollment.getEtat())
                );
            });
        }

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

        String type = params != null && params.get("type") != null ? params.get("type").toString() : "students";
        switch (type) {
            case "enseignants" -> {
                String csv = teacherRepository.findAll().stream()
                        .map(t -> String.join(";",
                                t.getMatricule() == null ? "" : t.getMatricule(),
                                t.getNom() == null ? "" : t.getNom(),
                                t.getPostnom() == null ? "" : t.getPostnom(),
                                t.getPrenom() == null ? "" : t.getPrenom(),
                                t.getEmail() == null ? "" : t.getEmail(),
                                t.getTelephone() == null ? "" : t.getTelephone()))
                        .collect(Collectors.joining("\n"));
                String filename = "enseignants-export-" + id + ".csv";
                entry.put("filename", filename);
                entry.put("content", Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8)));
                entry.put("contentType", "text/csv; charset=utf-8");
            }
            case "inscriptions" -> {
                String csv = enrollmentRepository.findAll().stream()
                        .map(e -> String.join(";",
                                e.getDateInscription() != null ? e.getDateInscription().toString() : "",
                                e.getClassroom() != null ? e.getClassroom().getNom() : "",
                                getStudentField(e, s -> s.getNom()),
                                getStudentField(e, s -> s.getPrenom()),
                                e.getEtat() == null ? "" : e.getEtat()))
                        .collect(Collectors.joining("\n"));
                String filename = "inscriptions-export-" + id + ".csv";
                entry.put("filename", filename);
                entry.put("content", Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8)));
                entry.put("contentType", "text/csv; charset=utf-8");
            }
            default -> {
                String csv = studentRepository.findAll().stream()
                        .map(s -> String.join(";",
                                s.getMatricule() == null ? "" : s.getMatricule(),
                                s.getNom() == null ? "" : s.getNom(),
                                s.getPostnom() == null ? "" : s.getPostnom(),
                                s.getPrenom() == null ? "" : s.getPrenom()))
                        .collect(Collectors.joining("\n"));
                String filename = "students-export-" + id + ".csv";
                entry.put("filename", filename);
                entry.put("content", Base64.getEncoder().encodeToString(csv.getBytes(StandardCharsets.UTF_8)));
                entry.put("contentType", "text/csv; charset=utf-8");
            }
        }

        exports.add(0, entry);
        return Map.of("id", id, "status", "completed", "filename", entry.get("filename"), "type", type);
    }

    @Override
    public List<Map<String, Object>> listExports() {
        return Collections.unmodifiableList(exports);
    }

    @Override
    public AdminDataService.ReportFile downloadExport(String exportId) {
        return exports.stream()
                .filter(entry -> exportId.equals(entry.get("id")))
                .findFirst()
                .map(entry -> {
                    String filename = (String) entry.getOrDefault("filename", "export-" + exportId + ".csv");
                    String contentType = (String) entry.getOrDefault("contentType", "text/csv; charset=utf-8");
                    String contentBase64 = (String) entry.get("content");
                    byte[] content = Base64.getDecoder().decode(contentBase64);
                    return new AdminDataService.ReportFile(content, filename, contentType);
                })
                .orElseThrow(() -> new IllegalArgumentException("Export introuvable: " + exportId));
    }

    @Override
    public Map<String, Object> importData(MultipartFile file) {
        Map<String, Object> out = new HashMap<>();
        out.put("filename", file.getOriginalFilename());
        out.put("size", file.getSize());
        out.put("status", "imported");

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            long rowCount = Arrays.stream(content.split("\r?\n"))
                    .filter(line -> !line.trim().isEmpty())
                    .count();
            out.put("rows", rowCount);
            out.put("preview", Arrays.stream(content.split("\r?\n")).limit(5).collect(Collectors.toList()));
        } catch (IOException e) {
            out.put("status", "failed");
            out.put("error", e.getMessage());
        }

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

    private AdminDataService.ReportFile buildEnrollmentReport(String header, String reportKey, java.util.function.Function<Enrollment, List<String>> rowMapper) {
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append(header).append("\n");
        for (Enrollment enrollment : enrollments) {
            sb.append(String.join(";", rowMapper.apply(enrollment))).append("\n");
        }
        byte[] content = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = reportKey + ".csv";
        String contentType = "text/csv; charset=utf-8";
        return new ReportFile(content, filename, contentType);
    }

    private String getStudentField(Enrollment enrollment, java.util.function.Function<com.bulletin.entity.Student, String> extractor) {
        if (enrollment.getStudent() == null) {
            return "";
        }
        String value = extractor.apply(enrollment.getStudent());
        return value == null ? "" : value;
    }
}
