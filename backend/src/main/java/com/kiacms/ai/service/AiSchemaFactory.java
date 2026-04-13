package com.kiacms.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiSchemaFactory {

    private final ObjectMapper objectMapper;

    public JsonNode noteSummarySchema() {
        return parseSchema("""
                {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["coreConceptSummary", "reviewPoints", "questionPoints", "easyToMissConcepts"],
                  "properties": {
                    "coreConceptSummary": { "type": "string" },
                    "reviewPoints": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "questionPoints": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "easyToMissConcepts": {
                      "type": "array",
                      "items": { "type": "string" }
                    }
                  }
                }
                """);
    }

    public JsonNode careerCourseRecommendationSchema() {
        return parseSchema("""
                {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["careerGoal", "recommendations"],
                  "properties": {
                    "careerGoal": { "type": "string" },
                    "recommendations": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "additionalProperties": false,
                        "required": ["courseId", "courseCode", "courseTitle", "trackName", "recommendedRank", "reason"],
                        "properties": {
                          "courseId": { "type": "string" },
                          "courseCode": { "type": "string" },
                          "courseTitle": { "type": "string" },
                          "trackName": { "type": "string" },
                          "recommendedRank": { "type": "integer" },
                          "reason": { "type": "string" }
                        }
                      }
                    }
                  }
                }
                """);
    }

    public JsonNode similarProjectRecommendationSchema() {
        return parseSchema("""
                {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["projectIdea", "recommendations"],
                  "properties": {
                    "projectIdea": { "type": "string" },
                    "recommendations": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "additionalProperties": false,
                        "required": ["projectPostId", "title", "similarityReason", "recommendedPosition"],
                        "properties": {
                          "projectPostId": { "type": "string" },
                          "title": { "type": "string" },
                          "similarityReason": { "type": "string" },
                          "recommendedPosition": { "type": "string" }
                        }
                      }
                    }
                  }
                }
                """);
    }

    public JsonNode siteChatbotSchema() {
        return parseSchema("""
                {
                  "type": "object",
                  "additionalProperties": false,
                  "required": [
                    "intentType",
                    "answer",
                    "keywords",
                    "suggestedRouteKeys",
                    "followUpQuestions",
                    "recommendedCourses",
                    "recommendedProjects"
                  ],
                  "properties": {
                    "intentType": {
                      "type": "string",
                      "enum": [
                        "SITE_GUIDE",
                        "CAREER_GUIDANCE",
                        "COURSE_RECOMMENDATION",
                        "PROJECT_RECOMMENDATION",
                        "PROJECT_PLAN_ANALYSIS"
                      ]
                    },
                    "answer": { "type": "string" },
                    "keywords": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "suggestedRouteKeys": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "followUpQuestions": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "recommendedCourses": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "additionalProperties": false,
                        "required": ["courseId", "reason"],
                        "properties": {
                          "courseId": { "type": "string" },
                          "reason": { "type": "string" }
                        }
                      }
                    },
                    "recommendedProjects": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "additionalProperties": false,
                        "required": ["projectPostId", "reason", "recommendedPosition"],
                        "properties": {
                          "projectPostId": { "type": "string" },
                          "reason": { "type": "string" },
                          "recommendedPosition": { "type": "string" }
                        }
                      }
                    }
                  }
                }
                """);
    }

    public JsonNode projectPlanInsightSchema() {
        return parseSchema("""
                {
                  "type": "object",
                  "additionalProperties": false,
                  "required": [
                    "analysisSummary",
                    "keywords",
                    "recommendedCourses",
                    "similarProjects",
                    "notificationMessage"
                  ],
                  "properties": {
                    "analysisSummary": { "type": "string" },
                    "keywords": {
                      "type": "array",
                      "items": { "type": "string" }
                    },
                    "recommendedCourses": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "additionalProperties": false,
                        "required": ["courseId", "reason"],
                        "properties": {
                          "courseId": { "type": "string" },
                          "reason": { "type": "string" }
                        }
                      }
                    },
                    "similarProjects": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "additionalProperties": false,
                        "required": ["projectPostId", "reason", "recommendedPosition"],
                        "properties": {
                          "projectPostId": { "type": "string" },
                          "reason": { "type": "string" },
                          "recommendedPosition": { "type": "string" }
                        }
                      }
                    },
                    "notificationMessage": { "type": "string" }
                  }
                }
                """);
    }

    private JsonNode parseSchema(String schema) {
        try {
            return objectMapper.readTree(schema);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse AI JSON schema.", exception);
        }
    }
}
