package com.bulletin.service.admin;

import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public interface AdminDataService {
    List<Map<String, Object>> listReports();

    byte[] generateReport(String reportKey);

    Map<String, Object> startExport(Map<String, Object> params);

    List<Map<String, Object>> listExports();

    Map<String, Object> importData(MultipartFile file);

    Map<String, Object> createBackup();

    List<Map<String, Object>> listBackups();
}
