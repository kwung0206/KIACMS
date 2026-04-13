package com.kiacms.project.repository;

import com.kiacms.project.entity.ProjectApplication;
import com.kiacms.project.entity.ProjectPosition;
import com.kiacms.project.entity.ProjectPost;
import com.kiacms.project.enums.ApplicationStatus;
import com.kiacms.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, UUID> {

    List<ProjectApplication> findAllByApplicantOrderByCreatedAtDesc(User applicant);

    List<ProjectApplication> findAllByProjectPositionOrderByCreatedAtAsc(ProjectPosition projectPosition);

    List<ProjectApplication> findAllByProjectPosition_ProjectPostOrderByCreatedAtAsc(ProjectPost projectPost);

    Optional<ProjectApplication> findByProjectPositionAndApplicant(ProjectPosition projectPosition, User applicant);

    Optional<ProjectApplication> findByIdAndApplicant(UUID id, User applicant);

    long countByProjectPositionAndStatus(ProjectPosition projectPosition, ApplicationStatus status);

    boolean existsByProjectPosition_ProjectPostAndApplicant(ProjectPost projectPost, User applicant);

    void deleteByIdAndApplicant(UUID id, User applicant);
}
