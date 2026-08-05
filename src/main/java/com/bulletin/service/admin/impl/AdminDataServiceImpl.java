package com.bulletin.service.admin.impl;

import com.bulletin.service.admin.AdminDataService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AdminDataServiceImpl implements AdminDataService {

    @Override
    public List<Map<String, Object>> listReports() {
        Map<String, Object> sample = new HashMap<>();
        sample.put("key", "eleves-par-niveau");
        sample.put("label", "Élèves par niveau");
        return Collections.singletonList(sample);
    }

    @Override
    public byte[] generateReport(String reportKey) {
        // Stub: returns a tiny PDF-like placeholder or simple bytes
        String text = "Rapport: " + reportKey + "\nGénéré (placeholder).";
        return text.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Map<String, Object> startExport(Map<String, Object> params) {
        Map<String, Object> out = new HashMap<>();
        out.put("id", UUID.randomUUID().toString());
        out.put("status", "started");
        out.put("params", params);
        return out;
    }

    @Override
    public List<Map<String, Object>> listExports() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> importData(MultipartFile file) {
        Map<String, Object> out = new HashMap<>();
        out.put("filename", file.getOriginalFilename());
        out.put("status", "imported (stub)");
        return out;
    }

    @Override
    public Map<String, Object> createBackup() {
        Map<String, Object> out = new HashMap<>();
        out.put("id", UUID.randomUUID().toString());
        out.put("status", "completed");
        return out;
    }

    @Override
    public List<Map<String, Object>> listBackups() {
        return Collections.emptyList();
    }
}
