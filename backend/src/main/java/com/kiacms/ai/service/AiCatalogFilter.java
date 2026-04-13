package com.kiacms.ai.service;

import com.kiacms.course.entity.Course;

public final class AiCatalogFilter {

    private AiCatalogFilter() {
    }

    public static boolean isUsableCourse(Course course) {
        if (course == null || course.getTitle() == null || course.getTitle().isBlank()) {
            return false;
        }

        return !looksCorrupted(course.getTitle())
                && !looksCorrupted(course.getTrackName())
                && !looksCorrupted(course.getDescription());
    }

    private static boolean looksCorrupted(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = value.trim();
        int questionCount = 0;
        for (char character : normalized.toCharArray()) {
            if (character == '?' || character == '\uFFFD') {
                questionCount++;
            }
        }

        if (normalized.contains("???") || normalized.contains("\uFFFD")) {
            return true;
        }

        return questionCount >= 2 && ((double) questionCount / normalized.length()) >= 0.18d;
    }
}
