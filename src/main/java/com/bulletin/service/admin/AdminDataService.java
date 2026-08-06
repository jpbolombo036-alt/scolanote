package com.bulletin.service.admin;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AdminDataService {
    List<Map<String, Object>> listReports();

    /**
     * Génère un rapport et retourne le contenu/nom/type.
     */
    ReportFile generateReport(String reportKey);

    Map<String, Object> startExport(Map<String, Object> params);

    List<Map<String, Object>> listExports();

    ReportFile downloadExport(String exportId);

    Map<String, Object> importData(MultipartFile file);

    Map<String, Object> createBackup();

    List<Map<String, Object>> listBackups();

    class ReportFile {
        public byte[] content;
        public String filename;
        public String contentType;

        public ReportFile(byte[] content, String filename, String contentType) {
            this.content = content;
            this.filename = filename;
            this.contentType = contentType;
        }
    }
}
