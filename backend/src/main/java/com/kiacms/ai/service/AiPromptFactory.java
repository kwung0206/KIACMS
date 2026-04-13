package com.kiacms.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiacms.ai.dto.request.AiChatbotMessageRequest;
import com.kiacms.ai.dto.request.CareerCourseRecommendationRequest;
import com.kiacms.ai.dto.request.ProjectPlanInsightRequest;
import com.kiacms.ai.dto.request.SimilarProjectRecommendationRequest;
import com.kiacms.course.entity.Course;
import com.kiacms.note.entity.Note;
import com.kiacms.project.entity.ProjectPosition;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiPromptFactory {

    private final ObjectMapper objectMapper;

    public String buildNoteSummarySystemPrompt() {
        return """
                You are KIACMS AI, an educational assistant for academy students.
                Summarize study notes in Korean.
                Use only the facts that appear in the provided note.
                Do not invent course content that is not present.
                If a concept is unclear or missing, state it cautiously.
                Return concise, practical outputs for review and instructor follow-up.
                """;
    }

    public String buildNoteSummaryUserPrompt(Note note) {
        String sessionText = note.getCourseSession() == null
                ? "과정 전체 정리"
                : "%d회차 - %s".formatted(note.getCourseSession().getSessionOrder(), note.getCourseSession().getTitle());

        return """
                Analyze the following KIACMS note and summarize it in Korean.

                [Note metadata]
                Course: %s
                Session: %s
                Title: %s

                [Note body]
                %s
                """.formatted(
                note.getCourse().getTitle(),
                sessionText,
                note.getTitle(),
                note.getContent()
        );
    }

    public String buildCareerCourseRecommendationSystemPrompt() {
        return """
                You are KIACMS AI, a career-to-course recommendation assistant.
                Recommend only from the provided course catalog.
                Never invent course IDs or titles.
                Rank the best matches first.
                Explain recommendations in Korean with direct relevance to the user's career goal.
                Prefer specific, practical reasoning over generic praise.
                """;
    }

    public String buildCareerCourseRecommendationUserPrompt(
            CareerCourseRecommendationRequest request,
            List<Course> courses
    ) {
        List<Map<String, Object>> courseCatalog = courses.stream()
                .map(course -> Map.<String, Object>of(
                        "courseId", course.getId(),
                        "courseCode", course.getCourseCode(),
                        "courseTitle", course.getTitle(),
                        "trackName", safeString(course.getTrackName()),
                        "description", safeString(shorten(course.getDescription(), 500)),
                        "status", course.getStatus().name(),
                        "startDate", course.getStartDate(),
                        "endDate", course.getEndDate()
                ))
                .toList();

        return """
                Recommend the most suitable KIACMS courses for the user's career goal.
                Reply only with structured data that matches the schema.
                Maximum recommendation count: %d

                [Career goal]
                %s

                [Course catalog JSON]
                %s
                """.formatted(
                request.limit() == null ? 3 : request.limit(),
                request.careerGoal(),
                toJson(courseCatalog)
        );
    }

    public String buildSimilarProjectRecommendationSystemPrompt() {
        return """
                You are KIACMS AI, a project similarity recommender.
                Compare the user's project idea against the provided project board data only.
                Never invent project IDs, titles, or positions.
                Recommend the most relevant existing project posts and suggest the best-fit position for each.
                Explain similarity in Korean with concrete overlap in goal, stack, or problem domain.
                """;
    }

    public String buildSimilarProjectRecommendationUserPrompt(
            SimilarProjectRecommendationRequest request,
            List<ProjectContext> projects
    ) {
        List<Map<String, Object>> catalog = projects.stream()
                .map(project -> Map.<String, Object>of(
                        "projectPostId", project.projectPost().getId(),
                        "title", project.projectPost().getTitle(),
                        "description", safeString(shorten(project.projectPost().getDescription(), 500)),
                        "goal", safeString(shorten(project.projectPost().getGoal(), 300)),
                        "techStack", safeString(project.projectPost().getTechStack()),
                        "positions", project.positions().stream()
                                .map(position -> Map.<String, Object>of(
                                        "name", position.getName(),
                                        "requiredSkills", safeString(shorten(position.getRequiredSkills(), 250)),
                                        "description", safeString(shorten(position.getDescription(), 250))
                                ))
                                .toList()
                ))
                .toList();

        return """
                Recommend the most similar projects from the existing KIACMS project board.
                Reply only with structured data that matches the schema.
                Maximum recommendation count: %d

                [User project idea]
                %s

                [Existing project catalog JSON]
                %s
                """.formatted(
                request.limit() == null ? 3 : request.limit(),
                request.projectDescription(),
                toJson(catalog)
        );
    }

    public String buildSiteChatbotSystemPrompt() {
        return """
                You are KIACMS Copilot, a Korean site-specific assistant for the KIACMS academy platform.
                Your job is not general trivia. You help users understand KIACMS features, role-based menus,
                project flows, note flows, approvals, class schedules, and AI recommendation features.

                Rules:
                - Always answer in natural Korean.
                - Use only the provided site context, route catalog, course catalog, and project catalog.
                - Never invent menu paths, route keys, course IDs, or project IDs.
                - Mentor in KIACMS is a student-management administrator, not a project mentor.
                - If the user's role cannot perform an action, explain the correct role and route clearly.
                - Keep the answer practical and action-oriented.
                - When useful, recommend exact route keys from the provided catalog.
                - When useful, recommend courses or projects only from the provided catalogs.
                - If the user pasted a project idea or plan, extract helpful keywords and use them for analysis.
                - Follow the response schema strictly. Use empty arrays instead of null.
                """;
    }

    public String buildSiteChatbotUserPrompt(
            AiChatbotMessageRequest request,
            User requester,
            List<SiteRouteContext> routes,
            List<Course> courses,
            List<ProjectContext> projects
    ) {
        List<Map<String, Object>> routeCatalog = routes.stream()
                .map(route -> Map.<String, Object>of(
                        "routeKey", route.routeKey(),
                        "label", route.label(),
                        "url", route.url(),
                        "description", route.description()
                ))
                .toList();

        List<Map<String, Object>> courseCatalog = courses.stream()
                .map(course -> Map.<String, Object>of(
                        "courseId", course.getId(),
                        "courseCode", course.getCourseCode(),
                        "courseTitle", course.getTitle(),
                        "trackName", safeString(course.getTrackName()),
                        "description", safeString(shorten(course.getDescription(), 320)),
                        "status", course.getStatus().name(),
                        "startDate", course.getStartDate(),
                        "endDate", course.getEndDate()
                ))
                .toList();

        List<Map<String, Object>> projectCatalog = projects.stream()
                .map(project -> Map.<String, Object>of(
                        "projectPostId", project.projectPost().getId(),
                        "title", project.projectPost().getTitle(),
                        "ownerName", project.projectPost().getOwner().getName(),
                        "description", safeString(shorten(project.projectPost().getDescription(), 280)),
                        "goal", safeString(shorten(project.projectPost().getGoal(), 220)),
                        "techStack", safeString(project.projectPost().getTechStack()),
                        "positions", project.positions().stream()
                                .map(position -> Map.<String, Object>of(
                                        "name", position.getName(),
                                        "requiredSkills", safeString(shorten(position.getRequiredSkills(), 160))
                                ))
                                .toList()
                ))
                .toList();

        return """
                Respond to the user as the KIACMS site assistant.

                [Requester]
                name: %s
                role: %s
                currentPath: %s

                [Role guide]
                STUDENT: class calendar, notes, note AI summary, project board, applications, own project posts.
                INSTRUCTOR: managed sessions, tagged notes, project support history.
                MENTOR: managed students, student-course mapping, student management. Not a project participant.
                ROOT: signup approvals, class schedule management, project deletion moderation.

                [Route catalog JSON]
                %s

                [Course catalog JSON]
                %s

                [Project catalog JSON]
                %s

                [Recent conversation]
                %s

                [User question]
                %s
                """.formatted(
                requester.getName(),
                requester.getRoleType().name(),
                safeString(request.currentPath()),
                toJson(routeCatalog),
                toJson(courseCatalog),
                toJson(projectCatalog),
                historyToText(request.history()),
                request.message()
        );
    }

    public String buildProjectPlanInsightSystemPrompt() {
        return """
                You are KIACMS Copilot, an assistant that analyzes a project plan and connects it to the KIACMS site.
                Analyze the provided project idea or plan in Korean.
                Extract concrete keywords, recommend the most relevant KIACMS courses, and find similar open project posts.
                Never invent course IDs, project IDs, or titles.
                The notification draft should be short, useful, and suitable for an in-site notification.
                Follow the response schema strictly. Use empty arrays instead of null.
                """;
    }

    public String buildProjectPlanInsightUserPrompt(
            ProjectPlanInsightRequest request,
            String resolvedProjectTitle,
            String resolvedProjectPlanText,
            List<Course> courses,
            List<ProjectContext> projects
    ) {
        int limit = request.limit() == null ? 3 : request.limit();

        List<Map<String, Object>> courseCatalog = courses.stream()
                .map(course -> Map.<String, Object>of(
                        "courseId", course.getId(),
                        "courseCode", course.getCourseCode(),
                        "courseTitle", course.getTitle(),
                        "trackName", safeString(course.getTrackName()),
                        "description", safeString(shorten(course.getDescription(), 360))
                ))
                .toList();

        List<Map<String, Object>> projectCatalog = projects.stream()
                .map(project -> Map.<String, Object>of(
                        "projectPostId", project.projectPost().getId(),
                        "title", project.projectPost().getTitle(),
                        "description", safeString(shorten(project.projectPost().getDescription(), 280)),
                        "goal", safeString(shorten(project.projectPost().getGoal(), 220)),
                        "techStack", safeString(project.projectPost().getTechStack()),
                        "positions", project.positions().stream()
                                .map(position -> Map.<String, Object>of(
                                        "name", position.getName(),
                                        "requiredSkills", safeString(shorten(position.getRequiredSkills(), 160))
                                ))
                                .toList()
                ))
                .toList();

        return """
                Analyze the following KIACMS project plan and produce structured recommendations.
                Maximum recommendation count per category: %d

                [Project title]
                %s

                [Project plan text]
                %s

                [Course catalog JSON]
                %s

                [Project board catalog JSON]
                %s
                """.formatted(
                limit,
                safeString(resolvedProjectTitle),
                resolvedProjectPlanText,
                toJson(courseCatalog),
                toJson(projectCatalog)
        );
    }

    public record ProjectContext(
            ProjectPost projectPost,
            List<ProjectPosition> positions
    ) {
    }

    public record SiteRouteContext(
            String routeKey,
            String label,
            String url,
            String description
    ) {
    }

    private String historyToText(List<AiChatbotMessageRequest.ChatHistoryMessageRequest> history) {
        if (history == null || history.isEmpty()) {
            return "(no previous conversation)";
        }

        return history.stream()
                .limit(8)
                .map(item -> "%s: %s".formatted(item.role(), shorten(item.content(), 300)))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("(no previous conversation)");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize AI prompt context.", exception);
        }
    }

    private String shorten(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
