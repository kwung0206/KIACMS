package com.kiacms.ai.service;

import com.kiacms.ai.dto.request.SimilarProjectRecommendationRequest;
import com.kiacms.ai.dto.response.ProjectRecommendationItemResponse;
import com.kiacms.ai.dto.response.SimilarProjectRecommendationResponse;
import com.kiacms.ai.enums.AiFeatureType;
import com.kiacms.ai.enums.AiReferenceType;
import com.kiacms.global.exception.BusinessException;
import com.kiacms.global.exception.ErrorCode;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ProjectPostStatus;
import com.kiacms.project.repository.ProjectPositionRepository;
import com.kiacms.project.repository.ProjectPostRepository;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SimilarProjectRecommendationAiService {

    private final ProjectPostRepository projectPostRepository;
    private final ProjectPositionRepository projectPositionRepository;
    private final OpenAiStructuredResponseClient openAiStructuredResponseClient;
    private final AiPromptFactory aiPromptFactory;
    private final AiSchemaFactory aiSchemaFactory;

    @Transactional(readOnly = true)
    public SimilarProjectRecommendationResponse recommendProjects(
            SimilarProjectRecommendationRequest request,
            User requester
    ) {
        int limit = request.limit() == null ? 3 : request.limit();
        List<ProjectPost> posts = projectPostRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(post -> post.getStatus() != ProjectPostStatus.ARCHIVED)
                .limit(20)
                .toList();

        if (posts.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "No project board data is available for AI recommendation.");
        }

        List<AiPromptFactory.ProjectContext> contexts = posts.stream()
                .map(post -> new AiPromptFactory.ProjectContext(
                        post,
                        projectPositionRepository.findAllByProjectPostOrderByCreatedAtAsc(post)
                ))
                .toList();

        SimilarProjectRecommendationResponse rawResponse = openAiStructuredResponseClient.requestStructuredOutput(
                requester,
                AiFeatureType.SIMILAR_PROJECT_RECOMMENDATION,
                AiReferenceType.FREE_TEXT,
                null,
                aiPromptFactory.buildSimilarProjectRecommendationSystemPrompt(),
                aiPromptFactory.buildSimilarProjectRecommendationUserPrompt(request, contexts),
                "similar_project_recommendation_response",
                aiSchemaFactory.similarProjectRecommendationSchema(),
                SimilarProjectRecommendationResponse.class,
                1000
        );

        Map<UUID, ProjectPost> allowedPosts = posts.stream()
                .collect(java.util.stream.Collectors.toMap(ProjectPost::getId, Function.identity()));

        List<ProjectRecommendationItemResponse> normalized = rawResponse.recommendations().stream()
                .filter(item -> item.projectPostId() != null && allowedPosts.containsKey(item.projectPostId()))
                .limit(limit)
                .map(item -> new ProjectRecommendationItemResponse(
                        item.projectPostId(),
                        allowedPosts.get(item.projectPostId()).getTitle(),
                        item.similarityReason(),
                        item.recommendedPosition()
                ))
                .toList();

        return new SimilarProjectRecommendationResponse(request.projectDescription(), normalized);
    }
}
