package com.bulletin.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Stats stats;
    private List<BulletinItem> recentBulletins;
    private MentionDistribution mentions;
    private List<ActivityItem> activities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private Long students;
        private Long classrooms;
        private Long reportCards;
        private BigDecimal average;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulletinItem {
        private Long id;
        private String student;
        private String classe;
        private String trimestre;
        private BigDecimal moyenne;
        private String mention;
        private String date;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MentionDistribution {
        private Long tresBien;
        private Long bien;
        private Long assezBien;
        private Long passable;
        private Long insuffisant;
        private Long total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private String text;
        private String time;
        private String type;
    }
}
